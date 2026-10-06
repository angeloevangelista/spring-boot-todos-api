# Duvidas

- [Linkedin](https://linkedin.com/in/angelo-evangelista-5474a2177)

## Part 1

- [ ] Create USERS table
  ```sql
  CREATE TABLE USERS (
    ID INTEGER GENERATED ALWAYS AS IDENTITY (
      START WITH 1
      INCREMENT BY 1
    ) PRIMARY KEY,
    EMAIL VARCHAR2(255) UNIQUE NOT NULL,
    PASSWORD VARCHAR2(255) NOT NULL
  );
  ```
- [ ] Create User entity

  ```java
  @Entity
  @Table(name = "USERS")
  public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    public User() { }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }
  ```

- [ ] Create UserRepository + findByEmail(String)

  ```java
  public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
  }
  ```

- [ ] Create UserController (POST /user) and Service

  ```java
  // controller/UserController.java
  @RestController()
  @RequestMapping("/user")
  public class UserController {
    private final UserService userService;

    public UserController(
        UserService userService
    ) {
      this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody User user) {
      return ResponseEntity.status(HttpStatus.CREATED).body(
        this.userService.save(user)
      );
    }
  }

  // service/UserController.java
  @Service()
  public class UserService {
    private final UserRepository userRepository;

    public UserService(
        UserRepository userRepository
    ) {
      this.userRepository = userRepository;
    }

    public User save(User user) {
      User savedUser = this.userRepository.save(user);

      savedUser.setPassword(null);

      return savedUser;
    }
  }
  ```

- [ ] Add dependencies

  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>

  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
  </dependency>
  ```

- [ ] Configure `jwt.secret` at `application.properties`.
  - 256-bit secret
  - use `openssl rand -base64 32`
  - use https://toolfarm.io/en/openssl-rand-generator

- [ ] Create SecurityConfig

  ```java
  // config/SecurityConfig.java
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
  ```

- [ ] Update UserService to hash password

  ```java
  // service/UserController.java
  @Service()
  public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
      this.userRepository = userRepository;
      this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
      user.setPassword(
        this.passwordEncoder.encode(
          user.getPassword()
        )
      );

      User savedUser = this.userRepository.save(user);

      savedUser.setPassword(null);

      return savedUser;
    }
  }
  ```

- [ ] Create Login DTOs (LoginRequest, LoginResponse)

  ```java
  // dto/LoginRequest.java
  public record LoginRequest (String email, String password) {}

  // dto/LoginResponse.java
  public record LoginResponse(String token) {}
  ```

- [ ] Create AuthController with /auth/login

  ```java
  // controller/AuthController.java
  @RestController()
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
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
      var authentication = this.authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginRequest.email(),
              loginRequest.password()
          )
      );

      var jwtHeader = JwsHeader
          .with(MacAlgorithm.HS256)
          .build();

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

      return ResponseEntity.ok(
          new LoginResponse(jwtToken.getTokenValue())
      );
    }
  }
  ```

## Part 2

- [ ] Containerize app

  ```dockerfile
  FROM eclipse-temurin:21-jdk

  WORKDIR /app

  COPY . .

  RUN ./mvnw clean package -DskipTests

  RUN mv ./target/*.jar ./app.jar

  EXPOSE 8080

  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

## Part 3

- [ ] Push it to Github
- [ ] Deploy on render
