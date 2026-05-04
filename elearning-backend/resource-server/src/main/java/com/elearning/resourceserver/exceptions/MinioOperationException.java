package com.elearning.resourceserver.exceptions;
public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message, Throwable cause) { super(message, cause); }
}
