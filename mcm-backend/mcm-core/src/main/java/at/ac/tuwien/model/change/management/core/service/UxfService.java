package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import org.springframework.core.io.InputStreamSource;

public interface UxfService {

    Configuration createConfigurationFromUxf(InputStreamSource file) throws UxfException;

    Configuration addUxfToConfiguration(InputStreamSource file, String configurationUUID) throws UxfException, ConfigurationException;

    String exportModel(String modelUuid) throws ModelNotFoundException, UxfException;

    String exportConfiguration(String configurationUuid) throws UxfException;
}
