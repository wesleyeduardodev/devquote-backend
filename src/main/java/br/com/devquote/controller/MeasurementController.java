package br.com.devquote.controller;
import br.com.devquote.controller.doc.MeasurementControllerDoc;
import br.com.devquote.dto.request.MeasurementRequestDTO;
import br.com.devquote.dto.response.MeasurementResponseDTO;
import br.com.devquote.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@Validated
@RequiredArgsConstructor
public class MeasurementController implements MeasurementControllerDoc {

    private final MeasurementService measurementService;

    @Override
    @GetMapping
    public ResponseEntity<List<MeasurementResponseDTO>> list() {
        return ResponseEntity.ok(measurementService.findAll());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MeasurementResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(measurementService.findById(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<MeasurementResponseDTO> create(@RequestBody @Valid MeasurementRequestDTO dto) {
        return new ResponseEntity<>(measurementService.create(dto), HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<MeasurementResponseDTO> update(@PathVariable Long id, @RequestBody @Valid MeasurementRequestDTO dto) {
        return ResponseEntity.ok(measurementService.update(id, dto));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        measurementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}