package uk.gov.dwp.rbc.sp.underpayments.utils;

public class UeException extends RuntimeException {
    public UeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UeException(String message) {
        super(message);
    }
}
