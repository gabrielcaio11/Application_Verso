package br.com.gabrielcaio.verso.controllers.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError extends ErrorMessage {
    private List<FieldMessage> errors = new ArrayList<FieldMessage>();

    public ValidationError(Instant timestamp, Integer status, String error, String path) {
        super(timestamp, status, error, path);
    }
    public void addError(String field, String defaultMessage) {
        errors.add(new FieldMessage(field, defaultMessage));
    }
}
