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
@Schema(description = "DTO para atualização de um artigo")
public class UpdateArticleRequestDTO {
    @Schema(
        description = "Título do artigo", 
        example = "Introdução ao Spring Boot", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED, 
        minLength = 3, 
        maxLength = 200
    )
    private String title;
    
    @Schema(
        description = "Conteúdo do artigo", 
        example = "Spring Boot é um framework...", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED, 
        minLength = 10
    )
    private String content;
    
    @Schema(
        description = "Nome da categoria do artigo", 
        example = "Tecnologia", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String category;
    
    @Schema(
        description = "Status do artigo", 
        example = "PUBLICADO", 
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String status;
}