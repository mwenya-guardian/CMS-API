package com.cms.service;

import com.cms.model.Members;
import com.cms.repository.MembersRepository;
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
public class MembersService {

    private final MembersRepository repo;

    public List<Members> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "position"));
    }
    public List<Members> getMembersByPositionType(String positionType){
        return repo.findByPositionType(positionType.toLowerCase());
    }
    public List<Members> getMembersByPosition(String position){
        return repo.findByPosition(position.toLowerCase());
    }
    public Members getByEmail(String email) {
        return repo.findByEmail(email.toLowerCase());
    }

    public Page<Members> getPaginated(int page, int size) {
        Pageable pg = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "position"));
        return repo.findAll(pg);
    }

    public Optional<Members> getMembersById(String id) {
        return repo.findById(id);
    }
    public Members create(Members incoming) {
        // if displayOrder not set, you could compute max+1 here
        Members newMembers = new Members();
        newMembers.setPosition(incoming.getPosition().toLowerCase());
        newMembers.setFirstname(incoming.getFirstname().toLowerCase());
        newMembers.setLastname(incoming.getLastname().toLowerCase());
        newMembers.setPositionType(incoming.getPositionType().toLowerCase());
        newMembers.setPhotoUrl(incoming.getPhotoUrl());
        newMembers.setEmail(incoming.getEmail());
        newMembers.setPhone(incoming.getPhone());
        return repo.save(newMembers);
    }

    public Members update(String id, Members incoming) {
        Members existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Members not found"));
        // copy updatable fields
        existing.setPosition(incoming.getPosition().toLowerCase());
        existing.setFirstname(incoming.getFirstname().toLowerCase());
        existing.setLastname(incoming.getLastname().toLowerCase());
        existing.setPositionType(incoming.getPositionType().toLowerCase());
        existing.setPhotoUrl(incoming.getPhotoUrl());
        existing.setEmail(incoming.getEmail());
        existing.setPhone(incoming.getPhone());
        return repo.save(existing);
    }
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Members not found");
        }
        repo.deleteById(id);
    }
}
