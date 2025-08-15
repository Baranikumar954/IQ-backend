package com.example.IntelliQuestBackend.controller;

import com.example.IntelliQuestBackend.services.UserService;
import com.example.IntelliQuestBackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(value = "https://iq-frontend-swart.vercel.app",allowCredentials = "true")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/user/register")
    public ResponseEntity<String> register(@RequestBody Map<String,String> body){
        String email = body.get("email");
        String password = body.get("password");
        return ResponseEntity.ok(userService.register(email,password));
    }

    @GetMapping("/user/verify")
    public ResponseEntity<String> verify(@RequestParam String email){
        return ResponseEntity.ok(userService.verify(email));
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body){
        String email = body.get("email");
        String password = body.get("password");
        System.out.println("Lgoin verification");
        if(userService.login(email,password)){
            String token = jwtUtil.generateToken(email);
            System.out.println("My token for login : "+token);
            return ResponseEntity.ok(Map.of("message", "Login success", "token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials or not verified");
    }

    @PostMapping("/user/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String,String> body){
        String email = body.get("email");
        String name = body.get("name");
        userService.googleLogin(email, name);
        String token = jwtUtil.generateToken(email);
        return ResponseEntity.ok(Map.of("message", "Google login success", "token", token));
    }
    @PostMapping("/user/logout")
    public ResponseEntity<String> logout() {
        // Logout is handled on frontend by clearing the token.
        return ResponseEntity.ok("Logout success");
    }
}
