package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long>
{
    Optional<Category> findByName(String category);
}