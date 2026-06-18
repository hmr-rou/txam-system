package hmr.servlet;

import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/StudentQueryScoreServlet")
public class StudentQueryScoreServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            request.setCharacterEncoding("UTF-8");

            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");

            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            String idCardNumber = request.getParameter("idCardNumber");

            if (idCardNumber == null || idCardNumber.trim().isEmpty()) {
                request.setAttribute("error", "请输入身份证号");
                request.getRequestDispatcher("/student_home.jsp").forward(request, response);
                return;
            }

            try {
                List<Cet4Score> scoreList = cet4ScoreService.findByIdCard(idCardNumber);
                request.setAttribute("scoreList", scoreList);
                request.getRequestDispatcher("/student_home.jsp").forward(request, response);
            } catch (SQLException e) {
                e.printStackTrace();
                request.setAttribute("error", "查询失败，请稍后重试");
                request.getRequestDispatcher("/student_home.jsp").forward(request, response);
            }
        }
}
