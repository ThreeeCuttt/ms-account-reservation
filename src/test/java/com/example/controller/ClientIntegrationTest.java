package com.example.controller;

import com.example.AbstractIntegrationTest;
import com.example.entity.Client; // <-- Добавлен импорт
import com.example.repository.ClientRepository; // <-- Добавлен импорт
import com.example.upg4.api.model.ClientStatus; // <-- Добавлен импорт
import com.example.upg4.api.model.CreateClientRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat; // <-- Добавлен импорт для проверок
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void createClient_ShouldReturn201() throws Exception {
        CreateClientRequest request = new CreateClientRequest();
        request.setMdmId(9876543210L);
        request.setFirstName("Иван");
        request.setLastName("Тестов");
        request.setMiddleName("Тестович");

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mdmId").value(9876543210L))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getClientById_ShouldReturn404() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/clients/" + randomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_NOT_FOUND"));
    }

    @Test
    void deleteClient_ShouldReturn204_AndSoftDelete() throws Exception {
        // 1. Создаем клиента
        CreateClientRequest request = new CreateClientRequest();
        request.setMdmId(111222333L);
        request.setFirstName("Удаляемый");
        request.setLastName("Клиент");

        String responseJson = mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String clientId = objectMapper.readTree(responseJson).get("id").asText();

        //(ожидаем 204)
        mockMvc.perform(delete("/api/v1/clients/" + clientId))
                .andExpect(status().isNoContent());

        // (ожидаем 404)
        mockMvc.perform(get("/api/v1/clients/" + clientId))
                .andExpect(status().isNotFound());


        Client deletedClient = clientRepository.findById(UUID.fromString(clientId)).orElseThrow();
        assertThat(deletedClient.getStatus()).isEqualTo(ClientStatus.DELETED);
    }

    @Test
    void createClient_ShouldReturn409_WhenMdmIdExists() throws Exception {
        // 1. Создаем первого клиента
        CreateClientRequest request = new CreateClientRequest();
        request.setMdmId(555666777L);
        request.setFirstName("Первый");
        request.setLastName("Клиент");

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        //(ожидаем 409)
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("MDM_ID_CONFLICT"));
    }

    @Test
    void searchClients_ShouldReturnPaginatedResult() throws Exception {

        for (int i = 0; i < 3; i++) {
            CreateClientRequest request = new CreateClientRequest();
            request.setMdmId(100000000L + i);
            request.setFirstName("Иван" + i);
            request.setLastName("Тестов" + i);
            mockMvc.perform(post("/api/v1/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }


        mockMvc.perform(get("/api/v1/clients?page=0&size=2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(2));
    }
}
