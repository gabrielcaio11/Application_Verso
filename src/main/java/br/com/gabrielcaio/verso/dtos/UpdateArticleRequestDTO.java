package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para atualização de um artigo")
public class UpdateArticleRequestDTO {

  @Schema(
      description = "Título do artigo",
      example = "Introdução ao Spring Boot",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED,
      minLength = 3,
      maxLength = 200)
  @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
  private String title;

  @Schema(
      description = "Conteúdo do artigo",
      example = "Spring Boot é um framework...",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED,
      minLength = 10)
  @Size(min = 10, message = "Conteúdo deve ter pelo menos 10 caracteres")
  private String content;

  @Schema(
      description = "Nome da categoria do artigo",
      example = "Tecnologia",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String category;

  @Schema(
      description = "Status do artigo",
      example = "PUBLICADO",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Status é obrigatório")
  private String status;
}
