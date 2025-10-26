package com.volunteerBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolunteerBackendApplication {

	public static void main(String[] args) {
		// System.out.println("Current JVM timezone: " + ZoneId.systemDefault());
		SpringApplication.run(VolunteerBackendApplication.class, args);
	}

}
