/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.test;

import java.util.Date;
import java.util.Map;

import app.test.OverrideApplicationTests.OverrideApplication;
import app.test.OverrideApplicationTests.SampleController;
import com.samskivert.mustache.Mustache.Compiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.boot.config.WebFluxConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.reactive.result.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.WebHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dave Syer
 *
 */
@SpringBootTest(classes = { OverrideApplication.class, SampleController.class })
public class OverrideApplicationTests {

	@Autowired
	private WebHandler webHandler;

	private WebTestClient client;

	@BeforeEach
	public void init() {
		client = WebTestClient.bindToWebHandler(webHandler).build();
	}

	@Test
	public void test() {
		client.get().uri("/").exchange().expectBody(String.class)
				.value(value -> assertThat(value).contains("Msg: Hello"));
	}

	@SpringBootFeaturesApplication({ WebFluxConfigurations.class,
			MustacheAutoConfiguration.class })
	static class OverrideApplication {

		public static void main(String[] args) throws Exception {
			SpringApplication.run(OverrideApplicationTests.OverrideApplication.class,
					args);
		}

		@Bean
		public MustacheViewResolver mustacheViewResolver(Compiler mustacheCompiler) {
			MustacheViewResolver resolver = new MustacheViewResolver(mustacheCompiler);
			resolver.setPrefix("classpath:/mustache/");
			resolver.setSuffix(".html");
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}

	}

	@Controller
	static class SampleController {

		@GetMapping("/")
		public String home(Map<String, Object> model) {
			model.put("time", new Date());
			model.put("message", "Hello");
			return "index";
		}
	}

}
