package com.cms.repository;

import com.cms.model.PastoralTeam;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PastoralTeamRepository extends MongoRepository<PastoralTeam, String> {
    List<PastoralTeam> findAllByActiveTrueOrderByDisplayOrderAsc();
}
