package at.ac.tuwien.model.change.management.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "at.ac.tuwien.model.change.management")
public class ModelChangeManagementServer {

    public static void main(String[] args) {
        SpringApplication.run(ModelChangeManagementServer.class, args);
    }
}
