package hmr.servlet;

import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import hmr.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/AdminSaveScoreServlet")
@MultipartConfig
public class AdminSaveScoreServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();
    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 权限检查
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "无权限操作");
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        String idStr = request.getParameter("id");
        String name = request.getParameter("name");
        String idCardNumber = request.getParameter("idCardNumber");
        String school = request.getParameter("school");
        String college = request.getParameter("college");
        String major = request.getParameter("major");
        String className = request.getParameter("className");
        String admissionNo = request.getParameter("admissionNo");
        String scoreStr = request.getParameter("score");
        String examTimeStr = request.getParameter("examTime");

        Map<String, Object> result = new HashMap<>();

        // 服务端必填校验
        if (isBlank(name) || isBlank(idCardNumber) || isBlank(school) || isBlank(college)
                || isBlank(major) || isBlank(className) || isBlank(admissionNo)
                || isBlank(scoreStr) || isBlank(examTimeStr)) {
            result.put("success", false);
            result.put("message", "所有字段均为必填项");
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        // 成绩数值校验
        double scoreValue;
        try {
            scoreValue = Double.parseDouble(scoreStr);
            if (scoreValue < 0 || scoreValue > 710) {
                result.put("success", false);
                result.put("message", "成绩必须在 0-710 之间");
                new ObjectMapper().writeValue(response.getWriter(), result);
                return;
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "成绩格式不正确");
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        // 日期格式校验
        Date examTime;
        try {
            examTime = Date.valueOf(examTimeStr);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "考试时间格式不正确");
            new ObjectMapper().writeValue(response.getWriter(), result);
            return;
        }

        try {
            Cet4Score score = new Cet4Score();
            score.setName(name.trim());
            score.setIdCardNumber(idCardNumber.trim());
            score.setSchool(school.trim());
            score.setCollege(college.trim());
            score.setMajor(major.trim());
            score.setClassName(className.trim());
            score.setAdmissionNo(admissionNo.trim());
            score.setScore(scoreValue);
            score.setExamTime(examTime);

            boolean success;
            boolean isNew = false;
            if (idStr != null && !idStr.trim().isEmpty()) {
                int id;
                try {
                    id = Integer.parseInt(idStr.trim());
                } catch (NumberFormatException e) {
                    result.put("success", false);
                    result.put("message", "ID 格式不正确");
                    new ObjectMapper().writeValue(response.getWriter(), result);
                    return;
                }
                score.setId(id);
                success = cet4ScoreService.update(score);
            } else {
                success = cet4ScoreService.add(score);
                isNew = true;
            }

            // 新增成绩时，同步人员信息到 user 表（默认密码 123456，角色 student）
            if (success && isNew) {
                try {
                    userService.syncUser(name.trim(), idCardNumber.trim());
                } catch (SQLException e) {
                    e.printStackTrace();
                    // 用户同步失败不影响成绩保存结果
                }
            }

            result.put("success", success);
            result.put("message", success ? "保存成功" : "保存失败");
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "数据库错误：" + e.getMessage());
        }

        new ObjectMapper().writeValue(response.getWriter(), result);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
