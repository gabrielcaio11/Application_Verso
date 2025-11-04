package br.com.gabrielcaio.verso.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequestDTO {
    @NotBlank(message = "Nome da categoria é obrigatório")
    private String name;
}
