package com.example.controller;

import com.example.service.ClientService;
import com.example.upg4.api.ClientsApi;
import com.example.upg4.api.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ClientController implements ClientsApi {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public ResponseEntity<ClientResponse> createClient(CreateClientRequest createClientRequest) {
        ClientResponse response = clientService.createClient(createClientRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ClientResponse> getClientById(UUID clientId) {
        ClientResponse response = clientService.getClientById(clientId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientPageResponse> searchClients(Integer page, Integer size, String lastName, Long mdmId) {
        ClientPageResponse response = clientService.searchClients(page, size, lastName, mdmId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ClientResponse> updateClient(UUID clientId, UpdateClientRequest updateClientRequest) {
        ClientResponse response = clientService.updateClient(clientId, updateClientRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteClient(UUID clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClientExistsResponse> checkClientExists(UUID clientId) {
        ClientExistsResponse response = clientService.checkExists(clientId);
        return ResponseEntity.ok(response);
    }
}
