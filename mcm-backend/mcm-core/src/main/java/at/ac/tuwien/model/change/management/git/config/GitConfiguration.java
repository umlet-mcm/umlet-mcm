package at.ac.tuwien.model.change.management.git.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GitProperties.class)
public class GitConfiguration {
}
