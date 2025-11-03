package br.com.gabrielcaio.verso.controllers.error;

public class DataBaseException extends RuntimeException {
    public DataBaseException(String message) {
        super(message);
    }
}