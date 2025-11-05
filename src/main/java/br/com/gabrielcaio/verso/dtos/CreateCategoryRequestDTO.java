package br.com.gabrielcaio.verso.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para criação de uma nova categoria")
public class CreateCategoryRequestDTO {
    
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