package com.fabio.recipebook.services;

import com.fabio.recipebook.dto.request.TagRequestDTO;
import com.fabio.recipebook.dto.response.TagResponseDTO;
import com.fabio.recipebook.entities.Tag;
import com.fabio.recipebook.exceptions.BadRequestException;
import com.fabio.recipebook.exceptions.NotFoundException;
import com.fabio.recipebook.repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponseDTO> findAll() {
        return tagRepository.findAll(Sort.by("name")).stream()
                .map(TagResponseDTO::from)
                .toList();
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag with id " + id + " not found"));
    }

    @Transactional
    public TagResponseDTO create(TagRequestDTO request) {
        String name = request.name().trim();
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Tag '" + name + "' already exists");
        }
        return TagResponseDTO.from(tagRepository.save(new Tag(name)));
    }

    // The tag is removed from all recipes; the recipes themselves are kept
    @Transactional
    public void delete(Long id) {
        Tag tag = findById(id);
        tagRepository.removeFromAllRecipes(id);
        tagRepository.delete(tag);
    }
}
