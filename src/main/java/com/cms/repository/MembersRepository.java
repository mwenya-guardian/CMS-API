package com.cms.repository;

import com.cms.model.Members;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembersRepository extends MongoRepository<Members, String> {
//    List<Members> findByActiveTrue(Sort sort);
//    Page<Members> findByActiveTrue(Pageable pageable);
//    List<Members> findAllByActiveTrueOrderByDisplayOrderAsc();
    List<Members> findByPosition(String position);
    List<Members> findByPositionType(String positionType);
    Members findByEmail(String email);
}
