package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para criação e gerenciamento de usuários")
public class UserDTO {

  @Schema(
      description = "Email do usuário (deve ser único)",
      example = "usuario@email.com",
      requiredMode = Schema.RequiredMode.REQUIRED,
      pattern = "^[A-Za-z0-9+_.-]+@(.+)$")
  private String email;

  @Schema(
      description = "Senha do usuário",
      example = "senhaSegura123",
      requiredMode = Schema.RequiredMode.REQUIRED,
      minLength = 6,
      format = "password")
  private String password;

  @Schema(
      description = "Roles/permissões do usuário",
      example = "[\"USER\", \"ADMIN\"]",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Set<String> roles = new HashSet<>();
}
