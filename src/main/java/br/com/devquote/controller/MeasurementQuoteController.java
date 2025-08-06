package br.com.devquote.controller;
import br.com.devquote.controller.doc.MeasurementQuoteControllerDoc;
import br.com.devquote.dto.request.MeasurementQuoteRequestDTO;
import br.com.devquote.dto.response.MeasurementQuoteResponseDTO;
import br.com.devquote.service.MeasurementQuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/measurement-quotes")
@Validated
@RequiredArgsConstructor
public class MeasurementQuoteController implements MeasurementQuoteControllerDoc {

    private final MeasurementQuoteService measurementQuoteService;

    @Override
    @GetMapping
    public ResponseEntity<List<MeasurementQuoteResponseDTO>> list() {
        return ResponseEntity.ok(measurementQuoteService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MeasurementQuoteResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(measurementQuoteService.findById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<MeasurementQuoteResponseDTO> create(@RequestBody @Valid MeasurementQuoteRequestDTO dto) {
        return new ResponseEntity<>(measurementQuoteService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<MeasurementQuoteResponseDTO> update(@PathVariable Long id, @RequestBody @Valid MeasurementQuoteRequestDTO dto) {
        return ResponseEntity.ok(measurementQuoteService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        measurementQuoteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}