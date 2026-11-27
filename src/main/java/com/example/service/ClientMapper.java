package com.example.service; // Укажи свой пакет, где лежит маппер

import com.example.entity.Client;
import com.example.upg4.api.model.ClientResponse;
import com.example.upg4.api.model.CreateClientRequest;
import com.example.upg4.api.model.UpdateClientRequest;
import com.example.upg4.api.model.ClientShortDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    // 1. Создание Entity из DTO запроса
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client toEntity(CreateClientRequest request);

    // 2. Преобразование Entity в Response (полный ответ)
    ClientResponse toResponse(Client client);

    // 3. Преобразование Entity в краткий DTO для пагинации
    ClientShortDto toShortDto(Client client);

    // 4. Обновление существующей Entity из DTO (для PUT запроса)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mdmId", ignore = true) // mdmId нельзя менять при обновлении
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateClientRequest request, @MappingTarget Client client);
}
