package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.config.BasicConfigurations;
import org.springframework.boot.config.SpringBootFeaturesApplication;

@SpringBootFeaturesApplication(BasicConfigurations.class)
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.run(args);
	}

}
