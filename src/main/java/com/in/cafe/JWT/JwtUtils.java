package com.in.cafe.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtils {

    private String secret= "rohanguptaproject";

    public Date extractExpiration(String token){
        return extractClaimis(token, Claims::getExpiration);
    }

    public String extractUsername(String token){
        return extractClaimis(token, Claims::getSubject);
    }

    public  <T> T extractClaimis(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token){
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT String cannot be null or empty");
        }



        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username= extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !this.isTokenExpired(token));


    }

    public String generateToken(String username, String role){
        Map<String, Object> claims= new HashMap<>();
        claims.put("role", role);
        return createToken(claims,username);

    }

    private String createToken(Map<String, Object> claims, String subject){

        //String encodedString = Base64.getEncoder().encodeToString(secret.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 *60 *60 *10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }
}
