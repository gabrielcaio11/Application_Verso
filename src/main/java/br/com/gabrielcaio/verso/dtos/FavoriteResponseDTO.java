package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de resposta com informações do artigo favoritado")
public class FavoriteResponseDTO {

    @Schema(
            description = "ID do favorito",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long favoriteId;

    @Schema(
            description = "ID do artigo",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long articleId;

    @Schema(
            description = "Título do artigo",
            example = "Introdução ao Spring Boot",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String articleTitle;

    @Schema(
            description = "Nome da categoria do artigo",
            example = "Tecnologia",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String categoryName;

    @Schema(
            description = "Data em que o artigo foi favoritado",
            example = "2024-01-15T10:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime favoritedAt;
}