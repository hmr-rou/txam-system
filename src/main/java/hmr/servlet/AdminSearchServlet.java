package hmr.servlet;

import hmr.javabean.Cet4Score;
import hmr.service.Cet4ScoreService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/search")
public class AdminSearchServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String idCard = request.getParameter("idCard");
        String admissionNo = request.getParameter("admissionNo");
        String school = request.getParameter("school");
        String college = request.getParameter("college");
        String major = request.getParameter("major");
        String className = request.getParameter("className");

        try {
            List<Cet4Score> scoreList = cet4ScoreService.findByCondition(idCard, admissionNo,
                    school, college, major, className);
            request.setAttribute("scoreList", scoreList);
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "查询失败");
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        }
    }
}