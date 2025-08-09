package com.cms.service;

import com.cms.model.ChurchDetails;
import com.cms.repository.ChurchDetailsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChurchDetailsService {

    private final ChurchDetailsRepository repo;

    /**
     * fetch all church detail records, ordered by name ascending
     */
    public List<ChurchDetails> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * fetch one record by id
     */
    public Optional<ChurchDetails> getById(String id) {
        return repo.findById(id);
    }

    /**
     * create a new church details record
     */
    public ChurchDetails create(ChurchDetails details) {
        return repo.save(details);
    }

    /**
     * update existing record; throws if not found
     */
    public ChurchDetails update(String id, ChurchDetails incoming) {
        ChurchDetails existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("ChurchDetails not found"));
        // copy fields that can be changed
        existing.setName(incoming.getName());
        existing.setAddress(incoming.getAddress());
        existing.setDocumentName(incoming.getDocumentName());
        existing.setGreeting(incoming.getGreeting());
        existing.setMessage(incoming.getMessage());
        existing.setPoBox(incoming.getPoBox());
        existing.setCity(incoming.getCity());
        existing.setProvince(incoming.getProvince());
        existing.setCountry(incoming.getCountry());
        existing.setTel(incoming.getTel());
        existing.setCell(incoming.getCell());
        existing.setEmail(incoming.getEmail());
        existing.setWebsite(incoming.getWebsite());
        return repo.save(existing);
    }

    /**
     * delete record by id; throws if not found
     */
    public void delete(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("ChurchDetails not found");
        }
        repo.deleteById(id);
    }
}
