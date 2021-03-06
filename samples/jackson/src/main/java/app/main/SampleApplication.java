package app.main;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.config.JacksonConfigurations;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.boot.config.WebFluxConfigurations;
import org.springframework.context.annotation.Bean;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.TypeHints;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@TypeHints(@TypeHint(types = { JacksonConfigurations.class,
		WebFluxConfigurations.class }))
@SpringBootFeaturesApplication({ JacksonConfigurations.class,
		WebFluxConfigurations.class })
public class SampleApplication {

	@Bean
	public Foo foo() {
		return new Foo();
	}

	@Bean
	public RouterFunction<?> userEndpoints(Foo foo) {
		return route(GET("/"), request -> ok().body(Mono.just(foo), Foo.class));
	}

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
