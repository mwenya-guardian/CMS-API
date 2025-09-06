package com.cms.service;

import com.cms.dto.response.FileUploadResponse;
import com.cms.model.Members;
import com.cms.repository.MembersRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MembersService {

    private final MembersRepository repo;
    private final FileService fileService;

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
    public Members create(Members request) {
        // if displayOrder not set, you could compute max+1 here
        Members newMembers = new Members();
        newMembers.setPosition(request.getPosition().toLowerCase());
        newMembers.setFirstname(request.getFirstname().toLowerCase());
        newMembers.setLastname(request.getLastname().toLowerCase());
        newMembers.setPositionType(request.getPositionType().toLowerCase());
        newMembers.setPhotoUrl(request.getPhotoUrl());
        newMembers.setEmail(request.getEmail());
        newMembers.setPhone(request.getPhone());
        return repo.save(newMembers);
    }

    public Members update(String id, Members request) throws IOException {
        Members existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Members not found"));
        // copy updatable fields
        existing.setPosition(request.getPosition().toLowerCase());
        existing.setFirstname(request.getFirstname().toLowerCase());
        existing.setLastname(request.getLastname().toLowerCase());
        existing.setPositionType(request.getPositionType().toLowerCase());
        if(request.getPhotoUrl() == null || request.getPhotoUrl().isBlank()){
            existing.setPhotoUrl(request.getPhotoUrl());
            if(existing.getPhotoUrl() != null && !existing.getPhotoUrl().isBlank())
                fileService.deleteFile(existing.getPhotoUrl());
        }
        existing.setPhotoUrl(request.getPhotoUrl());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        return repo.save(existing);
    }


public FileUploadResponse uploadImage(MultipartFile file) throws IOException {
    return fileService.uploadImage(file, true);
}
    public void delete(String id) throws IOException {
        Members members = repo.findById(id).orElseThrow(()->
          new RuntimeException("Member not found")
        );
        if(members.getPhotoUrl() !=null && !members.getPhotoUrl().isBlank()){
            fileService.deleteFile(members.getPhotoUrl());
        }
        repo.deleteById(id);
    }
}
