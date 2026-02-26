package com.oceanpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.oceanpay.repository")
@EntityScan(basePackages = "com.oceanpay.entity")
@EnableTransactionManagement
public class OceanPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(OceanPayApplication.class, args);
    }
}