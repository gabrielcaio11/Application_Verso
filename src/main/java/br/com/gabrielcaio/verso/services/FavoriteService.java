package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Favorite;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.FavoriteResponseDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final ArticleRepository articleRepository;
  private final UserService userService;

  @Transactional
  public FavoriteResponseDTO addFavorite(Long articleId) {
    var currentUser = userService.getCurrentUser();
    log.info(
        "[FAVORITE] Iniciando processo para favoritar artigo. articleId={}, userId={}",
        articleId,
        currentUser.getId());

    var article =
        articleRepository
            .findById(articleId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "[FAVORITE] Artigo não encontrado ao tentar favoritar. articleId={}, userId={}",
                      articleId,
                      currentUser.getId());
                  return new ResourceNotFoundException("Artigo não encontrado");
                });

    if (article.getStatus() != ArticleStatus.PUBLICADO) {
      log.warn(
          "[FAVORITE] Tentativa de favoritar artigo NÃO publicado. articleId={}, status={}, userId={}",
          articleId,
          article.getStatus(),
          currentUser.getId());
      throw new BusinessException("Apenas artigos publicados podem ser favoritados");
    }

    if (favoriteRepository.existsByUserAndArticleId(currentUser, articleId)) {
      log.warn(
          "[FAVORITE] Artigo já estava favoritado anteriormente. articleId={}, userId={}",
          articleId,
          currentUser.getId());
      throw new BusinessException("Este artigo já está nos seus favoritos");
    }

    // Criar favorito
    log.debug(
        "[FAVORITE] Criando novo favorito no banco. articleId={}, userId={}",
        articleId,
        currentUser.getId());

    var favorite = new Favorite();
    favorite.setUser(currentUser);
    favorite.setArticle(article);
    favorite = favoriteRepository.save(favorite);

    log.info(
        "[FAVORITE] Artigo favoritado com sucesso. favoriteId={}, articleId={}, userId={}",
        favorite.getId(),
        articleId,
        currentUser.getId());

    return toDto(favorite);
  }

  @Transactional
  public void removeFavorite(Long articleId) {
    var currentUser = userService.getCurrentUser();
    log.info(
        "[FAVORITE] Iniciando remoção de favorito. articleId={}, userId={}",
        articleId,
        currentUser.getId());

    if (!favoriteRepository.existsByUserAndArticleId(currentUser, articleId)) {
      log.warn(
          "[FAVORITE] Tentativa de remover favorito inexistente. articleId={}, userId={}",
          articleId,
          currentUser.getId());
      throw new ResourceNotFoundException("Artigo não encontrado nos seus favoritos");
    }

    favoriteRepository.deleteByUserAndArticleId(currentUser, articleId);

    log.info(
        "[FAVORITE] Favorito removido com sucesso. articleId={}, userId={}",
        articleId,
        currentUser.getId());
  }

  @Transactional(readOnly = true)
  public Page<FavoriteResponseDTO> findAllFavorites(Pageable pageable) {
    var currentUser = userService.getCurrentUser();
    log.debug("[FAVORITE] Buscando favoritos. userId={}, page={}", currentUser.getId(), pageable);

    var favoritesPage =
        favoriteRepository.findAllByUserAndArticleStatus(
            currentUser, ArticleStatus.PUBLICADO, pageable);

    log.info(
        "[FAVORITE] Favoritos recuperados com sucesso. userId={}, totalElements={}",
        currentUser.getId(),
        favoritesPage.getTotalElements());

    return favoritesPage.map(this::toDto);
  }

  @Transactional(readOnly = true)
  public boolean isFavorite(Long articleId) {
    var currentUser = userService.getCurrentUser();
    boolean result = favoriteRepository.existsByUserAndArticleId(currentUser, articleId);

    log.debug(
        "[FAVORITE] isFavorite verificado. articleId={}, userId={}, result={}",
        articleId,
        currentUser.getId(),
        result);

    return result;
  }

  private FavoriteResponseDTO toDto(Favorite favorite) {
    var article = favorite.getArticle();
    return new FavoriteResponseDTO(
        favorite.getId(),
        article.getId(),
        article.getTitle(),
        article.getCategory().getName(),
        favorite.getCreatedAt());
  }
}
