package com.example.service;

import com.example.entity.Client;
import com.example.upg4.api.model.ClientStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ClientSpecifications {

    // Фильтр: статус клиента не должен быть DELETED
    public static Specification<Client> isNotDeleted() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), ClientStatus.DELETED);
    }

    // Фильтр: поиск по фамилии (без учета регистра)
    public static Specification<Client> hasLastName(String lastName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(lastName)) {
                return null; // Если фильтр не передан, игнорируем его
            }
            return cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%");
        };
    }

    // Фильтр: точное совпадение по mdmId
    public static Specification<Client> hasMdmId(Long mdmId) {
        return (root, query, cb) -> {
            if (mdmId == null) {
                return null; // Если фильтр не передан, игнорируем его
            }
            return cb.equal(root.get("mdmId"), mdmId);
        };
    }
}
