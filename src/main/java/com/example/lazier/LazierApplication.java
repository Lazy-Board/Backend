package com.example.lazier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LazierApplication {

	public static void main(String[] args) {
		SpringApplication.run(LazierApplication.class, args);
	}

}
