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

//接收请求
//接收用户名密码 → 调用 UserService 验证 → 存入 session → 根据角色跳转
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    // 创建 UserService 实例（用于调用业务逻辑）
    private UserService userService = new UserService();
    // 处理 POST 请求（因为登录表单 method="post"）
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); //步骤 1：设置请求编码（防止中文乱码）
        // 步骤 2：从请求中获取用户输入的参数
        // 参数名必须与 login.jsp 中 input 的 name 属性一致
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //步骤 3：调用 Service 层进行登录验证
        try {
            User user = userService.login(username, password);
            //步骤 4：判断验证结果
            if (user == null) {
                // 登录失败：将错误信息存入 request，转发回登录页
                request.setAttribute("error", "身份证号或密码错误");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;// 终止执行
            }
            //步骤 5：登录成功，将用户信息存入 Session
            HttpSession session = request.getSession();// 获取/创建 Session
            session.setAttribute("user", user);// 存入用户对象
            //步骤 6：根据角色跳转不同页面
            if ("admin".equals(user.getRole())) {
                response.sendRedirect(request.getContextPath() + "/AdminHomeServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/StudentHomeServlet");
            }
        } catch (SQLException e) {
            // 数据库异常处理
            e.printStackTrace();
            request.setAttribute("error", "系统错误，请稍后重试");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}