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
@Schema(description = "DTO de resposta com informações da notificação")
public class NotificationResponseDTO {

    @Schema(description = "ID da notificação", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long notificationId;

    @Schema(description = "Mensagem da notificação", example = "João publicou um novo artigo: Introdução ao Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "ID do artigo relacionado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long articleId;

    @Schema(description = "Título do artigo", example = "Introdução ao Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED)
    private String articleTitle;

    @Schema(description = "Se a notificação foi lida", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean read;

    @Schema(description = "Data de criação da notificação", example = "2024-01-15T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}