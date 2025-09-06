package com.cms.repository;

import com.cms.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findAllByOrderByUpdatedAt(Pageable pageable);
    long countByType(Post.PostType type);
    long countById(String id);
    long countByUpdatedAtBetween(Instant start, Instant end);
}
