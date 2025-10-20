package com.thilina.WorkingTimeApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WorkingTimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkingTimeApplication.class, args);
	}

}
