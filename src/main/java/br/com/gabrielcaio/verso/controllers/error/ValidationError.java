package br.com.gabrielcaio.verso.controllers.error;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ValidationError extends ErrorMessage {
  private List<FieldMessage> errors = new ArrayList<FieldMessage>();

  public ValidationError(Instant timestamp, Integer status, String error, String path) {
    super(timestamp, status, error, path);
  }

  public void addError(String field, String defaultMessage) {
    errors.add(new FieldMessage(field, defaultMessage));
  }
}
