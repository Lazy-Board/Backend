package com.example.lazier.user.security;

import com.example.lazier.user.dto.TokenDTO;
import com.example.lazier.user.entity.RefreshToken;
import com.example.lazier.user.service.login.LoginService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    //private long accessTokenVaildTime = 30 * 60 * 1000L; //30분
    //private long refreshTokenValidTime = 24 * 60 * 60 * 1000L; //1일

    private long accessTokenVaildTime =  1 * 60 * 1000L; //1분
    private long refreshTokenValidTime = 1 * 60 * 1000L; //1분

    private final LoginService loginService;

    @PostConstruct //종속성 주입이 완료된 후 실행되는 메서드 <- 호출되지 않아도 수행
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes()); //객체 조기화, secretKey를 Base64로 인코딩
    }

    public TokenDTO createAccessToken(Authentication authentication) {

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("roles", authorities);
        Date now = new Date();

        //Access Token
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenVaildTime))
                .signWith(SignatureAlgorithm.HS256, secretKey) //암호화 알고리즘, signature에 들어갈 secretKey
                .compact();

        //Refresh Token
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        return TokenDTO.builder()
                .accessToken(accessToken)
                .grantType("Bearer")
                .refreshToken(refreshToken)
                .key(authentication.getName())
                .build();
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization"); //"Authorization" : "Token"
    }

    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject(); //username
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = loginService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()); //authentication 리턴
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        throw new JwtException("토큰이 만료되었습니다");
    }

    public String validateRefreshToken(RefreshToken token) {
        String refreshToken = token.getRefreshToken();

        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(refreshToken);
            if (!claims.getBody().getExpiration().before(new Date())) {
                return recreationAccessToken(claims.getBody().getSubject(), claims.getBody().get("roles"));
            }
        } catch (ExpiredJwtException e) {
            log.warn("Refresh 토큰이 만료");
            throw new JwtException("Refresh 토큰이 만료되었습니다. 로그인이 필요합니다.");
        }
        return null;
    }

    public String recreationAccessToken(String userId, Object roles) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles", roles);
        Date now = new Date();

        //new access Token
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenVaildTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

}
