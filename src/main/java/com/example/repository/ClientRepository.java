package com.example.repository;

import com.example.entity.Client;
import com.example.upg4.api.model.ClientStatus; // <-- Добавлен импорт
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional; // <-- Добавлен импорт
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID>, JpaSpecificationExecutor<Client> {
    boolean existsByMdmId(Long mdmId);

    // Метод для поиска клиента, который НЕ удален
    Optional<Client> findByIdAndStatusNot(UUID id, ClientStatus status);
}
