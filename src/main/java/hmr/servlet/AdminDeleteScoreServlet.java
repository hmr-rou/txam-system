package hmr.servlet;

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

@WebServlet("/AdminDeleteScoreServlet")
public class AdminDeleteScoreServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String idStr = request.getParameter("id");

        if (idStr == null || idStr.trim().isEmpty()) {
            session.setAttribute("message", "缺少要删除的记录 ID");
            response.sendRedirect(request.getContextPath() + "/AdminHomeServlet");
            return;
        }

        try {
            int id = Integer.parseInt(idStr.trim());
            boolean success = cet4ScoreService.delete(id);
            if (success) {
                session.setAttribute("message", "删除成功");
            } else {
                session.setAttribute("error", "删除失败：记录可能不存在");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("error", "ID 格式不正确");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("error", "删除失败，请稍后重试");
        }

        response.sendRedirect(request.getContextPath() + "/AdminHomeServlet");
    }
}