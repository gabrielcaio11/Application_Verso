package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Favorite;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Optional<Favorite> findByUserAndArticleId(User user, Long articleId);

  boolean existsByUserAndArticleId(User user, Long articleId);

  @Query("SELECT f FROM Favorite f WHERE f.user = :user AND f.article.status = :status")
  Page<Favorite> findAllByUserAndArticleStatus(
      @Param("user") User user, @Param("status") ArticleStatus status, Pageable pageable);

  Page<Favorite> findAllByUser(User user, Pageable pageable);

  void deleteByUserAndArticleId(User user, Long articleId);
}
