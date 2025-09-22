package com.cms.repository;

import com.cms.model.AnalysisJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnalysisJobRepository extends MongoRepository<AnalysisJob, String> {
    List<AnalysisJob> findByStatus(AnalysisJob.AnalysisStatus status);
}
