package com.ecoquest.websocket;

import com.ecoquest.entity.User;
import com.ecoquest.repository.UserRepository;
import com.ecoquest.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

/**
 * The STOMP handshake is a plain HTTP request, so our normal JwtAuthFilter (which reads the
 * Authorization header) doesn't apply - the browser's WebSocket/SockJS client can't set custom
 * headers on the handshake. Instead the frontend connects to /ws?token=<jwt> and this
 * interceptor validates it, resolving the user id into the session attributes so
 * UserHandshakeHandler can build a Principal from it (needed for convertAndSendToUser routing).
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String token = Optional.ofNullable(
                        UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token"))
                .orElse(null);

        if (token == null) {
            return false; // reject the handshake - no token provided
        }

        try {
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return false;
            }
            attributes.put("userId", user.getId());
            return true;
        } catch (Exception e) {
            return false; // expired/invalid token - reject the handshake
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
