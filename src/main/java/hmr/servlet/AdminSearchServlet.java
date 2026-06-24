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
// 多条件查询 → 转发到 admin_home.jsp 显示结果
@WebServlet("/AdminSearchServlet")
public class AdminSearchServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 1. 权限检查
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }


        // 2. 获取查询参数
        String idCard = request.getParameter("idCard");
        String admissionNo = request.getParameter("admissionNo");
        String school = request.getParameter("school");
        String college = request.getParameter("college");
        String major = request.getParameter("major");
        String className = request.getParameter("className");
        // 3. 执行查询
        try {
            List<Cet4Score> scoreList = cet4ScoreService.findByCondition(idCard, admissionNo,
                    school, college, major, className);
            // 4. 返回结果
            request.setAttribute("scoreList", scoreList);
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "查询失败");
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        }
    }
}