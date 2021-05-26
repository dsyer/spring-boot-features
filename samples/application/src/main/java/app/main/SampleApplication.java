package app.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.config.BasicConfigurations;
import org.springframework.boot.config.SpringBootFeaturesApplication;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.hint.TypeHints;

@TypeHints(@TypeHint(types = BasicConfigurations.class))
@SpringBootFeaturesApplication(BasicConfigurations.class)
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SampleApplication.class);
		app.run(args);
	}

}
