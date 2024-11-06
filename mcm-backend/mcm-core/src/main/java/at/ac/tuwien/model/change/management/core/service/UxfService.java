package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import org.springframework.core.io.InputStreamSource;

public interface UxfService {

    Configuration createConfigurationFromUxf(InputStreamSource file);
}
