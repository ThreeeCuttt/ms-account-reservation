package com.example.service;

import com.example.entity.Client;
import com.example.repository.ClientRepository;
import com.example.service.exception.ClientNotFoundException;
import com.example.service.exception.MdmIdConflictException;
import com.example.upg4.api.model.ClientResponse;
import com.example.upg4.api.model.ClientStatus;
import com.example.upg4.api.model.CreateClientRequest;
import com.example.upg4.api.model.UpdateClientRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Включает Mockito
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper mapper;

    @InjectMocks
    private ClientService clientService;

    // --- 1. Тест успешного создания ---
    @Test
    void createClient_ShouldReturnResponse() {
        // Arrange
        CreateClientRequest request = new CreateClientRequest();
        request.setMdmId(123L);
        request.setFirstName("Иван");

        Client savedClient = new Client();
        savedClient.setId(UUID.randomUUID());
        savedClient.setMdmId(123L);
        savedClient.setStatus(ClientStatus.ACTIVE);

        ClientResponse expectedResponse = new ClientResponse();
        expectedResponse.setId(savedClient.getId());
        expectedResponse.setStatus(ClientStatus.ACTIVE);

        // Мокаем поведение
        when(clientRepository.existsByMdmId(123L)).thenReturn(false);
        when(clientRepository.saveAndFlush(any(Client.class))).thenReturn(savedClient);
        when(mapper.toResponse(any(Client.class))).thenReturn(expectedResponse);

        // Act
        ClientResponse actualResponse = clientService.createClient(request);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getStatus()).isEqualTo(ClientStatus.ACTIVE);
        verify(clientRepository, times(1)).saveAndFlush(any(Client.class));
    }

    // --- 2. Тест ошибки при создании (дубликат mdmId) ---
    @Test
    void createClient_ShouldThrowConflict_WhenMdmIdExists() {
        CreateClientRequest request = new CreateClientRequest();
        request.setMdmId(123L);

        when(clientRepository.existsByMdmId(123L)).thenReturn(true);

        // Assert
        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOf(MdmIdConflictException.class)
                .hasMessageContaining("уже существует");

        verify(clientRepository, never()).saveAndFlush(any(Client.class));
    }

    // --- 3. Тест получения по ID (не найден) ---
    @Test
    void getClientById_ShouldThrowNotFound_WhenClientIsDeleted() {
        UUID id = UUID.randomUUID();
        // В реальности БД не вернет удаленного клиента, поэтому мокаем empty
        when(clientRepository.findByIdAndStatusNot(id, ClientStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById(id))
                .isInstanceOf(ClientNotFoundException.class);
    }

    // --- 4. Тест получения по ID (клиент удален) ---
    @Test
    void getClientById_ShouldThrowNotFound_WhenClientDoesNotExist() {
        UUID randomId = UUID.randomUUID();
        // Мокаем новый метод
        when(clientRepository.findByIdAndStatusNot(randomId, ClientStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById(randomId))
                .isInstanceOf(ClientNotFoundException.class);
    }

    // --- 5. Тест успешного обновления ---
    @Test
    void updateClient_ShouldReturnUpdatedResponse() {
        UUID id = UUID.randomUUID();
        Client existingClient = new Client();
        existingClient.setId(id);
        existingClient.setStatus(ClientStatus.ACTIVE);

        UpdateClientRequest request = new UpdateClientRequest();
        request.setFirstName("Новое Имя");

        ClientResponse expectedResponse = new ClientResponse();
        expectedResponse.setId(id);
        expectedResponse.setFirstName("Новое Имя");

        // Мокаем новый метод
        when(clientRepository.findByIdAndStatusNot(id, ClientStatus.DELETED))
                .thenReturn(Optional.of(existingClient));
        when(clientRepository.saveAndFlush(existingClient)).thenReturn(existingClient);
        when(mapper.toResponse(existingClient)).thenReturn(expectedResponse);

        ClientResponse actualResponse = clientService.updateClient(id, request);

        assertThat(actualResponse.getFirstName()).isEqualTo("Новое Имя");
        verify(mapper, times(1)).updateEntityFromDto(request, existingClient);
    }

    // --- 6. Тест Soft Delete (проверка, что статус меняется на DELETED) ---
    @Test
    void deleteClient_ShouldSetStatusToDeleted() {
        UUID id = UUID.randomUUID();
        Client activeClient = new Client();
        activeClient.setId(id);
        activeClient.setStatus(ClientStatus.ACTIVE);

        // Мокаем новый метод
        when(clientRepository.findByIdAndStatusNot(id, ClientStatus.DELETED))
                .thenReturn(Optional.of(activeClient));

        clientService.deleteClient(id);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());

        Client savedClient = captor.getValue();
        assertThat(savedClient.getStatus()).isEqualTo(ClientStatus.DELETED);
    }
}
