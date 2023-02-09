package com.dku.council.global.auth;

import com.dku.council.global.auth.jwt.AuthenticationToken;
import com.dku.council.global.auth.jwt.JwtAuthentication;
import com.dku.council.global.auth.jwt.JwtAuthenticationTokenProvider;
import com.dku.council.global.exception.CustomException;
import com.dku.council.global.exception.ExceptionDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationTokenProvider jwtAuthenticationTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

            AuthenticationToken tokenFromHeader = jwtAuthenticationTokenProvider.getTokenFromHeader(request);
            //인증 시작
            if (tokenFromHeader.getAccessToken() != null) {
                //Authentication 등록
                JwtAuthentication authentication = jwtAuthenticationTokenProvider.getAuthentication(tokenFromHeader.getAccessToken());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(requestWrapper, responseWrapper);
            responseWrapper.copyBodyToResponse();

        } catch (CustomException e) {
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ExceptionDto exceptionDto = new ExceptionDto(e);

            String exceptionMessage = objectMapper.writeValueAsString(exceptionDto);

            response.getWriter().write(exceptionMessage);
        }

    }
}
