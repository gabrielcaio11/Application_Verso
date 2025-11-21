package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CommentResponseDTO;
import br.com.gabrielcaio.verso.dtos.CreateCommentRequestDTO;
import br.com.gabrielcaio.verso.dtos.ThreadedCommentDTO;
import br.com.gabrielcaio.verso.services.CommentService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verso")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoints para comentários em artigos")
@Slf4j
public class CommentController {

  private final CommentService commentService;

  @Operation(
      summary = "Comentar artigo",
      description = "Cria um novo comentário em um artigo (use parentId para responder)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Comentário criado com sucesso",
        content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))),
    @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
    @ApiResponse(responseCode = "401", description = "Não autorizado - autenticação necessária"),
    @ApiResponse(responseCode = "422", description = "Erro de validação"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @PostMapping("/article/{articleId}/comments")
  public ResponseEntity<CommentResponseDTO> create(
      @PathVariable Long articleId, @Valid @RequestBody CreateCommentRequestDTO dto) {
    log.info(
        "Recebida requisição para criar comentario no artigo com id {} e com conteudo: {}",
        articleId,
        dto.getContent());
    var entity = commentService.create(articleId, dto);
    log.info("Comentario criado com sucesso.");
    return ResponseEntity.status(HttpStatus.CREATED).body(entity);
  }

  @Operation(
      summary = "Listar comentários do artigo (flat)",
      description = "Lista todos os comentários em ordem cronológica")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Lista de comentarios retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @Parameters({
    @Parameter(
        in = ParameterIn.QUERY,
        name = "page",
        description = "Número da página (inicia em 0). Padrão: 0",
        example = "0",
        schema = @Schema(type = "integer", defaultValue = "0")),
    @Parameter(
        in = ParameterIn.QUERY,
        name = "size",
        description = "Quantidade de itens por página. Padrão: 10",
        example = "10",
        schema = @Schema(type = "integer", defaultValue = "10")),
    @Parameter(
        in = ParameterIn.QUERY,
        name = "sort",
        description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
        examples = {
          @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
          @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
        },
        schema = @Schema(type = "string", defaultValue = "createdAt,DESC"))
  })
  @GetMapping("/article/{articleId}/comments")
  public ResponseEntity<Page<CommentResponseDTO>> listFlat(
      @PathVariable Long articleId, @ParameterObject Pageable pageable) {
    log.info(
        "Buscando comentarios do artigo: {}. Página: {}, Tamanho: {}",
        articleId,
        pageable.getPageNumber(),
        pageable.getPageSize());
    var pageResponse = commentService.listFlatByArticle(articleId, pageable);
    log.info("Total de comentarios encontrados: {}", pageResponse.getTotalElements());
    return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
  }

  @Operation(
      summary = "Listar comentários do artigo (threaded)",
      description = "Lista comentários raiz paginados, com respostas aninhadas")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Lista de comentarios retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @Parameters({
    @Parameter(
        in = ParameterIn.QUERY,
        name = "page",
        description = "Número da página (inicia em 0). Padrão: 0",
        example = "0",
        schema = @Schema(type = "integer", defaultValue = "0")),
    @Parameter(
        in = ParameterIn.QUERY,
        name = "size",
        description = "Quantidade de itens por página. Padrão: 10",
        example = "10",
        schema = @Schema(type = "integer", defaultValue = "10")),
    @Parameter(
        in = ParameterIn.QUERY,
        name = "sort",
        description = "Ordenação no formato: campo,(asc|desc). Padrão: createdAt,desc",
        examples = {
          @ExampleObject(name = "Ordenação por Data de criação", value = "createdAt,DESC"),
          @ExampleObject(name = "Ordenação por ultima atualização", value = "updatedAt,DESC")
        },
        schema = @Schema(type = "string", defaultValue = "createdAt,DESC"))
  })
  @GetMapping("/article/{id}/comments/threaded")
  public ResponseEntity<Page<ThreadedCommentDTO>> listThreaded(
      @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable
          Long id,
      @ParameterObject Pageable pageable) {
    log.info(
        "Buscando rascunhos do usuário autenticado. Página: {}, Tamanho: {}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    var pageResponse = commentService.listThreadedByArticle(id, pageable);
    log.info("Total de comentarios encontrados threaded: {}", pageResponse.getTotalElements());
    return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
  }

  @Operation(
      summary = "Excluir comentário",
      description = "Autor do comentário ou autor do artigo podem remover")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Comentario deletado com sucesso"),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(
        responseCode = "403",
        description =
            "Acesso negado - você só pode excluir seus próprios comentários ou os do seu artigo"),
    @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "ID do comentario", example = "1", required = true) @PathVariable
          Long id) {
    log.info("Requisição para deletar comentario ID: {}", id);
    commentService.delete(id);
    log.info("Comentario ID: {} deletado com sucesso", id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
