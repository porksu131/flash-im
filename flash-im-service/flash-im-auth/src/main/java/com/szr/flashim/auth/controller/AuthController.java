package com.szr.flashim.auth.controller;

import com.szr.flashim.auth.service.AuthService;
import com.szr.flashim.general.model.ResponseResult;
import com.szr.flashim.security.starter.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody Map<String, String> map) throws Exception {
        String userName = map.get("userName");
        String password = map.get("password");
        return ResponseResult.ok(authService.login(userName, password));
    }

    @PostMapping("/refresh")
    public ResponseResult<Map<String, Object>> refreshToken(@RequestBody Map<String, String> map) throws Exception {
        String refreshToken = map.get("refreshToken");
        return ResponseResult.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseResult<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseResult.ok();
    }

    @PostMapping("/register")
    public ResponseResult<Map<String, Object>> register(@RequestBody Map<String, String> map) {
        String userName = map.get("userName");
        String password = map.get("password");
        return ResponseResult.ok(authService.register(userName, password));
    }

    // 新增：公钥端点（供网关使用）
    @GetMapping("/publicKey")
    public ResponseResult<String> getPublicKey() {
        return ResponseResult.ok(jwtUtil.getPublicKeyPem());
    }
}