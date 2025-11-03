package br.com.gabrielcaio.verso.controllers.error;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}