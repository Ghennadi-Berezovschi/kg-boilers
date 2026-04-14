package com.kgboilers.exception.boilerinstallationquote;

public class OutOfAreaException extends RuntimeException {
    public OutOfAreaException() {
        super("Postcode out of service area");
    }
}
