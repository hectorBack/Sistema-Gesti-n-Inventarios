package com.comision.CFE.Services;

import com.comision.CFE.DTO.AuthResponseDTO;
import com.comision.CFE.DTO.LoginRequestDTO;

public interface AuthService {

    AuthResponseDTO authenticate(LoginRequestDTO request);
}
