package tech.badprogrammer.swayamscraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwayamScraperApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwayamScraperApplication.class, args);
    }

}
