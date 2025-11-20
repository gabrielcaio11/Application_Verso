package br.com.gabrielcaio.verso.services;

import br.com.gabrielcaio.verso.controllers.error.BusinessException;
import br.com.gabrielcaio.verso.controllers.error.DataBaseException;
import br.com.gabrielcaio.verso.controllers.error.ResourceNotFoundException;
import br.com.gabrielcaio.verso.domain.entity.Roles;
import br.com.gabrielcaio.verso.dtos.CreateRolesRequestDTO;
import br.com.gabrielcaio.verso.dtos.RolesWithIdAndName;
import br.com.gabrielcaio.verso.repositories.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolesService
{

    private final RolesRepository rolesRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public RolesWithIdAndName findById(Long id)
    {
        log.info("[RolesService] Buscando role por ID: {}", id);
        var roles = rolesRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("[RolesService] Role não encontrada para ID: {}", id);
                    return new ResourceNotFoundException("Role não encontrada com id: " + id);
                });

        log.info("[RolesService] Role encontrada: {} - {}", roles.getId(), roles.getName());
        return new RolesWithIdAndName(roles.getId(), roles.getName());
    }

    @Transactional(readOnly = true)
    public Page<RolesWithIdAndName> findAllRolesWithIdAndName(Pageable pageable)
    {
        log.info("[RolesService] Listando todas as roles. Página: {}", pageable.getPageNumber());
        var rolesPage = rolesRepository.findAll(pageable);
        log.info("[RolesService] Total de roles encontradas: {}", rolesPage.getTotalElements());

        return rolesPage.map(role -> new RolesWithIdAndName(role.getId(), role.getName()));
    }

    @Transactional(readOnly = true)
    public RolesWithIdAndName findByName(String name)
    {
        log.info("[RolesService] Buscando role por nome: {}", name);

        var response = rolesRepository.findByName(name)
                .orElseThrow(() ->
                {
                    log.warn("[RolesService] Role não encontrada com nome: {}", name);
                    return new ResourceNotFoundException("Role não encontrada com nome: " + name);
                });

        log.info("[RolesService] Role encontrada: {} - {}", response.getId(), response.getName());
        return new RolesWithIdAndName(response.getId(), response.getName());
    }

    @Transactional
    public RolesWithIdAndName save(CreateRolesRequestDTO dto)
    {
        log.info("[RolesService] Criando nova role: {}", dto.getName());

        if(rolesRepository.findByName(dto.getName())
                .isPresent())
        {
            log.warn("[RolesService] Falha ao criar role. Nome já existe: {}", dto.getName());
            throw new BusinessException("Role com nome '" + dto.getName() + "' já existe.");
        }

        Roles role = new Roles();
        role.setName(dto.getName());
        Roles savedRole = rolesRepository.save(role);

        log.info(
                "[RolesService] Role criada com sucesso: {} - {}", savedRole.getId(),
                savedRole.getName()
        );
        return new RolesWithIdAndName(savedRole.getId(), savedRole.getName());
    }

    @Transactional
    public RolesWithIdAndName update(Long id, CreateRolesRequestDTO dto)
    {
        log.info("[RolesService] Atualizando role ID: {} para novo nome: {}", id, dto.getName());

        Roles existingRole = rolesRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn(
                            "[RolesService] Falha ao atualizar. Role não encontrada para ID: {}",
                            id
                    );
                    return new ResourceNotFoundException("Role não encontrada com id: " + id);
                });

        existingRole.setName(dto.getName());
        existingRole = rolesRepository.save(existingRole);

        log.info(
                "[RolesService] Role atualizada com sucesso: {} - {}", existingRole.getId(),
                existingRole.getName()
        );
        return new RolesWithIdAndName(existingRole.getId(), existingRole.getName());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id)
    {
        log.info("[RolesService] Solicitada remoção da role com ID: {}", id);

        var role = rolesRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.warn("[RolesService] Falha ao deletar. Role não encontrada: {}", id);
                    return new ResourceNotFoundException("Role não encontrada com id: " + id);
                });

        if(userService.findUsersByRole(role)
                .isPresent())
        {
            log.warn(
                    "[RolesService] Não é possível excluir role ID: {}. Existem usuários associados.",
                    id
            );
            throw new BusinessException(
                    "Não é possível deletar a role, existem usuários associados a ela.");
        }

        try
        {
            rolesRepository.delete(role);
            log.info("[RolesService] Role deletada com sucesso: {}", id);
        } catch(DataIntegrityViolationException e)
        {
            log.error("[RolesService] Erro de integridade ao deletar role ID: {}", id, e);
            throw new DataBaseException("Falha de integridade referencial");
        }
    }
}
