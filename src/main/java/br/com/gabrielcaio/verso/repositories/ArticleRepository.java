package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Category;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findAllByStatus(ArticleStatus articleStatus, Pageable pageable);

    Page<Article> findAllByStatusAndAuthor(ArticleStatus articleStatus, User user, Pageable pageable);

    boolean existsByAuthorIdAndTitleIgnoreCase(Long id, String title);

    List<Article> findAllByCategory(Category category);
}