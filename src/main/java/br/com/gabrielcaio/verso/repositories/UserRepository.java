package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);
}
