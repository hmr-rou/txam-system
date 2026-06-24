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

//加载所有成绩 → 转发到 admin_home.jsp
@WebServlet("/AdminHomeServlet")
public class AdminHomeServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // ========== 1. 权限检查 ==========
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        // ========== 2. 查询数据 ==========
        try {
            List<Cet4Score> scoreList = cet4ScoreService.findAll();
            // ========== 3. 存储数据到 request ==========
            request.setAttribute("scoreList", scoreList);
            // ========== 4. 转发到 JSP 页面 ==========
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "获取数据失败");
            request.getRequestDispatcher("/admin_home.jsp").forward(request, response);
        }
    }
}