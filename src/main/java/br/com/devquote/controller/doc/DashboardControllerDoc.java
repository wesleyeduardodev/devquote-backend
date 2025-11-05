package br.com.devquote.controller.doc;

import br.com.devquote.dto.response.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Dashboard")
public interface DashboardControllerDoc {

    @Operation(summary = "Get dashboard statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @Parameter(description = "Authenticated user", hidden = true) Authentication authentication);
}
