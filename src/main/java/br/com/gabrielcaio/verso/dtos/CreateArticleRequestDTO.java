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
@Schema(description = "DTO para criação de um novo artigo")
public class CreateArticleRequestDTO {

    @Schema(
            description = "Título do artigo",
            example = "Introdução ao Spring Boot",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 200
    )
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, max = 200, message = "Título deve ter entre 3 e 200 caracteres")
    private String title;

    @Schema(
            description = "Conteúdo do artigo",
            example = "Spring Boot é um framework...",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 10
    )
    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(min = 10, message = "Conteúdo deve ter pelo menos 10 caracteres")
    private String content;

    @Schema(
            description = "Nome da categoria do artigo",
            example = "Tecnologia",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Categoria é obrigatória")
    private String category;

    @Schema(
            description = "Status do artigo (RASCUNHO, PUBLICADO)",
            example = "RASCUNHO",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Status é obrigatório")
    private String status;
}