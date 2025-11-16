package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import br.com.gabrielcaio.verso.dtos.ArticleReactionStatsDTO;
import br.com.gabrielcaio.verso.dtos.CreateReactionRequestDTO;
import br.com.gabrielcaio.verso.dtos.ReactionResponseDTO;
import br.com.gabrielcaio.verso.services.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verso/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "Endpoints para gerenciamento de reações em artigos")
@Slf4j
public class ReactionController {

    private final ReactionService reactionService;

    @Operation(
            summary = "Adicionar ou atualizar reação em um artigo",
            description = "Adiciona uma nova reação ou atualiza a reação existente do usuário autenticado em um artigo publicado. Se o usuário já reagiu, a reação será atualizada."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reação adicionada/atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ReactionResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Apenas artigos publicados podem receber reações"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/articles/{articleId}")
    public ResponseEntity<ReactionResponseDTO> addOrUpdateReaction(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            ) @PathVariable Long articleId,
            @Valid @RequestBody CreateReactionRequestDTO dto
    ) {
        log.info("Adicionando/atualizando reação para o artigo ID: {} com tipo de reação: {}", articleId, dto.getType());
        var response = reactionService.addOrUpdateReaction(articleId, dto);
        log.info("Reação adicionada/atualizada com sucesso para o artigo ID: {}", articleId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Remover reação de um artigo",
            description = "Remove a reação do usuário autenticado de um artigo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reação removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Reação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<Void> removeReaction(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            ) @PathVariable Long articleId
    ) {
        log.info("Removendo reação para o artigo ID: {}", articleId);
        reactionService.removeReaction(articleId);
        log.info("Reação removida com sucesso para o artigo ID: {}", articleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Listar reações de um artigo",
            description = "Retorna uma lista paginada de todas as reações de um artigo específico."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @Parameters({
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "page",
                    description = "Número da página (inicia em 0). Padrão: 0",
                    example = "0",
                    schema = @Schema(type = "integer", defaultValue = "0")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "size",
                    description = "Quantidade de itens por página. Padrão: 10",
                    example = "10",
                    schema = @Schema(type = "integer", defaultValue = "10")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "sort",
                    description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
                    examples = {
                            @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
                            @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
                    },
                    schema = @Schema(type = "string", defaultValue = "createdAt,DESC")
            )
    })
    @GetMapping("/articles/{articleId}")
    public ResponseEntity<Page<ReactionResponseDTO>> findAllReactionsByArticle(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long articleId,
            @ParameterObject Pageable pageable
    ) {
        log.info("Buscando reações para o artigo ID: {}. Página: {}, Tamanho: {}", articleId, pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = reactionService.findAllReactionsByArticle(articleId, pageable);
        log.info("Total de reações encontradas para o artigo ID {}: {}", articleId, pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }

    @Operation(
            summary = "Listar reações do usuário autenticado",
            description = "Retorna uma lista paginada de todas as reações feitas pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de reações retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @Parameters({
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "page",
                    description = "Número da página (inicia em 0). Padrão: 0",
                    example = "0",
                    schema = @Schema(type = "integer", defaultValue = "0")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "size",
                    description = "Quantidade de itens por página. Padrão: 10",
                    example = "10",
                    schema = @Schema(type = "integer", defaultValue = "10")
            ),
            @Parameter(
                    in = ParameterIn.QUERY,
                    name = "sort",
                    description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
                    examples = {
                            @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
                            @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
                    },
                    schema = @Schema(type = "string", defaultValue = "createdAt,DESC")
            )
    })
    @GetMapping("/my-reactions")
    public ResponseEntity<Page<ReactionResponseDTO>> findAllReactionsByUser(
            @ParameterObject
            Pageable pageable
    ) {
        log.info("Buscando reações do usuário autenticado. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = reactionService.findAllReactionsByUser(pageable);
        log.info("Total de reações encontradas para o usuário autenticado: {}", pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }

    @Operation(
            summary = "Obter estatísticas de reações de um artigo",
            description = "Retorna estatísticas detalhadas das reações de um artigo, incluindo contagem por tipo e a reação do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estatísticas retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = ArticleReactionStatsDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/articles/{articleId}/stats")
    public ResponseEntity<ArticleReactionStatsDTO> getArticleReactionStats(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long articleId
    ) {
        log.info("Buscando estatísticas de reações para o artigo ID: {}", articleId);
        var stats = reactionService.getArticleReactionStats(articleId);
        log.info("Estatísticas de reações obtidas com sucesso para o artigo ID: {}", articleId);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    @Operation(
            summary = "Verificar reação do usuário em um artigo",
            description = "Retorna o tipo de reação do usuário autenticado em um artigo específico, ou null se não reagiu."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reação do usuário retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = ReactionType.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/articles/{articleId}/my-reaction")
    public ResponseEntity<ReactionType> getUserReaction(
            @Parameter(
                    description = "ID do artigo",
                    example = "1",
                    required = true
            )
            @PathVariable Long articleId
    ) {
        log.info("Buscando reação do usuário para o artigo ID: {}", articleId);
        ReactionType reaction = reactionService.getUserReaction(articleId);
        log.info("Reação do usuário obtida com sucesso para o artigo ID: {}", articleId);
        return ResponseEntity.status(HttpStatus.OK).body(reaction);
    }
}
