package io.github.campbellbartlett.codeinsightextension.rest;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }
}
