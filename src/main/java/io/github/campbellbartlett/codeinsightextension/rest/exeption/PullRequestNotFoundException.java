package io.github.campbellbartlett.codeinsightextension.rest.exeption;

public class PullRequestNotFoundException extends RuntimeException {

    public PullRequestNotFoundException(String message) {
        super(message);
    }
}
