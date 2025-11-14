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
@Schema(description = "DTO de resposta para criação de artigo")
public class CreateArticleResponseDTO {

    @Schema(
            description = "Título do artigo criado",
            example = "Introdução ao Spring Boot",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
            description = "Conteúdo do artigo criado",
            example = "Spring Boot é um framework que facilita o desenvolvimento de aplicações Java...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Schema(
            description = "Nome da categoria do artigo",
            example = "Tecnologia",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String category;
}