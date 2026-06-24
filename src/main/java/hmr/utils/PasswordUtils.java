package hmr.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
// 密码加密工具
/**
 * 密码工具类 — 使用 SHA-256 + 随机盐进行哈希
 */
public class PasswordUtils {

    private static final int SALT_LENGTH = 16; // 盐长度（字节）

    /**
     * 生成随机盐（Base64 编码）
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 对密码 + 盐进行 SHA-256 哈希
     * @param password 明文密码
     * @param salt     盐（Base64 编码）
     * @return 哈希后的十六进制字符串
     */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 将盐和密码混合
            md.update(salt.getBytes());
            byte[] hashed = md.digest(password.getBytes());
            // 将字节数组转为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    /**
     * 验证密码是否匹配
     * @param password     待验证的明文密码
     * @param salt         存储的盐
     * @param storedHash   存储的哈希值
     * @return 是否匹配
     */
    public static boolean verify(String password, String salt, String storedHash) {
        // 1. 用相同的盐对用户输入的密码进行哈希
        String computed = hash(password, salt);
        // 2. 比较计算出的哈希值和存储的哈希值是否一致
        return computed.equals(storedHash);
    }
}
