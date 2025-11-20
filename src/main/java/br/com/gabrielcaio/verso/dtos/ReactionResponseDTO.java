package br.com.gabrielcaio.verso.dtos;

import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de resposta com informações da reação")
public class ReactionResponseDTO
{

    @Schema(
            description = "ID da reação",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long reactionId;

    @Schema(
            description = "Tipo de reação",
            example = "LIKE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ReactionType type;

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
            description = "ID do usuário que reagiu",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    @Schema(
            description = "Username do usuário que reagiu",
            example = "usuario123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @Schema(
            description = "Data em que a reação foi criada",
            example = "2024-01-15T10:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime createdAt;
}