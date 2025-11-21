package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Notification;
import br.com.gabrielcaio.verso.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  Page<Notification> findAllByUserAndReadOrderByCreatedAtDesc(
      User user, boolean read, Pageable pageable);

  long countByUserAndRead(User user, boolean read);

  @Modifying
  @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user AND n.read = false")
  void markAllAsReadByUser(@Param("user") User user);

  @Modifying
  @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.user = :user")
  void markAsRead(@Param("id") Long id, @Param("user") User user);
}
