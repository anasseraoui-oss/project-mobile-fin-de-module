package com.elearning.resourceserver.exceptions;
public class QuizSessionExpiredException extends RuntimeException {
    public QuizSessionExpiredException(String message) { super(message); }
}
