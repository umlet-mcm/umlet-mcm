package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.InvalidNameException;
import lombok.NonNull;


public interface NameValidationService {

    /**
     * Validates the name of a repository.
     *
     * @param name the name to validate
     * @throws InvalidNameException if the name is invalid
     */
    void validateRepositoryName(@NonNull String name) throws InvalidNameException;

    /**
     * Encodes a version name for use as a Git tag
     *
     * @param name the name to encode
     * @param sanitize whether to sanitize the name
     *                 i.e., remove invalid characters that will NOT be restored during decoding
     * @return the encoded name
     */
    String encodeVersionName(@NonNull String name, boolean sanitize);

    /**
     * Decodes a version name from a Git tag
     *
     * @param name the name to decode
     * @return the decoded name
     */
    String decodeVersionName(@NonNull String name);
}
