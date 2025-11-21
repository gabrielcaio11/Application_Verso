package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Representação de um response de categoria")
public class CategoryResponseWithNameDTO {
  @Schema(
      description = "Nome da categoria",
      example = "Tecnologia",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;
}
