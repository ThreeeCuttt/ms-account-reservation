package com.example.service;

import com.example.entity.Client;
import com.example.upg4.api.model.*;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setMdmId(client.getMdmId());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setMiddleName(client.getMiddleName());
        response.setStatus(client.getStatus());
        // UTC
        if (client.getCreatedAt() != null) {
            response.setCreatedAt(client.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        }
        if (client.getUpdatedAt() != null) {
            response.setUpdatedAt(client.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
        }
        response.setHasAccounts(false);
        return response;
    }

    public ClientShortDto toShortDto(Client client) {
        ClientShortDto dto = new ClientShortDto();
        dto.setId(client.getId());
        dto.setMdmId(client.getMdmId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setMiddleName(client.getMiddleName());
        dto.setStatus(client.getStatus());
        return dto;
    }

    public Client toEntity(CreateClientRequest request) {
        Client client = new Client();
        client.setMdmId(request.getMdmId());
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setMiddleName(request.getMiddleName());
        client.setStatus(ClientStatus.ACTIVE); // По умолчанию активен
        return client;
    }
}
