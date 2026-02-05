package com.comision.CFE.Services;

import com.comision.CFE.Entity.Usuario;
import io.jsonwebtoken.Claims;

import java.security.Key;
import java.util.function.Function;

public interface JwtService {

    String generateToken(Usuario usuario);
    String extractUsername(String token);
    boolean isTokenValid(String token, Usuario usuario);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    boolean isTokenExpired(String token);
    Key getSignInKey();
}
