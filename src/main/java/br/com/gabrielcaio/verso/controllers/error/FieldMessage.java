package br.com.gabrielcaio.verso.controllers.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FieldMessage {
  private String fieldName;
  private String message;
}
