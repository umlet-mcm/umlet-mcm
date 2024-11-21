package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

@Service
public class UxfServiceImpl implements UxfService{
    @Override
    public Configuration createConfigurationFromUxf(InputStreamSource file) {
        return null;
    }
}
