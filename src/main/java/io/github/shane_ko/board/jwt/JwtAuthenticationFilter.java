package io.github.shane_ko.board.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. 헤더에서 값 꺼내기
        String authHeader = request.getHeader("Authorization");

        // 2. Bearer 체크
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("[JwtFilter] 토큰 없음, 통과");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. validateToken - 토큰 잘라내기
        String token = authHeader.substring(7);     // "Bearer " 가 7글자

        // 4. 검증
        if (!jwtTokenProvider.validateToken(token)) {
            log.info("[JwtFilter] 토큰 유효하지 않음, 통과");
            filterChain.doFilter(request, response);
            return;
        }

        //5. SecurityContext 에 저장 (가장 어려운 부분)
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        log.info("[JwtFilter] 인증 성공, userId = {}", userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userId,                     // principal (신원 정보 -> 추후 Service 에서 꺼낼 값)
                null,                       // credentials (비밀번호 이지만 여기선 null)
                Collections.emptyList()     // 권한 목록 (현재는 없음)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request,response);
    }
}
