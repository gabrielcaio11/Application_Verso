package br.com.gabrielcaio.verso.dtos;

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
@Schema(description = "DTO de resposta com informações de seguimento")
public class FollowResponseDTO
{

    @Schema(description = "ID do relacionamento de seguimento", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long followId;

    @Schema(description = "ID do usuário que está seguindo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long followerId;

    @Schema(description = "Username do usuário que está seguindo", example = "joao", requiredMode = Schema.RequiredMode.REQUIRED)
    private String followerUsername;

    @Schema(description = "ID do usuário sendo seguido", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long followingId;

    @Schema(description = "Username do usuário sendo seguido", example = "maria", requiredMode = Schema.RequiredMode.REQUIRED)
    private String followingUsername;

    @Schema(description = "Data em que o seguimento foi criado", example = "2024-01-15T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}