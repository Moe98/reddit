package org.sab.validation.exceptions;

public class EnvironmentVariableNotLoaded extends Exception {
    public EnvironmentVariableNotLoaded(String envVariableName) {
        super(String.format("The %s environment variable was not loaded", envVariableName));
    }
}
