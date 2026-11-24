package com.example.service;

import com.example.entity.Client;
import com.example.repository.ClientRepository;
import com.example.service.exception.ClientNotFoundException;
import com.example.service.exception.MdmIdConflictException;
import com.example.upg4.api.model.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper mapper;

    public ClientService(ClientRepository clientRepository, ClientMapper mapper) {
        this.clientRepository = clientRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ClientResponse createClient(CreateClientRequest request) {
        if (clientRepository.existsByMdmId(request.getMdmId())) {
            throw new MdmIdConflictException("Клиент с mdmId " + request.getMdmId() + " уже существует");
        }

        // Шаг 1.3: Использование @Builder для создания сущности
        Client client = Client.builder()
                .mdmId(request.getMdmId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                // status = ACTIVE подтянется из @Builder.Default
                .build();

        // Используем saveAndFlush, чтобы даты createdAt/updatedAt сразу обновились в объекте
        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID clientId) {
        // Этап 2: Так как мы уберем @SQLRestriction, нужно руками проверять, что клиент не удален
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getStatus() != ClientStatus.DELETED) // <-- Явная проверка
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));
        return mapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientPageResponse searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);

        // Этап 4: Здесь позже вынесем это в класс ClientSpecifications
        Specification<Client> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Этап 2: Явно исключаем удаленных клиентов из поиска
            predicates.add(cb.notEqual(root.get("status"), ClientStatus.DELETED));

            if (StringUtils.hasText(lastName)) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            }
            if (mdmId != null) {
                predicates.add(cb.equal(root.get("mdmId"), mdmId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Client> clientsPage = clientRepository.findAll(spec, pageable);

        ClientPageResponse response = new ClientPageResponse();
        response.setContent(clientsPage.getContent().stream().map(mapper::toShortDto).toList());

        PageableInfo pageableInfo = new PageableInfo();
        pageableInfo.setPageNumber(clientsPage.getNumber());
        pageableInfo.setPageSize(clientsPage.getSize());
        pageableInfo.setTotalPages(clientsPage.getTotalPages());
        pageableInfo.setTotalElements(clientsPage.getTotalElements());
        response.setPageable(pageableInfo);

        return response;
    }

    @Transactional
    public ClientResponse updateClient(UUID clientId, UpdateClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getStatus() != ClientStatus.DELETED)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        // Используем MapStruct для обновления полей
        mapper.updateEntityFromDto(request, client);

        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        // Этап 2: Логика Soft Delete перенесена сюда из аннотаций @SQLDelete
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getStatus() != ClientStatus.DELETED) // Если уже удален, кидаем 404
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        // Проверка на активные счета (заглушка из задания)
        // if (hasActiveAccounts(clientId)) {
        //     throw new ClientHasActiveAccountsException("У клиента есть активные счета");
        // }

        client.setStatus(ClientStatus.DELETED);
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public ClientExistsResponse checkExists(UUID clientId) {
        // Для проверки существования мы ищем без фильтра по статусу,
        // чтобы вернуть status = DELETED, если клиент удален (как в твоем коде)
        Client client = clientRepository.findById(clientId).orElse(null);
        ClientExistsResponse response = new ClientExistsResponse();
        response.setClientId(clientId);
        if (client != null) {
            response.setExists(true);
            response.setStatus(client.getStatus());
        } else {
            response.setExists(false);
        }
        return response;
    }
}
