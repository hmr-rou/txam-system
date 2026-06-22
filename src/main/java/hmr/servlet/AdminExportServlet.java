package hmr.servlet;

import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import hmr.utils.ExcelUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 批量导出成绩（Excel）
 * 支持按学校、二级学院、专业、班级等条件筛选导出
 */
@WebServlet("/AdminExportServlet")
public class AdminExportServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 权限检查
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 读取查询条件（与 AdminSearchServlet 一致）
        String idCard = request.getParameter("idCard");
        String admissionNo = request.getParameter("admissionNo");
        String school = request.getParameter("school");
        String college = request.getParameter("college");
        String major = request.getParameter("major");
        String className = request.getParameter("className");

        try {
            List<Cet4Score> scoreList;
            // 如果没有任何查询条件，导出全部
            if (allBlank(idCard, admissionNo, school, college, major, className)) {
                scoreList = cet4ScoreService.findAll();
            } else {
                scoreList = cet4ScoreService.findByCondition(idCard, admissionNo, school, college, major, className);
            }

            // 生成文件名
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "四级成绩导出_" + timestamp + ".xlsx";

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            // 写入 Excel
            ExcelUtil.exportToExcel(scoreList, response.getOutputStream());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<script>alert('导出失败：数据库查询错误'); history.back();</script>");
        } catch (IOException e) {
            e.printStackTrace();
            // 如果输出流已开始写入，则无法再发送错误页面，仅记录日志
        }
    }

    private boolean allBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
