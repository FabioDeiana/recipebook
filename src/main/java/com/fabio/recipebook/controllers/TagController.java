package com.fabio.recipebook.controllers;

import com.fabio.recipebook.dto.request.TagRequestDTO;
import com.fabio.recipebook.dto.response.TagResponseDTO;
import com.fabio.recipebook.services.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagResponseDTO> findAll() {
        return tagService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponseDTO create(@RequestBody @Valid TagRequestDTO request) {
        return tagService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
