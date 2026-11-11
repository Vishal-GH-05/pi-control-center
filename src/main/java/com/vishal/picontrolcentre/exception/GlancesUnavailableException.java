package com.vishal.picontrolcentre.exception;

public class GlancesUnavailableException extends RuntimeException {

    public GlancesUnavailableException(String message) {
        super(message);
    }

    public GlancesUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}