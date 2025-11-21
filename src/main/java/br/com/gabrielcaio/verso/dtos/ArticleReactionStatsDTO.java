package br.com.gabrielcaio.verso.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO com estatísticas de reações de um artigo")
public class ArticleReactionStatsDTO {

  @Schema(description = "ID do artigo", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long articleId;

  @Schema(
      description = "Título do artigo",
      example = "Introdução ao Spring Boot",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String articleTitle;

  @Schema(
      description = "Total de reações no artigo",
      example = "150",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Long totalReactions;

  @Schema(
      description = "Contagem de reações por tipo",
      example = "{\"LIKE\": 100, \"LOVE\": 30, \"LAUGH\": 20}",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Map<String, Long> reactionsByType;

  @Schema(
      description = "Tipo de reação do usuário autenticado (null se não reagiu)",
      example = "LIKE")
  private String userReaction;
}
