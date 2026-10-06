package br.com.fiap.CrudApi.controller;

import br.com.fiap.CrudApi.dto.LoginRequest;
import br.com.fiap.CrudApi.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final JwtEncoder jwtEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthController(
    JwtEncoder jwtEncoder,
    AuthenticationManager authenticationManager
  ) {
    this.jwtEncoder = jwtEncoder;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest loginRequest) {
    var authentication = this.authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(
        loginRequest.email(),
        loginRequest.password()
      )
    );

    // JWT: Headers, Payload (claims) + sign

    var jwtHeader = JwsHeader.with(
      MacAlgorithm.HS256
    ).build();

    var now = Instant.now();

    var jwtClaims = JwtClaimsSet.builder()
      .subject(authentication.getName())
      .issuedAt(now)
      .expiresAt(now.plusSeconds(60 * 5))
      .build();

    var jwtToken = this.jwtEncoder.encode(
      JwtEncoderParameters.from(
        jwtHeader,
        jwtClaims
      )
    );

    return new LoginResponse(jwtToken.getTokenValue());
  }
}
