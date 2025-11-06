package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Favorite;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.domain.enums.ArticleStatus;
import br.com.gabrielcaio.verso.dtos.FavoriteResponseDTO;
import br.com.gabrielcaio.verso.repositories.ArticleRepository;
import br.com.gabrielcaio.verso.repositories.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final FavoriteRepository favoriteRepository;
    private final ArticleRepository articleRepository;
    private final UserService userService;

    @Transactional
    public FavoriteResponseDTO addFavorite(Long articleId) {
        var currentUser = userService.getCurrentUser();
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Artigo não encontrado"));

        // Validações
        if (article.getStatus() != ArticleStatus.PUBLICADO) {
            throw new BusinessException("Apenas artigos publicados podem ser favoritados");
        }

        if (favoriteRepository.existsByUserAndArticleId(currentUser, articleId)) {
            throw new BusinessException("Este artigo já está nos seus favoritos");
        }

        // Criar favorito
        var favorite = new Favorite();
        favorite.setUser(currentUser);
        favorite.setArticle(article);
        favorite = favoriteRepository.save(favorite);

        return toDto(favorite);
    }

    @Transactional
    public void removeFavorite(Long articleId) {
        var currentUser = userService.getCurrentUser();
        
        if (!favoriteRepository.existsByUserAndArticleId(currentUser, articleId)) {
            throw new ResourceNotFoundException("Artigo não encontrado nos seus favoritos");
        }

        favoriteRepository.deleteByUserAndArticleId(currentUser, articleId);
    }

    @Transactional(readOnly = true)
    public Page<FavoriteResponseDTO> findAllFavorites(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var favoritesPage = favoriteRepository.findAllByUserAndArticleStatus(
                currentUser, 
                ArticleStatus.PUBLICADO, 
                pageable
        );
        
        return favoritesPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long articleId) {
        var currentUser = userService.getCurrentUser();
        return favoriteRepository.existsByUserAndArticleId(currentUser, articleId);
    }

    private FavoriteResponseDTO toDto(Favorite favorite) {
        var article = favorite.getArticle();
        return new FavoriteResponseDTO(
                favorite.getId(),
                article.getId(),
                article.getTitle(),
                article.getCategory().getName(),
                favorite.getCreatedAt()
        );
    }
}