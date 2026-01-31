package spring.secondbite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SecondbiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecondbiteApplication.class, args);
	}

}
