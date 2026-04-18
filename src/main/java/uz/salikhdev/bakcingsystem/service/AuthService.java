package uz.salikhdev.bakcingsystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.salikhdev.bakcingsystem.dto.AuthResponse;
import uz.salikhdev.bakcingsystem.dto.LoginRequest;
import uz.salikhdev.bakcingsystem.dto.RefreshRequest;
import uz.salikhdev.bakcingsystem.dto.RegisterRequest;
import uz.salikhdev.bakcingsystem.entity.Role;
import uz.salikhdev.bakcingsystem.entity.User;
import uz.salikhdev.bakcingsystem.entity.UserStatus;
import uz.salikhdev.bakcingsystem.entity.UserType;
import uz.salikhdev.bakcingsystem.exception.AlreadyExistsException;
import uz.salikhdev.bakcingsystem.exception.BadRequestException;
import uz.salikhdev.bakcingsystem.exception.NotFoundException;
import uz.salikhdev.bakcingsystem.repository.UserRepository;
import uz.salikhdev.bakcingsystem.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AlreadyExistsException("Phone number already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .passport(request.getPassport())
                .pinfl(request.getPinfl())
                .status(UserStatus.ACTIVE)
                .type(UserType.INDIVIDUAL)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByPhone(username)
                .orElseThrow(() -> new NotFoundException("Foydalanuvchi topilmadi"));

        if (!jwtService.isTokenValid(refreshToken, user) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Refresh token yaroqsiz");
        }

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}
