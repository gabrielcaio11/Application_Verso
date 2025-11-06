package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CommentResponseDTO;
import br.com.gabrielcaio.verso.dtos.CreateCommentRequestDTO;
import br.com.gabrielcaio.verso.dtos.ThreadedCommentDTO;
import br.com.gabrielcaio.verso.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verso")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Endpoints para comentários em artigos")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Comentar artigo", description = "Cria um novo comentário em um artigo (use parentId para responder)")
    @ApiResponse(responseCode = "200", description = "Comentário criado",
            content = @Content(schema = @Schema(implementation = CommentResponseDTO.class)))
    @PostMapping("/article/{articleId}/comments")
    public ResponseEntity<CommentResponseDTO> create(
            @PathVariable Long articleId,
            @Valid @RequestBody CreateCommentRequestDTO dto
    ) {
        return ResponseEntity.ok(commentService.create(articleId, dto));
    }

    @Operation(summary = "Listar comentários do artigo (flat)", description = "Lista todos os comentários em ordem cronológica")
    @GetMapping("/article/{articleId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> listFlat(
            @PathVariable Long articleId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.listFlatByArticle(articleId, pageable));
    }

    @Operation(summary = "Listar comentários do artigo (threaded)", description = "Lista comentários raiz paginados, com respostas aninhadas")
    @GetMapping("/article/{articleId}/comments/threaded")
    public ResponseEntity<Page<ThreadedCommentDTO>> listThreaded(
            @PathVariable Long articleId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(commentService.listThreadedByArticle(articleId, pageable));
    }

    @Operation(summary = "Excluir comentário", description = "Autor do comentário ou autor do artigo podem remover")
    @ApiResponse(responseCode = "204", description = "Comentário removido")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}