package com.comision.CFE.Services.impl;

import com.comision.CFE.DTO.AuthResponseDTO;
import com.comision.CFE.DTO.LoginRequestDTO;
import com.comision.CFE.Entity.Usuario;
import com.comision.CFE.Repository.UsuarioRepository;
import com.comision.CFE.Services.AuthService;
import com.comision.CFE.Services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO authenticate(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponseDTO.builder()
                .token(token)
                .username(usuario.getUsername())
                .role(usuario.getRole())
                .build();
    }
}
