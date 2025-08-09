package com.cms.service;

import com.cms.model.PastoralTeam;
import com.cms.repository.PastoralTeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PastoralTeamService {

    private final PastoralTeamRepository repo;

    /**
     * fetch all active team members ordered by displayOrder ascending
     */
    public List<PastoralTeam> getAllActive() {
        return repo.findByActiveTrue(Sort.by(Sort.Direction.ASC, "displayOrder"));
    }

    /**
     * fetch all members regardless of active flag
     */
    public List<PastoralTeam> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
    }

    /**
     * paginated fetch of active members
     */
    public Page<PastoralTeam> getActivePaginated(int page, int size) {
        Pageable pg = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "displayOrder"));
        return repo.findByActiveTrue(pg);
    }

    /**
     * find one by id
     */
    public Optional<PastoralTeam> getById(String id) {
        return repo.findById(id);
    }

    /**
     * create a new team member
     */
    public PastoralTeam create(PastoralTeam pt) {
        // if displayOrder not set, you could compute max+1 here
        return repo.save(pt);
    }

    /**
     * update existing; throws if not found
     */
    public PastoralTeam update(String id, PastoralTeam incoming) {
        PastoralTeam existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("PastoralTeam not found"));
        // copy updatable fields
        existing.setPosition(incoming.getPosition());
        existing.setFullName(incoming.getFullName());
        existing.setPositionType(incoming.getPositionType());
        existing.setBiography(incoming.getBiography());
        existing.setPhotoUrl(incoming.getPhotoUrl());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        existing.setDisplayOrder(incoming.getDisplayOrder());
        existing.setActive(incoming.getActive());
        return repo.save(existing);
    }

    /**
     * delete by id; throws if not found
     */
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("PastoralTeam not found");
        }
        repo.deleteById(id);
    }
}
