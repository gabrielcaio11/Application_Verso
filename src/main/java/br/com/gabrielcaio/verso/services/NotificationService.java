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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public void createNotificationForFollowers(Article article) {
        User author = article.getAuthor();
        List<User> followers = followRepository.findFollowersByFollowing(author, Pageable.unpaged()).getContent();
        
        String message = String.format("%s publicou um novo artigo: %s", 
                author.getUsername(), article.getTitle());
        
        for (User follower : followers) {
            Notification notification = new Notification();
            notification.setUser(follower);
            notification.setArticle(article);
            notification.setMessage(message);
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotifications(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var notificationsPage = notificationRepository.findAllByUserOrderByCreatedAtDesc(currentUser, pageable);
        
        return notificationsPage.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getUnreadNotifications(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var notificationsPage = notificationRepository.findAllByUserAndReadOrderByCreatedAtDesc(
                currentUser, false, pageable);
        
        return notificationsPage.map(this::toDto);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        var currentUser = userService.getCurrentUser();
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notificação não encontrada");
        }
        
        notificationRepository.markAsRead(notificationId, currentUser);
    }

    @Transactional
    public void markAllAsRead() {
        var currentUser = userService.getCurrentUser();
        notificationRepository.markAllAsReadByUser(currentUser);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        var currentUser = userService.getCurrentUser();
        return notificationRepository.countByUserAndRead(currentUser, false);
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
        if (article.getAuthor() != null && !article.getAuthor().getId().equals(comment.getAuthor().getId())) {
            var notification = new Notification();
            notification.setUser(article.getAuthor());
            notification.setArticle(article);
            notification.setMessage("Novo comentário no seu artigo \"" + article.getTitle() + "\"");
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void createNotificationForCommentReply(Comment parent, Comment reply) {
        if (parent.getAuthor() != null && !parent.getAuthor().getId().equals(reply.getAuthor().getId())) {
            var notification = new Notification();
            notification.setUser(parent.getAuthor());
            notification.setArticle(reply.getArticle());
            notification.setMessage("Nova resposta ao seu comentário");
            notification.setRead(false);
            notificationRepository.save(notification);
        }
    }
}