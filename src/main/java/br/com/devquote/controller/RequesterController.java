package br.com.devquote.controller;
import br.com.devquote.controller.doc.RequesterControllerDoc;
import br.com.devquote.dto.request.RequesterRequestDTO;
import br.com.devquote.dto.response.RequesterResponseDTO;
import br.com.devquote.service.RequesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requesters")
@Validated
@RequiredArgsConstructor
public class RequesterController implements RequesterControllerDoc {

    private final RequesterService requesterService;

    @Override
    @GetMapping
    public ResponseEntity<List<RequesterResponseDTO>> list() {
        return ResponseEntity.ok(requesterService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RequesterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(requesterService.findById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<RequesterResponseDTO> create(@RequestBody @Valid RequesterRequestDTO dto) {
        return new ResponseEntity<>(requesterService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<RequesterResponseDTO> update(@PathVariable Long id, @RequestBody @Valid RequesterRequestDTO dto) {
        return ResponseEntity.ok(requesterService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        requesterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}