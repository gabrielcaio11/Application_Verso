package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import br.com.gabrielcaio.verso.dtos.ArticleReactionStatsDTO;
import br.com.gabrielcaio.verso.dtos.CreateReactionRequestDTO;
import br.com.gabrielcaio.verso.dtos.ReactionResponseDTO;
import br.com.gabrielcaio.verso.services.ReactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verso/reactions")
@RequiredArgsConstructor
@Tag(name = "Reactions", description = "Endpoints para gerenciamento de reações em artigos")
public class ReactionController {

    private final ReactionService reactionService;

    @Operation(summary = "Adicionar ou atualizar reação em um artigo", description = "Adiciona uma nova reação ou atualiza a reação existente do usuário autenticado em um artigo publicado. Se o usuário já reagiu, a reação será atualizada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reação adicionada/atualizada com sucesso", content = @Content(schema = @Schema(implementation = ReactionResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Apenas artigos publicados podem receber reações"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/articles/{articleId}")
    public ResponseEntity<ReactionResponseDTO> addOrUpdateReaction(
            @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable Long articleId,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateReactionRequestDTO dto) {
        var response = reactionService.addOrUpdateReaction(articleId, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remover reação de um artigo", description = "Remove a reação do usuário autenticado de um artigo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reação removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Reação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @DeleteMapping("/articles/{articleId}")
    public ResponseEntity<Void> removeReaction(
            @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable Long articleId) {
        reactionService.removeReaction(articleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar reações de um artigo", description = "Retorna uma lista paginada de todas as reações de um artigo específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reações retornada com sucesso", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/articles/{articleId}")
    public ResponseEntity<Page<ReactionResponseDTO>> findAllReactionsByArticle(
            @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable Long articleId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var pageResponse = reactionService.findAllReactionsByArticle(articleId, pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Listar reações do usuário autenticado", description = "Retorna uma lista paginada de todas as reações feitas pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reações retornada com sucesso", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/my-reactions")
    public ResponseEntity<Page<ReactionResponseDTO>> findAllReactionsByUser(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var pageResponse = reactionService.findAllReactionsByUser(pageable);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Obter estatísticas de reações de um artigo", description = "Retorna estatísticas detalhadas das reações de um artigo, incluindo contagem por tipo e a reação do usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso", content = @Content(schema = @Schema(implementation = ArticleReactionStatsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/articles/{articleId}/stats")
    public ResponseEntity<ArticleReactionStatsDTO> getArticleReactionStats(
            @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable Long articleId) {
        var stats = reactionService.getArticleReactionStats(articleId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Verificar reação do usuário em um artigo", description = "Retorna o tipo de reação do usuário autenticado em um artigo específico, ou null se não reagiu.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reação do usuário retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/articles/{articleId}/my-reaction")
    public ResponseEntity<ReactionType> getUserReaction(
            @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable Long articleId) {
        ReactionType reaction = reactionService.getUserReaction(articleId);
        return ResponseEntity.ok(reaction);
    }
}
