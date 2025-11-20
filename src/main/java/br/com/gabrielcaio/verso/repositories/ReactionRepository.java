package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Reaction;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionRepository extends JpaRepository<Reaction, Long>
{

    Optional<Reaction> findByUserAndArticle(User user, Article article);

    boolean existsByUserAndArticle(User user, Article article);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.article = :article AND r.type = :type")
    Long countByArticleAndType(@Param("article") Article article, @Param("type") ReactionType type);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.article = :article")
    Long countByArticle(@Param("article") Article article);

    @Query("SELECT r FROM Reaction r WHERE r.article = :article")
    Page<Reaction> findAllByArticle(@Param("article") Article article, Pageable pageable);

    @Query("SELECT r FROM Reaction r WHERE r.user = :user")
    Page<Reaction> findAllByUser(@Param("user") User user, Pageable pageable);

    void deleteByUserAndArticle(User user, Article article);
}
