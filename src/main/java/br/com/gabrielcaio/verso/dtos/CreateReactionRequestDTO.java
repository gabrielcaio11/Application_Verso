package br.com.gabrielcaio.verso.dtos;

import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO de requisição para criar ou atualizar uma reação")
public class CreateReactionRequestDTO
{

    @NotNull(message = "Tipo de reação é obrigatório")
    @Schema(description = "Tipo de reação (LIKE, LOVE, LAUGH, WOW, SAD, ANGRY)", example = "LIKE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ReactionType type;
}
