package br.com.devquote.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordTestUtil {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Senha que deveria estar no banco
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXKxXBRFU2PkxKP9BjBU3nQx7zO";
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println("Matches: " + passwordEncoder.matches(rawPassword, encodedPassword));
        
        // Gerar novo hash para conferir
        String newHash = passwordEncoder.encode(rawPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + passwordEncoder.matches(rawPassword, newHash));
    }
}