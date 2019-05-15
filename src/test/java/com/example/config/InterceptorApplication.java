package com.example.config;

import org.aspectj.lang.Aspects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.BackgroundPreinitializer;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.boot.config.WebFluxConfigurations;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.customizer.SpringApplicationCustomizer;
import org.springframework.boot.customizer.SpringApplicationCustomizers;
import org.springframework.boot.factory.AgentInstaller;
import org.springframework.boot.factory.FactoryInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootFeaturesApplication(WebFluxConfigurations.class)
@RestController
@SpringApplicationCustomizers(SimpleFactories.class)
public class InterceptorApplication {

	@RequestMapping("/")
	public String home() {
		return "Hello World";
	}

	public static void main(String[] args) {
		AgentInstaller.install();
		SpringApplication.run(InterceptorApplication.class, args);
	}

}

@Configuration(proxyBeanMethods = false)
class CheckAspectsConfiguration {

	@Bean
	public FactoryInterceptor interceptor() {
		// This will barf at runtime if the weaver isn't working (probably a
		// good thing)
		return Aspects.aspectOf(FactoryInterceptor.class);
	}

}

class SimpleFactories implements SpringApplicationCustomizer {

	@Override
	public void customize(SpringApplication application) {
		System.err.println("Customizing SpringApplication");
		application.addListeners(new BackgroundPreinitializer(),
				new ConfigFileApplicationListener(), new LoggingApplicationListener());
		application.addInitializers(new ContextIdApplicationContextInitializer());
	}

}