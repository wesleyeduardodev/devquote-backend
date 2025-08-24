package br.com.devquote.service;

import br.com.devquote.dto.response.DashboardStatsResponse;
import org.springframework.security.core.Authentication;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats(Authentication authentication);
}