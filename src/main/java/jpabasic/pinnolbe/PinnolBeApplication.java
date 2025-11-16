package jpabasic.pinnolbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoAuditing
@EnableCaching
public class PinnolBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PinnolBeApplication.class, args);
	}

}
