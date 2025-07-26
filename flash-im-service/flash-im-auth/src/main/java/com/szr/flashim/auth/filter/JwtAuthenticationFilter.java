package com.szr.flashim.auth.filter;

import com.szr.flashim.auth.model.CustomUserDetails;
import com.szr.flashim.auth.service.TokenService;
import com.szr.flashim.security.starter.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查黑名单
        if (tokenService.isBlacklisted(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);
            CustomUserDetails userDetails = createUserDetails(claims);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 滑动会话：自动刷新临近过期的令牌
            if (jwtUtil.isTokenNearExpiration(claims)) {
                String newToken = jwtUtil.generateAccessToken(claims);
                response.setHeader("X-New-Access-Token", newToken);
            }
        } catch (JwtException | IllegalArgumentException e) {
            //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            //return;
            //此处不处理，验证失败由EntryPoint处理
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private CustomUserDetails createUserDetails(Claims claims) {
        List<String> authorities = (List<String>) claims.get("authorities");
        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Long userId = (Long) claims.get("userId");
        String username = (String) claims.get("userName");

        return new CustomUserDetails(userId, username, "", grantedAuthorities);
    }
}
