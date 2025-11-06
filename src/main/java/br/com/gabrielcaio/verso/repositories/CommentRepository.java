package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Comment;
import br.com.gabrielcaio.verso.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleAndParentIsNullOrderByCreatedAtAsc(Article article, Pageable pageable);
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
    Page<Comment> findByArticleOrderByCreatedAtAsc(Article article, Pageable pageable);
    boolean existsByIdAndAuthor(Long id, User author);
    long countByArticle(Article article);
}