package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.EntityExistsException;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.dtos.UserDTO;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolesRepository rolesRepository;

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    @Transactional
    public void register(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityExistsException("Email já cadastrado");

        }
        User user = new User();
        user.setUsername(obterUserNamePeloEmail(dto.getEmail()));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        addRoles(user, dto);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<String> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(User::getUsername);
    }

    private void addRoles(User user, UserDTO dto) {
        Set<String> roles = dto.getRoles();
        roles.stream()
                .map(String::toUpperCase)
                .forEach(roleName -> {
                            Roles role = rolesRepository.findByName(roleName)
                                    .orElseThrow(() -> new IllegalArgumentException("Role não encontrada: " + roleName));
                            user.getRoles().add(role);
                        }
                );
    }

    private String obterUserNamePeloEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email inválido");
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0) {
            throw new IllegalArgumentException("Email inválido: sem parte local antes de '@'");
        }
        String local = trimmed.substring(0, atIndex).toLowerCase();
        // manter apenas caracteres permitidos em username
        local = local.replaceAll("[^a-z0-9._-]", "");
        if (local.isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário inválido após normalização");
        }
        // limitar tamanho do username
        int MAX_LENGTH = 30;
        return local.length() > MAX_LENGTH ? local.substring(0, MAX_LENGTH) : local;
    }

    public Optional<User> findUsersByRole(Roles role) {
        return null;
    }
}
