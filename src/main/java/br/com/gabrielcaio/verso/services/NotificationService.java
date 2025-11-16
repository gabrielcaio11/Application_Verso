package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Article;
import br.com.gabrielcaio.verso.domain.entity.Comment;
import br.com.gabrielcaio.verso.domain.entity.Notification;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.dtos.NotificationResponseDTO;
import br.com.gabrielcaio.verso.repositories.FollowRepository;
import br.com.gabrielcaio.verso.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public void createNotificationForFollowers(Article article) {
        var author = article.getAuthor();

        log.info("Criando notificações para seguidores. Autor id={} username={}",
                author.getId(), author.getUsername());

        List<User> followers = followRepository
                .findFollowersByFollowing(author, Pageable.unpaged()).getContent();

        log.debug("Total de seguidores encontrados: {}", followers.size());

        String message = String.format(
                "%s publicou um novo artigo: %s",
                author.getUsername(),
                article.getTitle()
        );

        for (User follower : followers) {
            log.debug("Criando notificação para seguidor id={} username={}",
                    follower.getId(), follower.getUsername());

            Notification notification = new Notification();
            notification.setUser(follower);
            notification.setArticle(article);
            notification.setMessage(message);
            notification.setRead(false);

            notificationRepository.save(notification);
        }

        log.info("Notificações criadas com sucesso para artigo id={}", article.getId());
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotifications(Pageable pageable) {
        var currentUser = userService.getCurrentUser();

        log.info("Buscando todas notificações do usuário id={} username={} page={} size={}",
                currentUser.getId(), currentUser.getUsername(), pageable.getPageNumber(), pageable.getPageSize());

        var notificationsPage =
                notificationRepository.findAllByUserOrderByCreatedAtDesc(currentUser, pageable);

        log.info("Total de notificações encontradas: {}", notificationsPage.getTotalElements());

        return notificationsPage.map(this::toDto);
    }


    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getUnreadNotifications(Pageable pageable) {
        var currentUser = userService.getCurrentUser();

        log.info("Buscando notificações NÃO LIDAS do usuário id={} username={}",
                currentUser.getId(), currentUser.getUsername());

        var notificationsPage =
                notificationRepository.findAllByUserAndReadOrderByCreatedAtDesc(currentUser, false, pageable);

        log.info("Total de notificações não lidas encontradas: {}",
                notificationsPage.getTotalElements());

        return notificationsPage.map(this::toDto);
    }


    @Transactional
    public void markAsRead(Long notificationId) {
        var currentUser = userService.getCurrentUser();

        log.info("Marcando notificação id={} como lida para usuário id={}",
                notificationId, currentUser.getId());

        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    log.warn("Notificação id={} não encontrada", notificationId);
                    return new ResourceNotFoundException("Notificação não encontrada");
                });

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            log.warn("Usuário id={} tentou acessar notificação que não pertence a ele. Notificação id={}",
                    currentUser.getId(), notificationId);
            throw new ResourceNotFoundException("Notificação não encontrada");
        }

        notificationRepository.markAsRead(notificationId, currentUser);

        log.info("Notificação id={} marcada como lida com sucesso", notificationId);
    }

    @Transactional
    public void markAllAsRead() {
        var currentUser = userService.getCurrentUser();

        log.info("Marcando todas as notificações como lidas para usuário id={}", currentUser.getId());

        notificationRepository.markAllAsReadByUser(currentUser);

        log.info("Todas as notificações do usuário id={} foram marcadas como lidas", currentUser.getId());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        var currentUser = userService.getCurrentUser();

        log.info("Contando notificações não lidas do usuário id={}", currentUser.getId());

        long count = notificationRepository.countByUserAndRead(currentUser, false);

        log.info("Total de notificações não lidas: {}", count);

        return count;
    }

    private NotificationResponseDTO toDto(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getMessage(),
                notification.getArticle().getId(),
                notification.getArticle().getTitle(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }


    @Transactional
    public void createNotificationForArticleComment(Article article, Comment comment) {
        log.info("Criando notificação de comentário para autor do artigo id={}", article.getId());

        if (article.getAuthor() != null
                && !article.getAuthor().getId().equals(comment.getAuthor().getId())) {

            log.debug("Comentário feito por usuário id={} será notificado ao autor id={}",
                    comment.getAuthor().getId(), article.getAuthor().getId());

            var notification = new Notification();
            notification.setUser(article.getAuthor());
            notification.setArticle(article);
            notification.setMessage("Novo comentário no seu artigo \"" + article.getTitle() + "\"");
            notification.setRead(false);

            notificationRepository.save(notification);

            log.info("Notificação criada para autor id={} (comentário recebido)",
                    article.getAuthor().getId());
        }
    }

    @Transactional
    public void createNotificationForCommentReply(Comment parent, Comment reply) {
        log.info("Criando notificação de resposta a comentário id={}", parent.getId());

        if (parent.getAuthor() != null
                && !parent.getAuthor().getId().equals(reply.getAuthor().getId())) {

            log.debug("Resposta feita por usuário id={} será notificada ao autor do comentário id={}",
                    reply.getAuthor().getId(), parent.getAuthor().getId());

            var notification = new Notification();
            notification.setUser(parent.getAuthor());
            notification.setArticle(reply.getArticle());
            notification.setMessage("Nova resposta ao seu comentário");
            notification.setRead(false);

            notificationRepository.save(notification);

            log.info("Notificação criada para usuário id={} (resposta recebida)",
                    parent.getAuthor().getId());
        }
    }
}
