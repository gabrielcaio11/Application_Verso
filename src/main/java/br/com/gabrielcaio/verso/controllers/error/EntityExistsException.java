package br.com.gabrielcaio.verso.controllers.error;

public class EntityExistsException extends RuntimeException {
    public EntityExistsException(String message) {
        super(message);
    }
}
