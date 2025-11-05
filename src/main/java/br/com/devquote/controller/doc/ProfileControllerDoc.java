package br.com.devquote.controller.doc;

import br.com.devquote.entity.Profile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Profiles")
public interface ProfileControllerDoc {

    @Operation(summary = "Get all profiles ordered by level")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    })
    ResponseEntity<List<Profile>> getAllProfiles();

    @Operation(summary = "Get all profiles with pagination and filters")
    @ApiResponse(responseCode = "200", description = "Paginated profiles retrieved successfully")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "10")
    @Parameter(name = "id", description = "Filter by profile ID", example = "1")
    @Parameter(name = "code", description = "Filter by profile code", example = "ADMIN")
    @Parameter(name = "name", description = "Filter by profile name", example = "Administrator")
    @Parameter(name = "description", description = "Filter by description")
    @Parameter(name = "level", description = "Filter by level", example = "1")
    @Parameter(name = "active", description = "Filter by active status", example = "true")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,code,name,description,level,active",
            array = @ArraySchema(schema = @Schema(type = "string", example = "level,asc"))
    )
    ResponseEntity<Page<Profile>> getAllProfilesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) List<String> sort);

    @Operation(summary = "Get profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    ResponseEntity<Profile> getProfileById(
            @Parameter(description = "Profile ID", required = true) @PathVariable Long id);

    @Operation(summary = "Create a new profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Profile code already exists")
    })
    ResponseEntity<Profile> createProfile(
            @Parameter(description = "Profile data", required = true) @RequestBody Profile profile);

    @Operation(summary = "Update an existing profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    ResponseEntity<Profile> updateProfile(
            @Parameter(description = "Profile ID", required = true) @PathVariable Long id,
            @Parameter(description = "Profile data", required = true) @RequestBody Profile profile);

    @Operation(summary = "Delete a profile")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Profile ID", required = true) @PathVariable Long id);

    @Operation(summary = "Delete multiple profiles")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profiles deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of Profile IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> ids);
}
