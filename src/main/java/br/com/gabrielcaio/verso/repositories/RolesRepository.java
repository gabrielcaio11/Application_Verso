package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Roles;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Long> {
  boolean existsByName(String name);

  Optional<Roles> findByName(String name);
}
