package br.com.gabrielcaio.verso.controllers;

import br.com.gabrielcaio.verso.dtos.UserDTO;
import br.com.gabrielcaio.verso.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/verso/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Registro de novo usuário")
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody UserDTO dto) {
        userService.register(dto);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Buscar todos os usuários")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> findAll() {
        var users = userService.findAll();
        return ResponseEntity.ok(users);
    }
}