package br.com.gabrielcaio.verso.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de reação que pode ser aplicada a um artigo")
public enum ReactionType {

    @Schema(description = "Curtir - indica que o usuário gostou do artigo")
    LIKE,

    @Schema(description = "Amar - indica que o usuário amou o artigo")
    LOVE,

    @Schema(description = "Rir - indica que o usuário achou o artigo engraçado")
    LAUGH,

    @Schema(description = "Uau - indica que o usuário ficou impressionado")
    WOW,

    @Schema(description = "Triste - indica que o usuário ficou triste com o artigo")
    SAD,

    @Schema(description = "Raiva - indica que o usuário ficou com raiva")
    ANGRY
}
