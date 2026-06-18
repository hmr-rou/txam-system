package hmr.servlet;

import hmr.service.Cet4ScoreService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/deleteScore")
public class AdminDeleteScoreServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");

        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/home");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            cet4ScoreService.delete(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin/home");
    }
}