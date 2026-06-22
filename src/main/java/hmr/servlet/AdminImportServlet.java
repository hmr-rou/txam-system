package hmr.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import hmr.service.UserService;
import hmr.utils.ExcelUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量导入成绩（Excel）
 */
@WebServlet("/AdminImportServlet")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,      // 最大 10MB
        maxRequestSize = 50 * 1024 * 1024     // 最大 50MB
)
public class AdminImportServlet extends HttpServlet {

    private Cet4ScoreService cet4ScoreService = new Cet4ScoreService();
    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        // 权限检查
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "无权限操作");
            mapper.writeValue(response.getWriter(), result);
            return;
        }

        // 获取上传的文件
        Part filePart = request.getPart("excelFile");
        if (filePart == null || filePart.getSize() == 0) {
            result.put("success", false);
            result.put("message", "请选择要上传的 Excel 文件");
            mapper.writeValue(response.getWriter(), result);
            return;
        }

        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            result.put("success", false);
            result.put("message", "请上传 .xlsx 或 .xls 格式的 Excel 文件");
            mapper.writeValue(response.getWriter(), result);
            return;
        }

        try (InputStream inputStream = filePart.getInputStream()) {
            // 解析 Excel
            ExcelUtil.ParseResult parseResult = ExcelUtil.parseExcel(inputStream);
            List<Cet4Score> scoreList = parseResult.getScoreList();
            List<String> errors = parseResult.getErrors();

            if (scoreList.isEmpty() && !errors.isEmpty()) {
                result.put("success", false);
                result.put("message", "Excel 文件中没有有效数据");
                result.put("errors", errors);
                mapper.writeValue(response.getWriter(), result);
                return;
            }

            // 批量写入数据库
            int insertedCount = 0;
            int dbErrorCount = 0;
            try {
                insertedCount = cet4ScoreService.batchAdd(scoreList);

                // 同步用户：为每个导入的学生创建账号
                int userSynced = 0;
                for (Cet4Score score : scoreList) {
                    try {
                        userService.syncUser(score.getName(), score.getIdCardNumber());
                        userSynced++;
                    } catch (SQLException e) {
                        // 用户同步失败不影响整体结果
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                result.put("success", false);
                result.put("message", "数据库写入失败：" + e.getMessage());
                mapper.writeValue(response.getWriter(), result);
                return;
            }

            // 构建结果消息
            StringBuilder msg = new StringBuilder();
            msg.append("成功导入 ").append(insertedCount).append(" 条记录");
            if (!errors.isEmpty()) {
                msg.append("，").append(errors.size()).append(" 行数据存在格式问题被跳过");
            }

            result.put("success", true);
            result.put("message", msg.toString());
            result.put("insertedCount", insertedCount);
            result.put("errorCount", errors.size());
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }

        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件读取失败：" + e.getMessage());
        }

        mapper.writeValue(response.getWriter(), result);
    }
}
