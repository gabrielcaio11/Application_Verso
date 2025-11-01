package br.com.gabrielcaio.verso.config;

import br.com.gabrielcaio.verso.models.Roles;
import br.com.gabrielcaio.verso.models.User;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDataLoader(
            UserRepository userRepository,
            RolesRepository rolesRepository, PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // deletar todos os usuários e roles existentes (opcional)
         userRepository.deleteAll();
         rolesRepository.deleteAll();

        if (rolesRepository.existsByName("USER")) {
            Roles role_user = new Roles();
            role_user.setName("USER");
            rolesRepository.save(role_user);
        }
        if (rolesRepository.existsByName("ADMIN")) {
            Roles role_admin = new Roles();
            role_admin.setName("ADMIN");
            rolesRepository.save(role_admin);
        }

        if (!userRepository.existsByEmail("admin@gmail.com")) {
            Roles role_admin = rolesRepository.findByName("ADMIN").get();
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.getRoles().add(role_admin);
            userRepository.save(adminUser);
        }
        if (!userRepository.existsByEmail("user@gmail.com")) {
            Roles role_user = rolesRepository.findByName("USER").get();
            User normalUser = new User();
            normalUser.setUsername("user");
            normalUser.setEmail("user@gmail.com");
            normalUser.setPassword(passwordEncoder.encode("user123"));
            normalUser.getRoles().add(role_user);
            userRepository.save(normalUser);
        }
    }
}
