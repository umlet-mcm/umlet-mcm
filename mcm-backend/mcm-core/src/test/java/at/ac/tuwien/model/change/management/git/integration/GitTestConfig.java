package at.ac.tuwien.model.change.management.git.integration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "at.ac.tuwien.model.change.management.git",
        "at.ac.tuwien.model.change.management.core.mapper.dsl",
        "at.ac.tuwien.model.change.management.core.transformer",
        "at.ac.tuwien.model.change.management.core.configuration"
})
public class GitTestConfig {
}
