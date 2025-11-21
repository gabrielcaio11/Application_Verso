package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.FavoriteResponseDTO;
import br.com.gabrielcaio.verso.services.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verso/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Endpoints para gerenciamento de favoritos")
@Slf4j
public class FavoriteController {

  private final FavoriteService favoriteService;

  @Operation(
      summary = "Adicionar artigo aos favoritos",
      description =
          "Adiciona um artigo publicado aos favoritos do usuário autenticado. Apenas artigos publicados podem ser favoritados.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Artigo adicionado aos favoritos com sucesso",
        content = @Content(schema = @Schema(implementation = FavoriteResponseDTO.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Artigo não pode ser favoritado (não publicado)"),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "404", description = "Artigo não encontrado"),
    @ApiResponse(responseCode = "422", description = "Artigo já está nos favoritos"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @PostMapping("/{articleId}")
  public ResponseEntity<FavoriteResponseDTO> addFavorite(
      @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable
          Long articleId) {
    log.info("Adicionando artigo ID {} aos favoritos", articleId);
    var response = favoriteService.addFavorite(articleId);
    log.info("Artigo ID {} adicionado aos favoritos com sucesso", articleId);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(
      summary = "Remover artigo dos favoritos",
      description = "Remove um artigo dos favoritos do usuário autenticado.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Artigo removido dos favoritos com sucesso"),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "404", description = "Artigo não encontrado nos favoritos"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @DeleteMapping("/{articleId}")
  public ResponseEntity<Void> removeFavorite(
      @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable
          Long articleId) {
    log.info("Removendo artigo ID {} dos favoritos", articleId);
    favoriteService.removeFavorite(articleId);
    log.info("Artigo ID {} removido dos favoritos com sucesso", articleId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Operation(
      summary = "Listar favoritos do usuário",
      description =
          "Retorna uma lista paginada de artigos favoritados pelo usuário autenticado. Apenas artigos publicados são retornados.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Lista de favoritos retornada com sucesso",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @GetMapping
  public ResponseEntity<Page<FavoriteResponseDTO>> findAllFavorites(
      @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    log.info(
        "Buscando artigos favoritados do usuário autenticado. Página: {}, Tamanho: {}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    var pageResponse = favoriteService.findAllFavorites(pageable);
    log.info("Total de artigos favoritados encontrados: {}", pageResponse.getTotalElements());
    return ResponseEntity.status(HttpStatus.FOUND).body(pageResponse);
  }

  @Operation(
      summary = "Verificar se artigo está nos favoritos",
      description = "Verifica se um artigo específico está nos favoritos do usuário autenticado.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status do favorito retornado com sucesso"),
    @ApiResponse(responseCode = "401", description = "Não autorizado"),
    @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
  })
  @GetMapping("/{articleId}/check")
  public ResponseEntity<Boolean> checkFavorite(
      @Parameter(description = "ID do artigo", example = "1", required = true) @PathVariable
          Long articleId) {
    log.info("Verificando se o artigo ID {} está nos favoritos", articleId);
    boolean isFavorite = favoriteService.isFavorite(articleId);
    log.info("Artigo ID {} está nos favoritos: {}", articleId, isFavorite);
    return ResponseEntity.status(HttpStatus.OK).body(isFavorite);
  }
}
