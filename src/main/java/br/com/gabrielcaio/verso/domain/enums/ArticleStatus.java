package br.com.gabrielcaio.verso.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status de publicação de um artigo")
public enum ArticleStatus {

    @Schema(description = "Artigo em rascunho - apenas visível pelo autor")
    RASCUNHO,

    @Schema(description = "Artigo publicado - visível para todos os usuários autenticados")
    PUBLICADO
}