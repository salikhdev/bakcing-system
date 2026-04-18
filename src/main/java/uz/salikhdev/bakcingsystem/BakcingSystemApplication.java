package uz.salikhdev.bakcingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BakcingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakcingSystemApplication.class, args);
    }

}
