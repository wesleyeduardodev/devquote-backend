package br.com.devquote.controller;
import br.com.devquote.controller.doc.QuoteControllerDoc;
import br.com.devquote.dto.request.QuoteRequestDTO;
import br.com.devquote.dto.response.QuoteResponseDTO;
import br.com.devquote.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@Validated
@RequiredArgsConstructor
public class QuoteController implements QuoteControllerDoc {

    private final QuoteService quoteService;

    @Override
    @GetMapping
    public ResponseEntity<List<QuoteResponseDTO>> list() {
        return ResponseEntity.ok(quoteService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.findById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<QuoteResponseDTO> create(@RequestBody @Valid QuoteRequestDTO dto) {
        return new ResponseEntity<>(quoteService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponseDTO> update(@PathVariable Long id, @RequestBody @Valid QuoteRequestDTO dto) {
        return ResponseEntity.ok(quoteService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}