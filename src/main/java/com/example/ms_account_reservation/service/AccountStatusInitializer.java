package com.example.ms_account_reservation.service;

import com.example.ms_account_reservation.entity.AccountStatus;
import com.example.ms_account_reservation.repository.AccountStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AccountStatusInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AccountStatusInitializer.class);

    private static final Map<String, String> DEFAULT_STATUSES = new LinkedHashMap<>();

    static {
        DEFAULT_STATUSES.put("NEW", "Счёт создан в БД");
        DEFAULT_STATUSES.put("IN_CREATION", "Запрос на создание счета был отправлен в смежную систему");
        DEFAULT_STATUSES.put("CREATED", "Счёт создан в смежной системе");
        DEFAULT_STATUSES.put("CANCELLED", "Счёт аннулирован");
        DEFAULT_STATUSES.put("CLOSED", "Счёт закрыт");
    }

    private final AccountStatusRepository repository;

    public AccountStatusInitializer(AccountStatusRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Проверка справочника account_status...");

        DEFAULT_STATUSES.forEach((name, description) -> {
            if (!repository.existsByName(name)) {
                repository.save(new AccountStatus(name, description));
                log.info("Добавлен статус: {} — {}", name, description);
            }
        });

        log.info("Справочник account_status готов. Всего записей: {}", repository.count());
    }
}
