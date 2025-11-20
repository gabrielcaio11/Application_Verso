package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para criação de uma nova categoria")
public class CreateCategoryRequestDTO
{

    @Schema(
            description = "Nome da categoria (deve ser único)",
            example = "Tecnologia",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 50
    )
    @NotBlank(message = "Nome da categoria é obrigatório")
    private String name;
}