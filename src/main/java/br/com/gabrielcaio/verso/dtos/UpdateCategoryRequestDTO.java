package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para atualização de uma categoria")
public class UpdateCategoryRequestDTO {
    @Schema(
        description = "Nome da categoria", 
        example = "Tecnologia", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Nome da categoria é obrigatório")
    private String name;
}
