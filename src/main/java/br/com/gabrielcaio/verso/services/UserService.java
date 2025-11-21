package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.EntityExistsException;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.dtos.UserDTO;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RolesRepository rolesRepository;

  public User getCurrentUser() {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    log.info("[UserService] Buscando usuário autenticado: {}", username);

    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> {
              log.warn("[UserService] Usuário autenticado '{}' não encontrado no banco", username);
              return new EntityNotFoundException("Usuário não encontrado");
            });
  }

  @Transactional
  public void register(UserDTO dto) {
    log.info("[UserService] Iniciando registro de usuário com email: {}", dto.getEmail());

    if (userRepository.existsByEmail(dto.getEmail())) {
      log.warn("[UserService] Falha ao registrar. Email já cadastrado: {}", dto.getEmail());
      throw new EntityExistsException("Email já cadastrado");
    }

    String usernameGerado = obterUserNamePeloEmail(dto.getEmail());
    log.info(
        "[UserService] Username gerado a partir do email '{}': {}", dto.getEmail(), usernameGerado);

    User user = new User();
    user.setUsername(usernameGerado);
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setEmail(dto.getEmail());

    log.info("[UserService] Atribuindo roles ao novo usuário {}", usernameGerado);
    addRoles(user, dto);

    userRepository.save(user);
    log.info(
        "[UserService] Usuário registrado com sucesso: {} (email: {})",
        user.getUsername(),
        user.getEmail());
  }

  @Transactional(readOnly = true)
  public Page<String> findAll(Pageable pageable) {
    log.info("[UserService] Listando usuários. Página: {}", pageable.getPageNumber());
    var page = userRepository.findAll(pageable);

    log.info(
        "[UserService] Total de usuários retornados na página: {}", page.getNumberOfElements());
    return page.map(User::getUsername);
  }

  private void addRoles(User user, UserDTO dto) {
    Set<String> roles = dto.getRoles();

    if (roles == null || roles.isEmpty()) {
      log.warn("[UserService] Nenhuma role enviada para o usuário {}", user.getUsername());
    }

    roles.stream()
        .map(String::toUpperCase)
        .forEach(
            roleName -> {
              log.info(
                  "[UserService] Atribuindo role '{}' ao usuário {}", roleName, user.getUsername());

              Roles role =
                  rolesRepository
                      .findByName(roleName)
                      .orElseThrow(
                          () -> {
                            log.error(
                                "[UserService] Role '{}' não encontrada ao registrar usuário {}",
                                roleName,
                                user.getUsername());
                            return new IllegalArgumentException("Role não encontrada: " + roleName);
                          });

              user.getRoles().add(role);
            });
  }

  private String obterUserNamePeloEmail(String email) {
    log.info("[UserService] Gerando username a partir do email {}", email);

    if (email == null || email.isBlank()) {
      log.error("[UserService] Email inválido (nulo ou vazio) ao gerar username");
      throw new IllegalArgumentException("Email inválido");
    }

    String trimmed = email.trim();
    int atIndex = trimmed.indexOf('@');

    if (atIndex <= 0) {
      log.error("[UserService] Falha ao gerar username. Email inválido: {}", email);
      throw new IllegalArgumentException("Email inválido: sem parte local antes de '@'");
    }

    String local = trimmed.substring(0, atIndex).toLowerCase();
    local = local.replaceAll("[^a-z0-9._-]", "");

    if (local.isEmpty()) {
      log.error("[UserService] Username inválido após normalização do email {}", email);
      throw new IllegalArgumentException("Nome de usuário inválido após normalização");
    }

    int MAX_LENGTH = 30;
    String usernameFinal = local.length() > MAX_LENGTH ? local.substring(0, MAX_LENGTH) : local;

    log.info("[UserService] Username final gerado: {}", usernameFinal);
    return usernameFinal;
  }

  public Optional<User> findUsersByRole(Roles role) {
    log.info("[UserService] Buscando usuários com a role '{}'", role.getName());
    return Optional.empty(); // implementar se necessário
  }
}
