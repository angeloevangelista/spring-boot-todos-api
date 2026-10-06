package br.com.fiap.CrudApi.config;

import br.com.fiap.CrudApi.repository.UserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class SecurityConfig {

  // HTTP Request rules
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
      // Cross-Site Request Forgery rules: WRITE
      .csrf(csrf -> csrf.disable())

      // Cross-Origin Resource Sharing Rules: READ
      .cors(cors -> {
        cors.configurationSource(request -> {
          CorsConfiguration config = new CorsConfiguration();

          config.setAllowedOriginPatterns(List.of("*"));
          config.setAllowedMethods(List.of("*"));
          config.setAllowedHeaders(List.of("*"));

          return config;
        });
      })

      // Do not store authentication in an HTTP session
      .sessionManagement(session ->
        session.sessionCreationPolicy(
          SessionCreationPolicy.STATELESS
        )
      )

      // Define routes that require or not authentication
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/example").permitAll()
        .requestMatchers("/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/user").permitAll()

        .anyRequest().authenticated()
      )

      // Enables native Spring Security JWT support
      .oauth2ResourceServer(oauth2 ->
        oauth2.jwt(Customizer.withDefaults())
      )

      .build();
  }

  // "Teaches" Spring Security how to fetch a user
  @Bean
  UserDetailsService userDetailsService(UserRepository repository) {
    return email -> {
      var user = repository.findByEmail(email)
        .orElseThrow(() ->
          new UsernameNotFoundException(email)
        );

      return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getPassword())
        .authorities(List.of())
        .build();
    };
  }

  // Used when Spring needs to encode or verify a password
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // Authenticates the user's email and password using UserDetailsService and PasswordEncoder
  @Bean
  AuthenticationManager authenticationManager(
    AuthenticationConfiguration config
  ) throws Exception {
    return config.getAuthenticationManager();
  }

  private SecretKey secretKey(String secret) {
    return new SecretKeySpec(
      Base64.getDecoder().decode(secret),
      "HmacSHA256"
    );
  }

  // Generates and signs JWTs using the secret key
  @Bean
  JwtEncoder jwtEncoder(@Value("${jwt.secret}") String secret) {
    return new NimbusJwtEncoder(
      new ImmutableSecret<>(secretKey(secret))
    );
  }

  // Validates and decodes incoming JWTs using the secret key
  @Bean
  JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
    return NimbusJwtDecoder
      .withSecretKey(secretKey(secret))
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }
}
