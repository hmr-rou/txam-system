package hmr.controller;

import hmr.javabean.User;
import hmr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class LoginController {

    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());

    @Autowired
    private UserService userService;

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, Model model) {
        try {
            User user = userService.login(username, password);
            if (user == null) {
                model.addAttribute("error", "身份证号或密码错误");
                return "login";
            }

            session.setAttribute("user", user);

            if ("admin".equals(user.getRole())) {
                return "redirect:/admin/home";
            } else {
                return "redirect:/student/home";
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "登录异常", e);
            model.addAttribute("error", "系统错误，请稍后重试");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ==================== REST API（供 Vue 前端调用） ====================

    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> apiLogin(@RequestParam String username,
                                        @RequestParam String password,
                                        HttpSession session) {
        try {
            User user = userService.login(username, password);
            if (user == null) {
                return Map.of("success", false, "message", "身份证号或密码错误");
            }
            session.setAttribute("user", user);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("name", user.getName());
            userMap.put("idCardNumber", user.getIdCardNumber());
            userMap.put("role", user.getRole());
            return Map.of("success", true, "user", userMap);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "登录异常", e);
            return Map.of("success", false, "message", "系统错误，请稍后重试");
        }
    }

    @GetMapping("/api/currentUser")
    @ResponseBody
    public Object currentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "未登录");
        }
        Map<String, Object> userMap = new java.util.HashMap<>();
        userMap.put("name", user.getName());
        userMap.put("idCardNumber", user.getIdCardNumber());
        userMap.put("role", user.getRole());
        return Map.of("success", true, "user", userMap);
    }
}
