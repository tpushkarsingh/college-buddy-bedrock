package com.college.buddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class CollegeBuddyApplication {

    public static void main(String[] args) {
        System.setProperty("spring.classformat.ignore", "true");
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(CollegeBuddyApplication.class, args);
    }

}
