package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CategoryResponseWithNameDTO;
import br.com.gabrielcaio.verso.dtos.CreateCategoryRequestDTO;
import br.com.gabrielcaio.verso.dtos.UpdateCategoryRequestDTO;
import br.com.gabrielcaio.verso.services.CategoryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/verso/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints para gerenciamento de categorias (apenas ADMIN)")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Listar todas as categorias",
            description = "Retorna uma lista de todas as categorias cadastradas no sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorias retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponseWithNameDTO.class))
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
    @GetMapping
    public ResponseEntity<Page<CategoryResponseWithNameDTO>> findAll(
            @ParameterObject
            Pageable pageable
    ) {
        log.info("Buscando todas as categorias. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());
        var pageResponse = categoryService.findAll(pageable);
        log.info("Total de categorias encontradas: {}", pageResponse.getTotalElements());
        return ResponseEntity.status(HttpStatus.OK).body(pageResponse);
    }

    @Operation(
            summary = "Buscar categoria por ID",
            description = "Retorna uma categoria específica baseada no ID fornecido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponseWithNameDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseWithNameDTO> findById(
            @Parameter(
                    description = "ID da categoria",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        log.info("Buscando categoria por ID: {}", id);
        var response = categoryService.findById(id);
        log.info("Artigo encontrado: {}", response.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Criar nova categoria",
            description = "Cria uma nova categoria. Apenas usuários com perfil ADMIN podem criar categorias."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponseWithNameDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN pode criar categorias"),
            @ApiResponse(responseCode = "422", description = "Erro de validação - nome de categoria já existe"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponseWithNameDTO> create(
            @Valid @RequestBody CreateCategoryRequestDTO dto
    ) {
        log.info("Recebida requisição para criar categoria com nome: {}", dto.getName());
        var response = categoryService.create(dto);
        log.info("Artigo criado com sucesso.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Atualizar categoria",
            description = "Atualiza uma categoria existente. Apenas usuários com perfil ADMIN podem atualizar categorias."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoria atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryResponseWithNameDTO.class))
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
    public ResponseEntity<CategoryResponseWithNameDTO> update(
            @Parameter(
                    description = "ID da categoria",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDTO dto
    ) {
        log.info("Atualizando categoria ID: {} com novos dados", id);
        var response = categoryService.update(id, dto);
        log.info("Artigo ID: {} atualizado com sucesso", id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
            @Parameter(
                    description = "ID da categoria",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        log.info("Requisição para deletar categoria ID: {}", id);
        categoryService.delete(id);
        log.info("Categoria ID: {} deletado com sucesso", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}