package app.main;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.boot.config.WebFluxConfigurations;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootFeaturesApplication(WebFluxConfigurations.class)
public class SampleApplication {

	@Value("${app.value}")
	private String value;

	@Bean
	public RouterFunction<?> userEndpoints() {
		return route(GET("/"), request -> ok().body(Mono.just(value), String.class));
	}

	@Bean
	public CommandLineRunner runner(ConfigurableListableBeanFactory beans) {
		return args -> {
			System.err.println("Class count: " + ManagementFactory.getClassLoadingMXBean()
					.getTotalLoadedClassCount());
			System.err.println("Bean count: " + beans.getBeanDefinitionNames().length);
			System.err.println(
					"Bean names: " + Arrays.asList(beans.getBeanDefinitionNames()));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
