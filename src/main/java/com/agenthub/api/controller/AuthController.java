package com.agenthub.api.controller;

import com.agenthub.api.dto.*;
import com.agenthub.api.mapper.AuthResponseMapper;
import com.agenthub.application.service.AuthApplicationService;
import com.agenthub.application.usecase.GetCurrentUserUseCase;
import com.agenthub.application.usecase.VerifyTokenUseCase;
import com.agenthub.infrastructure.context.annotations.IgnoreTenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口控制器.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthApplicationService authApplicationService;
    private final VerifyTokenUseCase verifyTokenUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final AuthResponseMapper responseMapper;

    public AuthController(
            AuthApplicationService authApplicationService,
            VerifyTokenUseCase verifyTokenUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase,
            AuthResponseMapper responseMapper) {
        this.authApplicationService = authApplicationService;
        this.verifyTokenUseCase = verifyTokenUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/login")
    @IgnoreTenantContext
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        var tokens = authApplicationService.login(request.username(), request.password());
        return ResponseEntity.ok(responseMapper.toResponse(tokens));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        var tokens = authApplicationService.refresh(request.refreshToken());
        return ResponseEntity.ok(responseMapper.toResponse(tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authApplicationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody VerifyRequest request) {
        Map<String, Object> result = verifyTokenUseCase.execute(request.accessToken());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@RequestHeader("Authorization") String authorization) {
        String token = extractBearerToken(authorization);
        var userInfo = getCurrentUserUseCase.execute(token);
        return ResponseEntity.ok(responseMapper.toResponse(userInfo));
    }

    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
