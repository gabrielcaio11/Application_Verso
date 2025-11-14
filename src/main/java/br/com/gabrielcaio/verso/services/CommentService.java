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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponseDTO create(Long articleId, CreateCommentRequestDTO dto) {
        var article = getArticle(articleId);
        var author = userService.getCurrentUser();

        Comment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentário pai não encontrado"));
            if (!parent.getArticle().getId().equals(articleId)) {
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

        // Atualiza contador do artigo, se existir o campo
        if (article.getCommentsCount() != null) {
            article.setCommentsCount(article.getCommentsCount() + 1);
            articleRepository.save(article);
        }

        // Notificações
        try {
            if (parent == null) {
                notificationService.createNotificationForArticleComment(article, comment);
            } else if (!parent.getAuthor().getId().equals(author.getId())) {
                notificationService.createNotificationForCommentReply(parent, comment);
            }
        } catch (Exception ignored) {
            // todo: logar erro
        }

        return toFlatDto(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> listFlatByArticle(Long articleId, Pageable pageable) {
        var article = getArticle(articleId);
        return commentRepository.findByArticleOrderByCreatedAtAsc(article, pageable).map(this::toFlatDto);
    }

    @Transactional(readOnly = true)
    public Page<ThreadedCommentDTO> listThreadedByArticle(Long articleId, Pageable pageable) {
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
                                // nível 2; para níveis mais fundos, repetir busca recursiva
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

    @Transactional
    public void delete(Long commentId) {
        var currentUser = userService.getCurrentUser();
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário não encontrado"));

        var isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        var isArticleAuthor = comment.getArticle().getAuthor().getId().equals(currentUser.getId());

        if (!isAuthor && !isArticleAuthor) {
            throw new AccessDeniedException("Você não tem permissão para remover este comentário");
        }

        var article = comment.getArticle();
        commentRepository.delete(comment);

        if (article.getCommentsCount() != null) {
            // Recalcula para manter consistência quando há exclusão em cascata (replies)
            article.setCommentsCount(commentRepository.countByArticle(article));
            articleRepository.save(article);
        }
    }

    private Article getArticle(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));
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