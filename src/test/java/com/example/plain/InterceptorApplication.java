package com.example.plain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.boot.config.WebFluxConfigurations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootFeaturesApplication(WebFluxConfigurations.class)
@RestController
public class InterceptorApplication {

	@RequestMapping("/")
	public String home() {
		return "Hello World";
	}

	public static void main(String[] args) {
		SpringApplication.run(InterceptorApplication.class, args);
	}

}