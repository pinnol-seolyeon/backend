package jpabasic.pinnolbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class PinnolBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PinnolBeApplication.class, args);
	}

}
