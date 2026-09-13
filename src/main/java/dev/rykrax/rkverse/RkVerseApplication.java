package dev.rykrax.rkverse;

import dev.rykrax.rkverse.feature.user.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RkVerseApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RkVerseApplication.class, args);
    }
}
