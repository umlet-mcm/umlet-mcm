package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Override
    public Configuration createConfiguration(Configuration configuration) {
        return null;
    }

    @Override
    public Configuration updateConfiguration(Configuration configuration) {
        return null;
    }

    @Override
    public void deleteConfiguration(Configuration configuration) {

    }

    @Override
    public Configuration getConfigurationByName(String name) {
        return null;
    }

    @Override
    public List<Configuration> getAllConfigurations() {
        return List.of();
    }
}
