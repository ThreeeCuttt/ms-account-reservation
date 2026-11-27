package com.example.service;

import com.example.entity.Client;
import com.example.repository.ClientRepository;
import com.example.service.exception.ClientNotFoundException;
import com.example.service.exception.MdmIdConflictException;
import com.example.upg4.api.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Client client = Client.builder()
                .mdmId(request.getMdmId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .build();

        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(UUID clientId) {
        Client client = clientRepository.findByIdAndStatusNot(clientId, ClientStatus.DELETED)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));
        return mapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientPageResponse searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);


        Specification<Client> spec = ClientSpecifications.isNotDeleted()
                .and(ClientSpecifications.hasLastName(lastName))
                .and(ClientSpecifications.hasMdmId(mdmId));

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
        Client client = clientRepository.findByIdAndStatusNot(clientId, ClientStatus.DELETED)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        mapper.updateEntityFromDto(request, client);
        return mapper.toResponse(clientRepository.saveAndFlush(client));
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        Client client = clientRepository.findByIdAndStatusNot(clientId, ClientStatus.DELETED)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден"));

        // Заглушка на проверку счетов
        // if (hasActiveAccounts(clientId)) throw new ClientHasActiveAccountsException("...");

        client.setStatus(ClientStatus.DELETED);
        clientRepository.save(client);
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
