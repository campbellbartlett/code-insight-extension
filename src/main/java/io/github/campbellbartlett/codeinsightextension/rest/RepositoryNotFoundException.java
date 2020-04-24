package io.github.campbellbartlett.codeinsightextension.rest;

public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
