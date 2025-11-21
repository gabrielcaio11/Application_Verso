package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Follow;
import br.com.gabrielcaio.verso.domain.entity.User;
import br.com.gabrielcaio.verso.dtos.FollowResponseDTO;
import br.com.gabrielcaio.verso.dtos.UserProfileDTO;
import br.com.gabrielcaio.verso.repositories.FollowRepository;
import br.com.gabrielcaio.verso.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final UserService userService;

  @Transactional
  public FollowResponseDTO followUser(Long userId) {
    var currentUser = userService.getCurrentUser();

    log.info(
        "[FOLLOW] Tentando seguir usuário. followerId={}, followingId={}",
        currentUser.getId(),
        userId);

    var userToFollow =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("[FOLLOW] Usuário para seguir não encontrado. followingId={}", userId);
                  return new ResourceNotFoundException("Usuário não encontrado");
                });

    if (currentUser.getId().equals(userId)) {
      log.warn("[FOLLOW] Usuário tentou seguir a si mesmo. userId={}", userId);
      throw new BusinessException("Você não pode seguir a si mesmo");
    }

    if (followRepository.existsByFollowerAndFollowing(currentUser, userToFollow)) {
      log.warn(
          "[FOLLOW] Usuário já estava seguindo. followerId={}, followingId={}",
          currentUser.getId(),
          userId);
      throw new BusinessException("Você já está seguindo este usuário");
    }

    // Criar seguimento
    log.debug(
        "[FOLLOW] Criando relação de follow no banco. followerId={}, followingId={}",
        currentUser.getId(),
        userId);

    var follow = new Follow();
    follow.setFollower(currentUser);
    follow.setFollowing(userToFollow);
    follow = followRepository.save(follow);

    log.info(
        "[FOLLOW] Usuário seguido com sucesso. followId={}, followerId={}, followingId={}",
        follow.getId(),
        currentUser.getId(),
        userId);

    return toDto(follow);
  }

  @Transactional
  public void unfollowUser(Long userId) {
    var currentUser = userService.getCurrentUser();

    log.info(
        "[FOLLOW] Tentando deixar de seguir usuário. followerId={}, followingId={}",
        currentUser.getId(),
        userId);

    var userToUnfollow =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "[FOLLOW] Usuário para deixar de seguir não encontrado. followingId={}",
                      userId);
                  return new ResourceNotFoundException("Usuário não encontrado");
                });

    if (!followRepository.existsByFollowerAndFollowing(currentUser, userToUnfollow)) {
      log.warn(
          "[FOLLOW] Tentativa de desfazer follow inexistente. followerId={}, followingId={}",
          currentUser.getId(),
          userId);
      throw new ResourceNotFoundException("Você não está seguindo este usuário");
    }

    followRepository.deleteByFollowerAndFollowing(currentUser, userToUnfollow);

    log.info(
        "[FOLLOW] Follow removido com sucesso. followerId={}, unfollowedId={}",
        currentUser.getId(),
        userId);
  }

  @Transactional(readOnly = true)
  public Page<UserProfileDTO> getFollowing(Pageable pageable) {
    var currentUser = userService.getCurrentUser();

    log.debug(
        "[FOLLOW] Buscando usuários que o currentUser segue. followerId={}, page={}",
        currentUser.getId(),
        pageable);

    var followingPage = followRepository.findFollowingByFollower(currentUser, pageable);

    log.info(
        "[FOLLOW] Following recuperado. followerId={}, totalElements={}",
        currentUser.getId(),
        followingPage.getTotalElements());

    return followingPage.map(user -> toUserProfileDto(user, currentUser));
  }

  @Transactional(readOnly = true)
  public Page<UserProfileDTO> getFollowers(Pageable pageable) {
    var currentUser = userService.getCurrentUser();

    log.debug(
        "[FOLLOW] Buscando seguidores do currentUser. followingId={}, page={}",
        currentUser.getId(),
        pageable);

    var followersPage = followRepository.findFollowersByFollowing(currentUser, pageable);

    log.info(
        "[FOLLOW] Followers recuperados. followingId={}, totalElements={}",
        currentUser.getId(),
        followersPage.getTotalElements());

    return followersPage.map(user -> toUserProfileDto(user, currentUser));
  }

  @Transactional(readOnly = true)
  public UserProfileDTO getUserProfile(Long userId) {
    var currentUser = userService.getCurrentUser();

    log.info(
        "[PROFILE] Buscando perfil do usuário. targetUserId={}, requesterId={}",
        userId,
        currentUser.getId());

    var user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("[PROFILE] Perfil não encontrado. userId={}", userId);
                  return new ResourceNotFoundException("Usuário não encontrado");
                });

    return toUserProfileDto(user, currentUser);
  }

  @Transactional(readOnly = true)
  public boolean isFollowing(Long userId) {
    var currentUser = userService.getCurrentUser();

    log.debug(
        "[FOLLOW] Verificando isFollowing. followerId={}, targetUserId={}",
        currentUser.getId(),
        userId);

    var user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn(
                      "[FOLLOW] Usuário não encontrado para verificação de follow. userId={}",
                      userId);
                  return new ResourceNotFoundException("Usuário não encontrado");
                });

    boolean result = followRepository.existsByFollowerAndFollowing(currentUser, user);

    log.debug(
        "[FOLLOW] isFollowing result. followerId={}, targetUserId={}, result={}",
        currentUser.getId(),
        userId,
        result);

    return result;
  }

  private FollowResponseDTO toDto(Follow follow) {
    return new FollowResponseDTO(
        follow.getId(),
        follow.getFollower().getId(),
        follow.getFollower().getUsername(),
        follow.getFollowing().getId(),
        follow.getFollowing().getUsername(),
        follow.getCreatedAt());
  }

  private UserProfileDTO toUserProfileDto(User user, User currentUser) {
    long followersCount = followRepository.countByFollowing(user);
    long followingCount = followRepository.countByFollower(user);
    boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, user);

    return new UserProfileDTO(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        followersCount,
        followingCount,
        isFollowing);
  }
}
