package br.com.gabrielcaio.verso.repositories;

import br.com.gabrielcaio.verso.domain.entity.Follow;
import br.com.gabrielcaio.verso.domain.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

  Optional<Follow> findByFollowerAndFollowing(User follower, User following);

  boolean existsByFollowerAndFollowing(User follower, User following);

  Page<Follow> findAllByFollower(User follower, Pageable pageable);

  Page<Follow> findAllByFollowing(User following, Pageable pageable);

  @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
  Page<User> findFollowingByFollower(@Param("user") User user, Pageable pageable);

  @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
  Page<User> findFollowersByFollowing(@Param("user") User user, Pageable pageable);

  long countByFollower(User follower);

  long countByFollowing(User following);

  void deleteByFollowerAndFollowing(User follower, User following);
}
