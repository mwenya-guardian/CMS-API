package com.cms.service;

import com.cms.model.TitheAndOffering;
import com.cms.repository.TitheAndOfferingRepository;
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
public class TitheAndOfferingService {

    private final TitheAndOfferingRepository repo;

    public List<TitheAndOffering> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "position"));
    }

    public Page<TitheAndOffering> getPaginated(int page, int size) {
        Pageable pg = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "position"));
        return repo.findAll(pg);
    }

    public Optional<TitheAndOffering> getTitheAndOfferingById(String id) {
        return repo.findById(id);
    }
    public TitheAndOffering create(TitheAndOffering incoming) {
        // if displayOrder not set, you could compute max+1 here
        TitheAndOffering newTitheAndOffering = new TitheAndOffering();
        newTitheAndOffering.setTitle(incoming.getTitle().toLowerCase());
        List<String> lowerCaseMethods = incoming.getMethod().stream().map(String::toLowerCase).toList();
        newTitheAndOffering.setMethod(lowerCaseMethods);
        return repo.save(newTitheAndOffering);
    }

    public TitheAndOffering update(String id, TitheAndOffering incoming) {
        TitheAndOffering existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("TitheAndOffering not found"));
        // copy updatable fields
        TitheAndOffering newTitheAndOffering = new TitheAndOffering();
        newTitheAndOffering.setId(existing.getId());
        newTitheAndOffering.setTitle(incoming.getTitle().toLowerCase());
        List<String> lowerCaseMethods = incoming.getMethod().stream().map(String::toLowerCase).toList();
        newTitheAndOffering.setMethod(lowerCaseMethods);
        return repo.save(newTitheAndOffering);
    }
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("TitheAndOffering not found");
        }
        repo.deleteById(id);
    }
}
