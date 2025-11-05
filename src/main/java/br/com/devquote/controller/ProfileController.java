package br.com.devquote.controller;

import br.com.devquote.controller.doc.ProfileControllerDoc;
import br.com.devquote.entity.Profile;
import br.com.devquote.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController implements ProfileControllerDoc {

    private final ProfileRepository profileRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_USER')")
    public ResponseEntity<List<Profile>> getAllProfiles() {
        List<Profile> profiles = profileRepository.findAllOrderedByLevel();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<Profile>> getAllProfilesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) List<String> sort) {

        Sort sortObj = Sort.by(Sort.Direction.ASC, "level", "name");
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = sort.stream()
                    .map(s -> {
                        String[] parts = s.split(",");
                        String field = parts[0];
                        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, field);
                    })
                    .toList();
            sortObj = Sort.by(orders);
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Profile> profiles = profileRepository.findAllWithFilters(
                id, code, name, description, level, active, pageable
        );

        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Profile> createProfile(@RequestBody Profile profile) {
        if (profileRepository.existsByCode(profile.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        Profile savedProfile = profileRepository.save(profile);
        return ResponseEntity.ok(savedProfile);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Profile> updateProfile(@PathVariable Long id, @RequestBody Profile profile) {
        if (!profileRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        profile.setId(id);
        Profile updatedProfile = profileRepository.save(profile);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        if (!profileRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        profileRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBulk(@RequestBody List<Long> ids) {
        profileRepository.deleteAllById(ids);
        return ResponseEntity.noContent().build();
    }
}
