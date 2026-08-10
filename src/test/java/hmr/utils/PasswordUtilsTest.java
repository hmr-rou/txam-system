package hmr.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类单元测试
 */
class PasswordUtilsTest {

    @Test
    @DisplayName("生成盐 — 长度正确")
    void generateSalt_shouldReturnNonEmptyString() {
        String salt = PasswordUtils.generateSalt();
        assertNotNull(salt, "盐不能为 null");
        assertFalse(salt.isEmpty(), "盐不能为空");
        assertTrue(salt.length() >= 16, "Base64 编码后长度应 >= 16");
    }

    @Test
    @DisplayName("每次生成的盐应该不同")
    void generateSalt_shouldBeRandom() {
        String salt1 = PasswordUtils.generateSalt();
        String salt2 = PasswordUtils.generateSalt();
        assertNotEquals(salt1, salt2, "两次生成的盐应该不同");
    }

    @Test
    @DisplayName("相同密码+相同盐的哈希值一致")
    void hash_shouldBeDeterministic() {
        String password = "123456";
        String salt = "test-salt-value";
        String hash1 = PasswordUtils.hash(password, salt);
        String hash2 = PasswordUtils.hash(password, salt);
        assertEquals(hash1, hash2, "相同输入应产出相同哈希");
    }

    @Test
    @DisplayName("不同密码的哈希值不同")
    void hash_shouldDifferForDifferentPasswords() {
        String salt = "salt";
        String hash1 = PasswordUtils.hash("123456", salt);
        String hash2 = PasswordUtils.hash("654321", salt);
        assertNotEquals(hash1, hash2, "不同密码应产出不同哈希");
    }

    @Test
    @DisplayName("正确密码验证通过")
    void verify_shouldReturnTrue_forCorrectPassword() {
        String password = "123456";
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hash(password, salt);
        assertTrue(PasswordUtils.verify(password, salt, hash), "正确密码应该验证通过");
    }

    @Test
    @DisplayName("错误密码验证失败")
    void verify_shouldReturnFalse_forWrongPassword() {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hash("correct", salt);
        assertFalse(PasswordUtils.verify("wrong", salt, hash), "错误密码应该验证失败");
    }

    @Test
    @DisplayName("错误盐验证失败")
    void verify_shouldReturnFalse_forWrongSalt() {
        String password = "123456";
        String hash = PasswordUtils.hash(password, PasswordUtils.generateSalt());
        String wrongSalt = PasswordUtils.generateSalt();
        assertFalse(PasswordUtils.verify(password, wrongSalt, hash), "错误盐应该验证失败");
    }
}
