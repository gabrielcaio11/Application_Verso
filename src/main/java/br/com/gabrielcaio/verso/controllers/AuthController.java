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
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para autenticação e registro")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Login de usuário")
    @GetMapping("/login")
    public ResponseEntity<String> login() {
        var message = "Sucesso ao realizar login";
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Registro de novo usuário")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserDTO dto) {
        userService.register(dto);
        var message = "Sucesso ao registrar usuario";
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Buscar todos os usuários")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> findAll() {
        var users = userService.findAll();
        return ResponseEntity.ok(users);
    }
}
