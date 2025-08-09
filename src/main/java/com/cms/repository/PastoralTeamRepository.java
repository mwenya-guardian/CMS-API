package com.cms.repository;

import com.cms.model.PastoralTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PastoralTeamRepository extends MongoRepository<PastoralTeam, String> {
    List<PastoralTeam> findByActiveTrue(Sort sort);
    Page<PastoralTeam> findByActiveTrue(Pageable pageable);
    List<PastoralTeam> findAllByActiveTrueOrderByDisplayOrderAsc();
}
