package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.ConfigurationException;
import at.ac.tuwien.model.change.management.core.exception.ModelNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UxfException;
import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import org.springframework.core.io.InputStreamSource;

public interface UxfService {

    Configuration createConfigurationFromUxf(InputStreamSource file, String name, String version) throws UxfException;

    Configuration updateConfigurationFromUxf(InputStreamSource file, String configurationName, String version) throws UxfException;

    Configuration addUxfToConfiguration(InputStreamSource file, String configurationUUID, String modelName) throws UxfException, ConfigurationException;

    String exportModel(String configurationName, String modelUuid) throws ModelNotFoundException, UxfException;

    String exportModel(Model model) throws UxfException;

    String exportConfiguration(String configurationUuid) throws UxfException;

    Model updateModelFromUxf(InputStreamSource file, String newModelName) throws UxfException;

    default Configuration createConfigurationFromUxf(InputStreamSource file, String name) throws UxfException {
        return createConfigurationFromUxf(file, name, null);
    }

    default Configuration updateConfigurationFromUxf(InputStreamSource file, String configurationName) throws UxfException {
        return updateConfigurationFromUxf(file, configurationName, null);
    }
}
