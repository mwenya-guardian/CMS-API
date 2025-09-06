package com.cms.exception;

import java.io.IOException;

public class WrongFileTypeException extends IOException {
    public WrongFileTypeException(String message) {
        super(message);
    }
}
