package com.cms.service;

import com.cms.dto.response.FileUploadResponse;
import com.cms.dto.response.PageResponse;
import com.cms.exception.WrongFileTypeException;
import com.cms.model.Post;
import com.cms.model.PostReaction;
import com.cms.model.ReactionBaseDocument.ReactionType;
import com.cms.repository.PostRepository;
import com.cms.service.ReactionService.ReactionCategory;

import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ReactionService reactionService;
    private final AuthService authService;
    private final FileService fileService;


    public Optional<Post> getById(String id) {
        return postRepository.findById(id);
    }
    public Page<Post> getAllByIdList(List<String> ids, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return postRepository.findAllById(ids, pageable);
    }
    public PageResponse<Post> getAllLikedPostByUser(int page, int limit){
        String userId = authService.getCurrentUser().getId();
        List<String> idList= reactionService
            .findByTypeAndUserId(ReactionType.LIKE, ReactionCategory.POST, userId)
            .stream().map((reaction)->{
                if(reaction instanceof PostReaction){
                    return ((PostReaction)reaction).getPost().getId();
                }
                return "";
            }).toList();
        Page<Post> post = getAllByIdList(idList, page, limit);
        return new PageResponse<>(post.getContent(), post.getNumber(), post.getSize(), post.getTotalPages());
    }
    public PageResponse<Post> getPageResponse(int page, int limit){
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Post> post = postRepository.findAll(pageable);
        return new PageResponse<>(post.getContent(), post.getNumber(), post.getSize(), post.getTotalPages());
    }

    public PageResponse<Post> getPageResponseByType(int page, int limit, Post.PostType type){
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Post> post = postRepository.findAllByType(type, pageable);
        return new PageResponse<>(post.getContent(), post.getNumber(), post.getSize(), post.getTotalPages());
    }

    /**
     * Create a post: saves file via FileService.uploadImage and persists Post with resourceUrl (relative path).
     */

    public Post createImagePost(String caption, MultipartFile file, boolean isPublic) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Media file is required");
        }

        boolean isImage = file.getContentType() != null && file.getContentType().startsWith("image");
        if(!isImage)
            throw new WrongFileTypeException("This file type is not supported");
        FileUploadResponse fileUploadResponse = fileService.uploadImage(file, isPublic);

        Post post = new Post();
        post.setType(Post.PostType.IMAGE);
        post.setCaption(caption);
        post.setResourceUrl(fileUploadResponse.getUrl()); // store url like private/photos/2025/uuid.jpg
        post.setIsPublic(isPublic);
        return postRepository.save(post);
    }

    public Post createVideoPost(String caption, MultipartFile file, boolean isPublic) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Media file is required");
        }

        boolean isVideo = file.getContentType() != null && file.getContentType().startsWith("video");
        if(!isVideo)
            throw new WrongFileTypeException("This file type is not supported");
        FileUploadResponse fileUploadResponse = fileService.uploadVideo(file, isPublic);

        Post post = new Post();
        post.setType(Post.PostType.VIDEO);
        post.setCaption(caption);
        post.setResourceUrl(fileUploadResponse.getUrl()); // store relative path like protected/photos/2025/uuid.jpg
        post.setIsPublic(isPublic);
        return postRepository.save(post);
    }

    public Post createCaptionPost(String caption, Boolean isPublic) {
        Post post = new Post();
        post.setType(Post.PostType.TEXT);
        post.setCaption(caption);
        post.setIsPublic(Boolean.TRUE.equals(isPublic));
        return postRepository.save(post);
    }

    public Post updateCaption(String id, String caption) {
        Post p = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        p.setCaption(caption);
        return postRepository.save(p);
    }

    public void delete(String id) {
        Post p = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        // attempt to delete underlying file
        if (p.getResourceUrl() != null) {
            try {
                fileService.deleteFile(p.getResourceUrl());
            } catch (Exception e) {
                // log but continue deleting DB entry
                // logger.warn("Failed to delete file for post {}", id, e);
            }
        }
        postRepository.deleteById(id);
    }

    /**
     * Load the media Resource associated with a post.
     */
    public Resource loadMediaForPost(String postId) throws Exception {
        Post p = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        String rel = p.getResourceUrl();
        if (rel == null) throw new RuntimeException("No media attached");
        return fileService.loadAsResource(rel);
    }
}
