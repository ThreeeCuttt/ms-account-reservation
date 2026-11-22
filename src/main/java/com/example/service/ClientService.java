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
        Client client = mapper.toEntity(request);
        // Используем saveAndFlush, чтобы даты createdAt/updatedAt сразу обновились в объекте
        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));
        return mapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientPageResponse searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);

        Specification<Client> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
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
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setMiddleName(request.getMiddleName());

        // saveAndFlush также нужен здесь, чтобы updatedAt обновилось
        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        // Благодаря @SQLDelete в сущности Client, этот вызов сделает soft delete
        clientRepository.delete(client);
    }

    @Transactional(readOnly = true)
    public ClientExistsResponse checkExists(UUID clientId) {
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
