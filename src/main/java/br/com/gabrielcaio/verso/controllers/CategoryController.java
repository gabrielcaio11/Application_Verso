package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.CategoryDTO;
import br.com.gabrielcaio.verso.dtos.CreateCategoryRequestDTO;
import br.com.gabrielcaio.verso.dtos.UpdateCategoryRequestDTO;
import br.com.gabrielcaio.verso.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/verso/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Endpoints para gerenciamento de categorias")
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Listar todas as categorias")
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @Operation(summary = "Buscar categoria por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Criar nova categoria")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CreateCategoryRequestDTO request) {
        return ResponseEntity.ok(service.create(request));
    }

    @Operation(summary = "Atualizar categoria")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Deletar categoria")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
