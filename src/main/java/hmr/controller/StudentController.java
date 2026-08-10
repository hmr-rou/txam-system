package hmr.controller;

import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import hmr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger LOG = Logger.getLogger(StudentController.class.getName());

    @Autowired
    private Cet4ScoreService cet4ScoreService;

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        try {
            model.addAttribute("scoreList", cet4ScoreService.findByIdCard(user.getIdCardNumber()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "查询学生成绩失败", e);
            model.addAttribute("error", "查询成绩失败，请稍后重试");
        }
        return "student_home";
    }

    @PostMapping("/query")
    public String query(HttpSession session, Model model) {
        return home(session, model);
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        try {
            boolean success = userService.changePassword(user.getIdCardNumber(), oldPassword, newPassword);
            ra.addFlashAttribute(success ? "message" : "error",
                    success ? "密码修改成功" : "原密码错误");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "学生修改密码失败", e);
            ra.addFlashAttribute("error", "修改失败，请稍后重试");
        }
        return "redirect:/student/home";
    }

    // ==================== REST API（供 Vue 前端调用） ====================

    @GetMapping("/api/student/scores")
    @ResponseBody
    public List<Cet4Score> apiListScores(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Collections.emptyList();
        }
        return cet4ScoreService.findByIdCard(user.getIdCardNumber());
    }
}
