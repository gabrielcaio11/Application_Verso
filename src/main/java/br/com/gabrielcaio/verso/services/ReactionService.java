package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Reaction;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import br.com.gabrielcaio.verso.dtos.ArticleReactionStatsDTO;
import br.com.gabrielcaio.verso.dtos.CreateReactionRequestDTO;
import br.com.gabrielcaio.verso.dtos.ReactionResponseDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;

    @Transactional
    public ReactionResponseDTO addOrUpdateReaction(Long articleId, CreateReactionRequestDTO dto) {
        var currentUser = userService.getCurrentUser();
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        // Validações
        if (article.getStatus() != ArticleStatus.PUBLICADO) {
            throw new BusinessException("Apenas artigos publicados podem receber reações");
        }

        // Verificar se já existe reação do usuário para este artigo
        var existingReaction = reactionRepository.findByUserAndArticle(currentUser, article);

        Reaction reaction;
        if (existingReaction.isPresent()) {
            // Atualizar reação existente
            reaction = existingReaction.get();
            ReactionType oldType = reaction.getType();
            reaction.setType(dto.getType());
            reaction = reactionRepository.save(reaction);

            // Atualizar contador de likes se necessário
            updateLikesCount(article, oldType, dto.getType());
        } else {
            // Criar nova reação
            reaction = new Reaction();
            reaction.setUser(currentUser);
            reaction.setArticle(article);
            reaction.setType(dto.getType());
            reaction = reactionRepository.save(reaction);

            // Atualizar contador de likes
            if (dto.getType() == ReactionType.LIKE) {
                article.setLikesCount(article.getLikesCount() + 1);
                articleRepository.save(article);
            }
        }

        return toDto(reaction);
    }

    @Transactional
    public void removeReaction(Long articleId) {
        var currentUser = userService.getCurrentUser();
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        var reaction = reactionRepository.findByUserAndArticle(currentUser, article)
                .orElseThrow(() -> new ResourceNotFoundException("Reação não encontrada"));

        // Atualizar contador de likes se necessário
        if (reaction.getType() == ReactionType.LIKE) {
            article.setLikesCount(Math.max(0, article.getLikesCount() - 1));
            articleRepository.save(article);
        }

        reactionRepository.delete(reaction);
    }

    @Transactional(readOnly = true)
    public Page<ReactionResponseDTO> findAllReactionsByArticle(Long articleId, Pageable pageable) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        var reactionsPage = reactionRepository.findAllByArticle(article, pageable);
        return reactionsPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReactionResponseDTO> findAllReactionsByUser(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var reactionsPage = reactionRepository.findAllByUser(currentUser, pageable);
        return reactionsPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ArticleReactionStatsDTO getArticleReactionStats(Long articleId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        var currentUser = userService.getCurrentUser();
        var userReaction = reactionRepository.findByUserAndArticle(currentUser, article);

        Map<String, Long> reactionsByType = new HashMap<>();
        long totalReactions = 0;

        for (ReactionType type : ReactionType.values()) {
            Long count = reactionRepository.countByArticleAndType(article, type);
            if (count > 0) {
                reactionsByType.put(type.name(), count);
                totalReactions += count;
            }
        }

        return new ArticleReactionStatsDTO(
                article.getId(),
                article.getTitle(),
                totalReactions,
                reactionsByType,
                userReaction.map(r -> r.getType().name()).orElse(null));
    }

    @Transactional(readOnly = true)
    public ReactionType getUserReaction(Long articleId) {
        var currentUser = userService.getCurrentUser();
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        return reactionRepository.findByUserAndArticle(currentUser, article)
                .map(Reaction::getType)
                .orElse(null);
    }

    private void updateLikesCount(Article article, ReactionType oldType, ReactionType newType) {
        boolean wasLike = oldType == ReactionType.LIKE;
        boolean isLike = newType == ReactionType.LIKE;

        if (wasLike && !isLike) {
            // Removeu like
            article.setLikesCount(Math.max(0, article.getLikesCount() - 1));
            articleRepository.save(article);
        } else if (!wasLike && isLike) {
            // Adicionou like
            article.setLikesCount(article.getLikesCount() + 1);
            articleRepository.save(article);
        }
    }

    private ReactionResponseDTO toDto(Reaction reaction) {
        var article = reaction.getArticle();
        var user = reaction.getUser();

        return new ReactionResponseDTO(
                reaction.getId(),
                reaction.getType(),
                article.getId(),
                article.getTitle(),
                user.getId(),
                user.getUsername(),
                reaction.getCreatedAt());
    }
}
