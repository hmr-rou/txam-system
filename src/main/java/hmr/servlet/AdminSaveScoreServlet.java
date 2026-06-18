package hmr.servlet;

import hmr.javabean.Cet4Score;
import hmr.service.Cet4ScoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin/saveScore")
public class AdminSaveScoreServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

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

        try {
            Cet4Score score = new Cet4Score();
            score.setName(name);
            score.setIdCardNumber(idCardNumber);
            score.setSchool(school);
            score.setCollege(college);
            score.setMajor(major);
            score.setClassName(className);
            score.setAdmissionNo(admissionNo);
            score.setScore(Double.parseDouble(scoreStr));
            score.setExamTime(Date.valueOf(examTimeStr));

            boolean success;
            if (idStr != null && !idStr.isEmpty()) {
                score.setId(Integer.parseInt(idStr));
                success = cet4ScoreService.update(score);
            } else {
                success = cet4ScoreService.add(score);
            }

            result.put("success", success);
            result.put("message", success ? "保存成功" : "保存失败");
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "数据库错误：" + e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), result);
    }
}
