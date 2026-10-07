package com.lele.leleinventory.web.api;

import com.lele.leleinventory.domain.AuditAction;
import com.lele.leleinventory.domain.AuditEntityType;
import com.lele.leleinventory.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController {

  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final AuditService auditService;

  public AuthApiController(
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository,
      AuditService auditService
  ) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.auditService = auditService;
  }

  @PostMapping("/api/auth/login")
  public ResponseEntity<?> login(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody LoginRequest loginRequest
  ) {
    if (loginRequest == null
        || loginRequest.username() == null
        || loginRequest.username().isBlank()
        || loginRequest.password() == null) {
      return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "username_and_password_required"));
    }

    try {
      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());

      Authentication authentication = authenticationManager.authenticate(token);

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, request, response);

      auditService.success(
          AuditAction.LOGIN,
          AuditEntityType.AUTH,
          null,
          authentication.getName(),
          "用户登录 " + authentication.getName(),
          null,
          AuditService.mapOf("username", authentication.getName())
      );

      return ResponseEntity.ok(Map.of("ok", true, "username", authentication.getName()));
    } catch (AuthenticationException ex) {
      auditService.failure(
          AuditAction.LOGIN,
          AuditEntityType.AUTH,
          null,
          loginRequest.username(),
          "登录失败 " + loginRequest.username(),
          null,
          AuditService.mapOf("username", loginRequest.username()),
          ex.getMessage()
      );
      throw ex;
    }
  }

  @PostMapping("/api/auth/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication == null ? "anonymous" : authentication.getName();
    new SecurityContextLogoutHandler().logout(request, response, authentication);
    auditService.success(
        AuditAction.LOGOUT,
        AuditEntityType.AUTH,
        null,
        username,
        "用户退出 " + username,
        AuditService.mapOf("username", username),
        null
    );
    return ResponseEntity.ok(Map.of("ok", true));
  }

  @GetMapping("/api/auth/me")
  public ResponseEntity<?> me(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(401).body(Map.of("ok", false));
    }
    return ResponseEntity.ok(Map.of("ok", true, "username", authentication.getName()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<?> onAuthFail(AuthenticationException ex) {
    return ResponseEntity.status(401).body(Map.of(
        "ok", false,
        "error", "invalid_username_or_password",
        "detail", ex.getMessage() == null ? "" : ex.getMessage()
    ));
  }

  public record LoginRequest(String username, String password) {}
}
