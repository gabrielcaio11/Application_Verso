package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Reaction;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.domain.enums.ReactionType;
import br.com.gabrielcaio.verso.dtos.ArticleReactionStatsDTO;
import br.com.gabrielcaio.verso.dtos.CreateReactionRequestDTO;
import br.com.gabrielcaio.verso.dtos.ReactionResponseDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.ReactionRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionService
{

    private final ReactionRepository reactionRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;

    @Transactional
    public ReactionResponseDTO addOrUpdateReaction(Long articleId, CreateReactionRequestDTO dto)
    {
        var currentUser = userService.getCurrentUser();

        log.info("[REACTION] Iniciando add/update reação. userId={}, articleId={}, newType={}",
                currentUser.getId(), articleId, dto.getType()
        );

        var article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                {
                    log.warn("[REACTION] Artigo não encontrado. articleId={}", articleId);
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        if(article.getStatus() != ArticleStatus.PUBLICADO)
        {
            log.warn(
                    "[REACTION] Tentativa de reagir a artigo não publicado. articleId={}, status={}",
                    articleId, article.getStatus()
            );
            throw new BusinessException("Apenas artigos publicados podem receber reações");
        }

        var existingReaction = reactionRepository.findByUserAndArticle(currentUser, article);
        Reaction reaction;

        if(existingReaction.isPresent())
        {
            reaction = existingReaction.get();

            log.info(
                    "[REACTION] Atualizando reação existente. reactionId={}, userId={}, oldType={}, newType={}",
                    reaction.getId(), currentUser.getId(), reaction.getType(), dto.getType()
            );

            ReactionType oldType = reaction.getType();
            reaction.setType(dto.getType());
            reaction = reactionRepository.save(reaction);

            updateLikesCount(article, oldType, dto.getType());
        } else
        {
            log.info("[REACTION] Criando nova reação. userId={}, articleId={}, type={}",
                    currentUser.getId(), articleId, dto.getType()
            );

            reaction = new Reaction();
            reaction.setUser(currentUser);
            reaction.setArticle(article);
            reaction.setType(dto.getType());
            reaction = reactionRepository.save(reaction);

            if(dto.getType() == ReactionType.LIKE)
            {
                article.setLikesCount(article.getLikesCount() + 1);
                articleRepository.save(article);
            }
        }

        log.info("[REACTION] Reação registrada com sucesso. reactionId={}, articleId={}, userId={}",
                reaction.getId(), articleId, currentUser.getId()
        );

        return toDto(reaction);
    }

    @Transactional
    public void removeReaction(Long articleId)
    {
        var currentUser = userService.getCurrentUser();

        log.info("[REACTION] Removendo reação. userId={}, articleId={}",
                currentUser.getId(), articleId
        );

        var article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[REACTION] Artigo não encontrado na remoção. articleId={}", articleId);
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        var reaction = reactionRepository.findByUserAndArticle(currentUser, article)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[REACTION] Nenhuma reação encontrada para remoção. userId={}, articleId={}",
                            currentUser.getId(), articleId
                    );
                    return new ResourceNotFoundException("Reação não encontrada");
                });

        if(reaction.getType() == ReactionType.LIKE)
        {
            log.debug("[REACTION] Removendo like. articleId={}, oldLikes={}",
                    articleId, article.getLikesCount()
            );
            article.setLikesCount(Math.max(0, article.getLikesCount() - 1));
            articleRepository.save(article);
        }

        reactionRepository.delete(reaction);

        log.info("[REACTION] Reação removida com sucesso. reactionId={}, articleId={}, userId={}",
                reaction.getId(), articleId, currentUser.getId()
        );
    }

    @Transactional(readOnly = true)
    public Page<ReactionResponseDTO> findAllReactionsByArticle(Long articleId, Pageable pageable)
    {
        log.debug("[REACTION] Buscando reações de artigo. articleId={}, page={}",
                articleId, pageable
        );

        var article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[REACTION] Artigo não encontrado na listagem de reações. articleId={}",
                            articleId
                    );
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        var reactionsPage = reactionRepository.findAllByArticle(article, pageable);

        log.info("[REACTION] Reações listadas. articleId={}, total={}",
                articleId, reactionsPage.getTotalElements()
        );

        return reactionsPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReactionResponseDTO> findAllReactionsByUser(Pageable pageable)
    {
        var currentUser = userService.getCurrentUser();

        log.debug("[REACTION] Buscando reações do usuário. userId={}, page={}",
                currentUser.getId(), pageable
        );

        var reactionsPage = reactionRepository.findAllByUser(currentUser, pageable);

        log.info("[REACTION] Reações encontradas para usuário. userId={}, total={}",
                currentUser.getId(), reactionsPage.getTotalElements()
        );

        return reactionsPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ArticleReactionStatsDTO getArticleReactionStats(Long articleId)
    {
        log.debug("[REACTION] Calculando estatísticas de reações. articleId={}", articleId);

        var article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[REACTION] Artigo não encontrado para estatísticas. articleId={}",
                            articleId
                    );
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        var currentUser = userService.getCurrentUser();
        var userReaction = reactionRepository.findByUserAndArticle(currentUser, article);

        Map<String, Long> reactionsByType = new HashMap<>();
        long totalReactions = 0;

        for(ReactionType type : ReactionType.values())
        {
            Long count = reactionRepository.countByArticleAndType(article, type);
            if(count > 0)
            {
                reactionsByType.put(type.name(), count);
                totalReactions += count;
            }
        }

        log.info("[REACTION] Estatísticas geradas. articleId={}, totalReactions={}",
                articleId, totalReactions
        );

        return new ArticleReactionStatsDTO(
                article.getId(),
                article.getTitle(),
                totalReactions,
                reactionsByType,
                userReaction.map(r -> r.getType()
                                .name())
                        .orElse(null)
        );
    }

    @Transactional(readOnly = true)
    public ReactionType getUserReaction(Long articleId)
    {
        var currentUser = userService.getCurrentUser();

        log.debug("[REACTION] Buscando reação do usuário para artigo. userId={}, articleId={}",
                currentUser.getId(), articleId
        );

        var article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[REACTION] Artigo não encontrado ao buscar reação do usuário. articleId={}",
                            articleId
                    );
                    return new ResourceNotFoundException("Artigo não encontrado");
                });

        var reaction = reactionRepository.findByUserAndArticle(currentUser, article)
                .map(Reaction::getType)
                .orElse(null);

        log.info("[REACTION] Reação do usuário encontrada. userId={}, articleId={}, reaction={}",
                currentUser.getId(), articleId, reaction
        );

        return reaction;
    }

    private void updateLikesCount(Article article, ReactionType oldType, ReactionType newType)
    {
        boolean wasLike = oldType == ReactionType.LIKE;
        boolean isLike = newType == ReactionType.LIKE;

        log.debug("[REACTION] Ajustando likes. articleId={}, oldType={}, newType={}",
                article.getId(), oldType, newType
        );

        if(wasLike && !isLike)
        {
            article.setLikesCount(Math.max(0, article.getLikesCount() - 1));
            articleRepository.save(article);
            log.debug("[REACTION] Like removido. articleId={}, newLikes={}",
                    article.getId(), article.getLikesCount()
            );
        } else if(!wasLike && isLike)
        {
            article.setLikesCount(article.getLikesCount() + 1);
            articleRepository.save(article);
            log.debug("[REACTION] Like adicionado. articleId={}, newLikes={}",
                    article.getId(), article.getLikesCount()
            );
        }
    }

    private ReactionResponseDTO toDto(Reaction reaction)
    {
        var article = reaction.getArticle();
        var user = reaction.getUser();

        return new ReactionResponseDTO(
                reaction.getId(),
                reaction.getType(),
                article.getId(),
                article.getTitle(),
                user.getId(),
                user.getUsername(),
                reaction.getCreatedAt()
        );
    }
}
