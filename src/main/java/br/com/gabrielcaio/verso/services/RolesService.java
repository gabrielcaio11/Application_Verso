package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.dtos.CreateRolesRequestDTO;
import br.com.gabrielcaio.verso.dtos.RolesWithIdAndName;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolesService {

    private final RolesRepository rolesRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public RolesWithIdAndName findById(Long id) {
        var roles = rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com id: " + id));
        return new RolesWithIdAndName(roles.getId(), roles.getName());
    }

    @Transactional(readOnly = true)
    public Page<RolesWithIdAndName> findAllRolesWithIdAndName(Pageable pageable) {
        var rolesPage = rolesRepository.findAll(pageable);
        return rolesPage.map(role -> new RolesWithIdAndName(role.getId(), role.getName()));
    }

    @Transactional(readOnly = true)
    public RolesWithIdAndName findByName(String name) {
        var response = rolesRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com nome: " + name)
                );
        return new RolesWithIdAndName(response.getId(), response.getName());
    }

    @Transactional
    public RolesWithIdAndName save(CreateRolesRequestDTO dto) {
        if (rolesRepository.findByName(dto.getName()).isPresent()) {
            throw new BusinessException("Role com nome '" + dto.getName() + "' já existe.");
        }
        Roles role = new Roles();
        role.setName(dto.getName());
        Roles savedRole = rolesRepository.save(role);
        return new RolesWithIdAndName(savedRole.getId(), savedRole.getName());
    }

    @Transactional
    public RolesWithIdAndName update(Long id, CreateRolesRequestDTO dto) {
        Roles existingRole = rolesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role não encontrada com id: " + id));
        existingRole.setName(dto.getName());
        existingRole = rolesRepository.save(existingRole);
        return new RolesWithIdAndName(existingRole.getId(), existingRole.getName());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        var role = rolesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role não encontrada com id: " + id));

        if (userService.findUsersByRole(role).isPresent()) {
            throw new BusinessException("Não é possível deletar a role, existem usuários associados a ela.");
        }

        try {
            rolesRepository.delete(role);
        } catch (DataIntegrityViolationException e) {
            throw new DataBaseException("Falha de integridade referencial");
        }
    }
}