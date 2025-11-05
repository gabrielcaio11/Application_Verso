package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Representação de uma categoria")
public class CategoryDTO {
    
    @Schema(description = "ID único da categoria", example = "1")
    private Long id;
    
    @Schema(description = "Nome da categoria", example = "Tecnologia", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
}