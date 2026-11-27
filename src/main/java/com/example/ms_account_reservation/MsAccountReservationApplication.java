package com.example.ms_account_reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan("com.example.entity")
@EnableJpaRepositories("com.example.repository")
public class MsAccountReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAccountReservationApplication.class, args);
	}

}
