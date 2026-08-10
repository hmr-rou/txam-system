package hmr.service;

import hmr.javabean.User;
import hmr.mapper.UserMapper;
import hmr.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务层单元测试
 * 使用真实的 PasswordUtils（纯计算，无外部依赖），只 mock 数据库层
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String correctPassword = "123456";
    private final String wrongPassword = "wrong-password";

    @BeforeEach
    void setUp() {
        // 用真实 PasswordUtils 生成测试用户（哈希密码）
        String salt = PasswordUtils.generateSalt();
        String hashed = PasswordUtils.hash(correctPassword, salt);

        testUser = new User();
        testUser.setId(1);
        testUser.setName("张三");
        testUser.setIdCardNumber("110101199001011234");
        testUser.setPassword(hashed);
        testUser.setSalt(salt);
        testUser.setRole("student");
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("登录 — 凭据正确应返回用户")
    void login_shouldReturnUser_whenCredentialsCorrect() {
        when(userMapper.selectByIdCardWithSalt("110101199001011234")).thenReturn(testUser);

        User result = userService.login("110101199001011234", correctPassword);
        assertNotNull(result);
        assertEquals("张三", result.getName());
    }

    @Test
    @DisplayName("登录 — 密码错误返回 null")
    void login_shouldReturnNull_whenPasswordWrong() {
        when(userMapper.selectByIdCardWithSalt("110101199001011234")).thenReturn(testUser);

        User result = userService.login("110101199001011234", wrongPassword);
        assertNull(result);
    }

    @Test
    @DisplayName("登录 — 用户不存在返回 null")
    void login_shouldReturnNull_whenUserNotFound() {
        when(userMapper.selectByIdCardWithSalt("nonexistent")).thenReturn(null);
        when(userMapper.selectByIdCardNoSalt("nonexistent")).thenReturn(null);

        User result = userService.login("nonexistent", correctPassword);
        assertNull(result);
    }

    @Test
    @DisplayName("登录 — 盐列不存在时降级到无盐查询（明文比对）")
    void login_shouldFallbackToNoSalt_whenSaltColumnMissing() {
        when(userMapper.selectByIdCardWithSalt("110101199001011234")).thenReturn(null);

        User noSaltUser = new User();
        noSaltUser.setId(1);
        noSaltUser.setName("张三");
        noSaltUser.setPassword("plaintext-password");
        noSaltUser.setSalt(null);
        when(userMapper.selectByIdCardNoSalt("110101199001011234")).thenReturn(noSaltUser);

        User result = userService.login("110101199001011234", "plaintext-password");
        assertNotNull(result);
    }

    // ==================== 修改密码测试 ====================

    @Test
    @DisplayName("修改密码 — 原密码正确时更新成功")
    void changePassword_shouldReturnTrue_whenOldPasswordCorrect() {
        when(userMapper.selectByIdCardWithSalt("110101199001011234")).thenReturn(testUser);
        when(userMapper.updatePasswordWithSalt(eq("110101199001011234"), anyString(), anyString())).thenReturn(1);

        boolean result = userService.changePassword("110101199001011234", correctPassword, "newpass");
        assertTrue(result);
        verify(userMapper).updatePasswordWithSalt(eq("110101199001011234"), anyString(), anyString());
    }

    @Test
    @DisplayName("修改密码 — 原密码错误返回 false")
    void changePassword_shouldReturnFalse_whenOldPasswordWrong() {
        when(userMapper.selectByIdCardWithSalt("110101199001011234")).thenReturn(testUser);

        boolean result = userService.changePassword("110101199001011234", wrongPassword, "newpass");
        assertFalse(result);
        verify(userMapper, never()).updatePasswordWithSalt(anyString(), anyString(), anyString());
    }
}
