package br.com.gabrielcaio.verso.config;

import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.repositories.CategoryRepository;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RolesRepository rolesRepository;
  private final CategoryRepository categoryRepository;
  private final PasswordEncoder passwordEncoder;

  public TestDataLoader(
      UserRepository userRepository,
      RolesRepository rolesRepository,
      CategoryRepository categoryRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.rolesRepository = rolesRepository;
    this.categoryRepository = categoryRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    // Limpar dados existentes para garantir estado consistente
    categoryRepository.deleteAll();
    userRepository.deleteAll();
    rolesRepository.deleteAll();

    // Criar ROLE USER se não existir
    Roles roleUser =
        rolesRepository
            .findByName("USER")
            .orElseGet(
                () -> {
                  Roles role = new Roles();
                  role.setName("USER");
                  return rolesRepository.save(role);
                });

    // Criar ROLE ADMIN se não existir
    Roles roleAdmin =
        rolesRepository
            .findByName("ADMIN")
            .orElseGet(
                () -> {
                  Roles role = new Roles();
                  role.setName("ADMIN");
                  return rolesRepository.save(role);
                });

    // Criar usuário admin
    User adminUser = new User();
    adminUser.setUsername("admin_test");
    adminUser.setEmail("admin@test.com");
    adminUser.setPassword(passwordEncoder.encode("123456"));
    adminUser.setRoles(Set.of(roleAdmin));
    userRepository.save(adminUser);

    // Criar usuário comum
    User normalUser = new User();
    normalUser.setUsername("user_test");
    normalUser.setEmail("user@test.com");
    normalUser.setPassword(passwordEncoder.encode("123456"));
    normalUser.setRoles(Set.of(roleUser));
    userRepository.save(normalUser);

    // Criar categorias padrão
    Category defaultCategory = new Category();
    defaultCategory.setName("Sem categoria");
    categoryRepository.save(defaultCategory);

    Category techCategory = new Category();
    techCategory.setName("Tecnologia");
    categoryRepository.save(techCategory);

    Category educationCategory = new Category();
    educationCategory.setName("Educação");
    categoryRepository.save(educationCategory);
  }
}
