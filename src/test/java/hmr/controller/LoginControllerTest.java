package hmr.controller;

import hmr.javabean.User;
import hmr.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录控制器测试（Standalone MockMvc，不加载 Spring 上下文）
 */
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET /login — 返回登录页面")
    void loginPage_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET / — 根路径返回登录页面")
    void rootPath_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("POST /login — 密码错误返回 login 视图并携带 error")
    void login_shouldReturnLoginWithError_whenCredentialsWrong() throws Exception {
        when(userService.login("110101199001011234", "wrong"))
                .thenReturn(null);

        mockMvc.perform(post("/login")
                        .param("username", "110101199001011234")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("POST /login — 管理员登录重定向到 /admin/home")
    void login_shouldRedirectToAdmin_whenAdminLogin() throws Exception {
        User adminUser = new User();
        adminUser.setRole("admin");
        when(userService.login("admin", "pass")).thenReturn(adminUser);

        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/home"));
    }

    @Test
    @DisplayName("POST /login — 学生登录重定向到 /student/home")
    void login_shouldRedirectToStudent_whenStudentLogin() throws Exception {
        User studentUser = new User();
        studentUser.setRole("student");
        when(userService.login("student", "pass")).thenReturn(studentUser);

        mockMvc.perform(post("/login")
                        .param("username", "student")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/home"));
    }

    @Test
    @DisplayName("GET /logout — 退出后重定向到登录页")
    void logout_shouldInvalidateSessionAndRedirect() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
