package io.github.campbellbartlett.codeinsightextension.rest.exeption;

public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
