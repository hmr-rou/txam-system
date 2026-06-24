package hmr.servlet;

import hmr.javabean.User;
import hmr.service.UserService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

//学生修改密码 → 重定向到主页
@WebServlet("/StudentChangePasswordServlet")
public class StudentChangePasswordServlet extends HttpServlet {

    private UserService userService = new UserService();

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

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        try {
            boolean success = userService.changePassword(user.getIdCardNumber(), oldPassword, newPassword);
            if (success) {
                session.setAttribute("message", "密码修改成功");
            } else {
                session.setAttribute("error", "原密码错误");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("error", "修改失败，请稍后重试");
        }

        // 重新查询成绩
        response.sendRedirect(request.getContextPath() + "/StudentHomeServlet");
    }
}