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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public FollowResponseDTO followUser(Long userId) {
        var currentUser = userService.getCurrentUser();
        var userToFollow = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Validações
        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("Você não pode seguir a si mesmo");
        }

        if (followRepository.existsByFollowerAndFollowing(currentUser, userToFollow)) {
            throw new BusinessException("Você já está seguindo este usuário");
        }

        // Criar seguimento
        var follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowing(userToFollow);
        follow = followRepository.save(follow);

        return toDto(follow);
    }

    @Transactional
    public void unfollowUser(Long userId) {
        var currentUser = userService.getCurrentUser();
        var userToUnfollow = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!followRepository.existsByFollowerAndFollowing(currentUser, userToUnfollow)) {
            throw new ResourceNotFoundException("Você não está seguindo este usuário");
        }

        followRepository.deleteByFollowerAndFollowing(currentUser, userToUnfollow);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getFollowing(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var followingPage = followRepository.findFollowingByFollower(currentUser, pageable);

        return followingPage.map(user -> toUserProfileDto(user, currentUser));
    }

    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getFollowers(Pageable pageable) {
        var currentUser = userService.getCurrentUser();
        var followersPage = followRepository.findFollowersByFollowing(currentUser, pageable);

        return followersPage.map(user -> toUserProfileDto(user, currentUser));
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long userId) {
        var currentUser = userService.getCurrentUser();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return toUserProfileDto(user, currentUser);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId) {
        var currentUser = userService.getCurrentUser();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return followRepository.existsByFollowerAndFollowing(currentUser, user);
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
                isFollowing
        );
    }
}