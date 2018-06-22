package com.test.ruleeditor.exception;

/**
 * Created by Srilakshmi on 31/08/17.
 */
public class RuleCreationException extends RuntimeException {

    public RuleCreationException(String message) {
        super(message);
    }

    public RuleCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
