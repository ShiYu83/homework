import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBCryptPasswords {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // 原始密码
        String adminRawPassword = "123456";
        String doctorRawPassword = "123456";
        
        // 生成BCrypt密码
        String adminEncryptedPassword = passwordEncoder.encode(adminRawPassword);
        String doctorEncryptedPassword = passwordEncoder.encode(doctorRawPassword);
        
        System.out.println("Admin BCrypt Password: " + adminEncryptedPassword);
        System.out.println("Doctor BCrypt Password: " + doctorEncryptedPassword);
    }
}