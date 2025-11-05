package br.com.devquote.controller.doc;

import br.com.devquote.dto.CreateUserDto;
import br.com.devquote.dto.UpdateUserDto;
import br.com.devquote.dto.UserDto;
import br.com.devquote.dto.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "User Management")
public interface UserManagementControllerDoc {

    @Operation(summary = "Get all users with pagination, filters and sorting")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @Parameter(name = "page", description = "Page number (0-based)", example = "0")
    @Parameter(name = "size", description = "Number of items per page", example = "5")
    @Parameter(name = "id", description = "Filter by user ID", example = "1")
    @Parameter(name = "username", description = "Filter by username (partial match)", example = "john")
    @Parameter(name = "email", description = "Filter by email (partial match)", example = "john@example.com")
    @Parameter(name = "name", description = "Filter by first or last name (partial match)", example = "John")
    @Parameter(name = "enabled", description = "Filter by enabled status", example = "true")
    @Parameter(
            name = "sort",
            description = "Repeatable. Format: field,(asc|desc). Allowed: id,username,email,firstName,lastName,enabled,createdAt,updatedAt",
            array = @ArraySchema(schema = @Schema(type = "string", example = "id,desc"))
    )
    ResponseEntity<PagedResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) MultiValueMap<String, String> params);

    @Operation(summary = "Get user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<UserDto> getUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);

    @Operation(summary = "Create a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or username/email already exists")
    })
    ResponseEntity<UserDto> createUser(
            @Parameter(description = "User creation data", required = true) @Valid @RequestBody CreateUserDto request);

    @Operation(summary = "Update an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(description = "User update data", required = true) @Valid @RequestBody UpdateUserDto request);

    @Operation(summary = "Delete a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);

    @Operation(summary = "Delete multiple users")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Users deleted successfully")
    })
    ResponseEntity<Void> deleteBulk(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of User IDs to delete",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Long.class)))
            )
            @RequestBody List<Long> ids);

    @Operation(summary = "Reset user password to default")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<Void> resetPassword(
            @Parameter(description = "User ID", required = true) @PathVariable Long id);
}
