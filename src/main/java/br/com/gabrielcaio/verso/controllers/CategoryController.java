package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CategoryDTO;
import br.com.gabrielcaio.verso.dtos.CreateCategoryRequestDTO;
import br.com.gabrielcaio.verso.dtos.UpdateCategoryRequestDTO;
import br.com.gabrielcaio.verso.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/verso/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Endpoints para gerenciamento de categorias (apenas ADMIN)")
public class CategoryController {

    private final CategoryService service;

    @Operation(
            summary = "Listar todas as categorias",
            description = "Retorna uma lista de todas as categorias cadastradas no sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> findAll(
        @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(service.listAll(pageable));
    }

    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna uma categoria específica baseada no ID fornecido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(
            @Parameter(description = "ID da categoria", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(
            summary = "Criar nova categoria",
            description = "Cria uma nova categoria. Apenas usuários com perfil ADMIN podem criar categorias."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode criar categorias"),
            @ApiResponse(responseCode = "422", description = "Erro de validação - nome de categoria já existe"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDTO> create(
            @RequestBody(
                    description = "Dados da categoria a ser criada",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateCategoryRequestDTO.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateCategoryRequestDTO request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza uma categoria existente. Apenas usuários com perfil ADMIN podem atualizar categorias."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode atualizar categorias"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "422", description = "Erro de validação - nome de categoria já existe"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(
            @Parameter(description = "ID da categoria", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody(
                    description = "Dados atualizados da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateCategoryRequestDTO.class))
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateCategoryRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(
            summary = "Deletar categoria",
            description = "Exclui uma categoria. Ao excluir, todos os artigos associados serão movidos para a categoria 'Sem categoria'. Apenas usuários com perfil ADMIN podem excluir categorias."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria deletada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode excluir categorias"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "422", description = "Erro de validação - categoria padrão não pode ser excluída"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria", example = "1", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}