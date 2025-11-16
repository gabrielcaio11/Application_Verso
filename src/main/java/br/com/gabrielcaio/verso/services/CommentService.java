package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Comment;
import br.com.gabrielcaio.verso.dtos.CommentResponseDTO;
import br.com.gabrielcaio.verso.dtos.CreateCommentRequestDTO;
import br.com.gabrielcaio.verso.dtos.ThreadedCommentDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Transactional
    public CommentResponseDTO create(Long articleId, CreateCommentRequestDTO dto) {
        log.info("[COMMENT CREATE] Criando comentário no artigo {}. Conteúdo recebido: {}",
                articleId, dto.getContent());

        var article = getArticle(articleId);
        var author = userService.getCurrentUser();

        Comment parent = null;

        // Comentário pai
        if (dto.getParentId() != null) {
            log.info("[COMMENT CREATE] Comentário terá parentId: {}", dto.getParentId());

            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> {
                        log.warn("[COMMENT CREATE] Comentário pai {} não encontrado", dto.getParentId());
                        return new ResourceNotFoundException("Comentário pai não encontrado");
                    });

            if (!parent.getArticle().getId().equals(articleId)) {
                log.warn("[COMMENT CREATE] Comentário pai {} pertence a outro artigo", dto.getParentId());
                throw new AccessDeniedException("Comentário pai pertence a outro artigo");
            }
        }

        var comment = Comment.builder()
                .content(dto.getContent().trim())
                .author(author)
                .article(article)
                .parent(parent)
                .build();

        comment = commentRepository.save(comment);
        log.info("[COMMENT CREATE] Comentário {} criado com sucesso", comment.getId());

        // Atualiza contador
        if (article.getCommentsCount() != null) {
            article.setCommentsCount(article.getCommentsCount() + 1);
            articleRepository.save(article);
            log.info("[COMMENT CREATE] Contador de comentários do artigo {} incrementado para {}",
                    article.getId(), article.getCommentsCount());
        }

        // Notificações
        try {
            if (parent == null) {
                log.info("[COMMENT CREATE] Criando notificação para autor do artigo {}", article.getId());
                notificationService.createNotificationForArticleComment(article, comment);
            } else if (!parent.getAuthor().getId().equals(author.getId())) {
                log.info("[COMMENT CREATE] Criando notificação de resposta para autor do comentário {}", parent.getId());
                notificationService.createNotificationForCommentReply(parent, comment);
            }
        } catch (Exception e) {
            log.error("[COMMENT CREATE] Falha ao enviar notificação: {}", e.getMessage());
        }

        return toFlatDto(comment);
    }

    // ---------------------------------------------------------
    // LIST FLAT
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> listFlatByArticle(Long articleId, Pageable pageable) {
        log.info("[COMMENT LIST FLAT] Listando comentários flat do artigo {}. Página: {}",
                articleId, pageable.getPageNumber());

        var article = getArticle(articleId);

        return commentRepository
                .findByArticleOrderByCreatedAtAsc(article, pageable)
                .map(this::toFlatDto);
    }

    // ---------------------------------------------------------
    // LIST THREADED
    // ---------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<ThreadedCommentDTO> listThreadedByArticle(Long articleId, Pageable pageable) {
        log.info("[COMMENT LIST THREAD] Listando comentários estruturados do artigo {}. Página: {}",
                articleId, pageable.getPageNumber());

        var article = getArticle(articleId);

        var roots = commentRepository.findByArticleAndParentIsNullOrderByCreatedAtAsc(article, pageable);

        return roots.map(root -> new ThreadedCommentDTO(
                root.getId(),
                root.getContent(),
                root.getAuthor().getId(),
                root.getAuthor().getUsername(),
                root.getArticle().getId(),
                root.getCreatedAt(),
                commentRepository.findByParentOrderByCreatedAtAsc(root).stream()
                        .map(child -> new ThreadedCommentDTO(
                                child.getId(),
                                child.getContent(),
                                child.getAuthor().getId(),
                                child.getAuthor().getUsername(),
                                child.getArticle().getId(),
                                child.getCreatedAt(),
                                commentRepository.findByParentOrderByCreatedAtAsc(child).stream()
                                        .map(grand -> new ThreadedCommentDTO(
                                                grand.getId(),
                                                grand.getContent(),
                                                grand.getAuthor().getId(),
                                                grand.getAuthor().getUsername(),
                                                grand.getArticle().getId(),
                                                grand.getCreatedAt(),
                                                java.util.List.of()
                                        )).collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList())
        ));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Transactional
    public void delete(Long commentId) {
        log.info("[COMMENT DELETE] Solicitada exclusão do comentário {}", commentId);

        var currentUser = userService.getCurrentUser();

        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("[COMMENT DELETE] Comentário {} não encontrado", commentId);
                    return new ResourceNotFoundException("Comentário não encontrado");
                });

        var isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        var isArticleAuthor = comment.getArticle().getAuthor().getId().equals(currentUser.getId());

        if (!isAuthor && !isArticleAuthor) {
            log.warn("[COMMENT DELETE] Usuário {} tentou excluir comentário {} sem permissão",
                    currentUser.getId(), commentId);
            throw new AccessDeniedException("Você não tem permissão para remover este comentário");
        }

        var article = comment.getArticle();

        commentRepository.delete(comment);
        log.info("[COMMENT DELETE] Comentário {} removido com sucesso", commentId);

        if (article.getCommentsCount() != null) {
            long count = commentRepository.countByArticle(article);
            article.setCommentsCount(count);
            articleRepository.save(article);

            log.info("[COMMENT DELETE] Recalculado contador de comentários do artigo {} para {}",
                    article.getId(), count);
        }
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------
    private Article getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.warn("[COMMENT ARTICLE] Artigo {} não encontrado", articleId);
                    return new ResourceNotFoundException("Artigo não encontrado");
                });
    }

    private CommentResponseDTO toFlatDto(Comment c) {
        return new CommentResponseDTO(
                c.getId(),
                c.getContent(),
                c.getAuthor().getId(),
                c.getAuthor().getUsername(),
                c.getArticle().getId(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getCreatedAt()
        );
    }
}
