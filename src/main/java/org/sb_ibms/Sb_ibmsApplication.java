package org.sb_ibms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "org.sb_ibms")
@EnableJpaRepositories(basePackages = "org.sb_ibms")
public class Sb_ibmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Sb_ibmsApplication.class, args);
    }

}
