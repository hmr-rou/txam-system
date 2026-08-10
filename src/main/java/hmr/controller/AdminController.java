package hmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hmr.javabean.Cet4Score;
import hmr.javabean.User;
import hmr.service.Cet4ScoreService;
import hmr.service.UserService;
import hmr.utils.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger LOG = Logger.getLogger(AdminController.class.getName());

    @Autowired
    private Cet4ScoreService cet4ScoreService;

    @Autowired
    private UserService userService;

    // ==================== 主页 ====================

    @GetMapping("/home")
    public String home(Model model) {
        try {
            model.addAttribute("scoreList", cet4ScoreService.findAll());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "查询成绩列表失败", e);
            model.addAttribute("error", "获取数据失败");
        }
        return "admin_home";
    }

    // ==================== 多条件查询 ====================

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String idCard,
                         @RequestParam(required = false) String admissionNo,
                         @RequestParam(required = false) String school,
                         @RequestParam(required = false) String college,
                         @RequestParam(required = false) String major,
                         @RequestParam(required = false) String className,
                         Model model) {
        try {
            model.addAttribute("scoreList",
                    cet4ScoreService.findByCondition(idCard, admissionNo, school, college, major, className));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "多条件查询失败", e);
            model.addAttribute("error", "查询失败");
        }
        return "admin_home";
    }

    // ==================== 添加/修改成绩 ====================

    @PostMapping("/save")
    @ResponseBody
    public Map<String, Object> save(@RequestParam(required = false) String id,
                                     @RequestParam String name,
                                     @RequestParam String idCardNumber,
                                     @RequestParam String school,
                                     @RequestParam String college,
                                     @RequestParam String major,
                                     @RequestParam String className,
                                     @RequestParam String admissionNo,
                                     @RequestParam String score,
                                     @RequestParam String examTime) {
        // 必填校验
        if (isAnyBlank(name, idCardNumber, school, college, major, className, admissionNo, score, examTime)) {
            return Map.of("success", false, "message", "所有字段均为必填项");
        }

        // 成绩校验
        double scoreValue;
        try {
            scoreValue = Double.parseDouble(score);
            if (scoreValue < 0 || scoreValue > 710) {
                return Map.of("success", false, "message", "成绩必须在 0-710 之间");
            }
        } catch (NumberFormatException e) {
            return Map.of("success", false, "message", "成绩格式不正确");
        }

        // 日期校验
        Date examDate;
        try {
            examDate = Date.valueOf(examTime);
        } catch (IllegalArgumentException e) {
            return Map.of("success", false, "message", "考试时间格式不正确");
        }

        try {
            Cet4Score s = new Cet4Score();
            s.setName(name.trim());
            s.setIdCardNumber(idCardNumber.trim());
            s.setSchool(school.trim());
            s.setCollege(college.trim());
            s.setMajor(major.trim());
            s.setClassName(className.trim());
            s.setAdmissionNo(admissionNo.trim());
            s.setScore(scoreValue);
            s.setExamTime(examDate);

            boolean success;
            boolean isNew = false;

            if (id != null && !id.trim().isEmpty()) {
                s.setId(Integer.parseInt(id.trim()));
                success = cet4ScoreService.update(s);
            } else {
                success = cet4ScoreService.add(s);
                isNew = true;
            }

            if (success && isNew) {
                try {
                    userService.syncUser(name.trim(), idCardNumber.trim());
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "用户同步失败: " + idCardNumber, e);
                }
            }

            return Map.of("success", success, "message", success ? "保存成功" : "保存失败");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "保存成绩失败", e);
            return Map.of("success", false, "message", "数据库错误：" + e.getMessage());
        }
    }

    // ==================== 获取单条成绩 (JSON) ====================

    @GetMapping("/getScore")
    @ResponseBody
    public Object getScore(@RequestParam int id) {
        try {
            return cet4ScoreService.findById(id);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "查询成绩失败 id=" + id, e);
            return Map.of("error", "查询失败");
        }
    }

    // ==================== 删除成绩 ====================

    @GetMapping("/delete")
    public String delete(@RequestParam int id, RedirectAttributes ra) {
        try {
            boolean success = cet4ScoreService.delete(id);
            ra.addFlashAttribute(success ? "message" : "error",
                    success ? "删除成功" : "删除失败：记录可能不存在");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "删除成绩失败 id=" + id, e);
            ra.addFlashAttribute("error", "删除失败，请稍后重试");
        }
        return "redirect:/admin/home";
    }

    // ==================== 批量导入 ====================

    @PostMapping("/import")
    @ResponseBody
    public Map<String, Object> importExcel(@RequestParam("excelFile") MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("success", false, "message", "请选择要上传的 Excel 文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return Map.of("success", false, "message", "请上传 .xlsx 或 .xls 格式的 Excel 文件");
        }

        try (InputStream inputStream = file.getInputStream()) {
            ExcelUtil.ParseResult parseResult = ExcelUtil.parseExcel(inputStream);
            List<Cet4Score> scoreList = parseResult.getScoreList();
            List<String> errors = parseResult.getErrors();

            if (scoreList.isEmpty() && !errors.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "Excel 文件中没有有效数据");
                result.put("errors", errors);
                return result;
            }

            int insertedCount = cet4ScoreService.batchAdd(scoreList);

            // 同步用户账号
            for (Cet4Score score : scoreList) {
                try {
                    userService.syncUser(score.getName(), score.getIdCardNumber());
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "用户同步失败: " + score.getIdCardNumber(), e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "成功导入 " + insertedCount + " 条记录"
                    + (errors.isEmpty() ? "" : "，" + errors.size() + " 行数据存在格式问题被跳过"));
            result.put("insertedCount", insertedCount);
            result.put("errorCount", errors.size());
            if (!errors.isEmpty()) {
                result.put("errors", errors);
            }
            return result;

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "批量导入失败", e);
            return Map.of("success", false, "message", "导入失败：" + e.getMessage());
        }
    }

    // ==================== 导出 Excel ====================

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String idCard,
                       @RequestParam(required = false) String admissionNo,
                       @RequestParam(required = false) String school,
                       @RequestParam(required = false) String college,
                       @RequestParam(required = false) String major,
                       @RequestParam(required = false) String className,
                       HttpServletResponse response) throws IOException {
        try {
            List<Cet4Score> scoreList;
            if (allBlank(idCard, admissionNo, school, college, major, className)) {
                scoreList = cet4ScoreService.findAll();
            } else {
                scoreList = cet4ScoreService.findByCondition(idCard, admissionNo, school, college, major, className);
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String fileName = "四级成绩导出_" + timestamp + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

            ExcelUtil.exportToExcel(scoreList, response.getOutputStream());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "导出失败", e);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<script>alert('导出失败：数据库查询错误'); history.back();</script>");
        }
    }

    // ==================== 下载模板 ====================

    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        String fileName = "四级成绩导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        ExcelUtil.createTemplate(response.getOutputStream());
    }

    // ==================== 修改密码 ====================

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        try {
            boolean success = userService.changePassword(user.getIdCardNumber(), oldPassword, newPassword);
            ra.addFlashAttribute(success ? "message" : "error",
                    success ? "密码修改成功" : "原密码错误");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "管理员修改密码失败", e);
            ra.addFlashAttribute("error", "修改失败，请稍后重试");
        }
        return "redirect:/admin/home";
    }

    // ==================== 辅助方法 ====================

    private boolean isAnyBlank(String... values) {
        for (String v : values) {
            if (v == null || v.trim().isEmpty()) return true;
        }
        return false;
    }

    private boolean allBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return false;
        }
        return true;
    }

    // ==================== REST API（供 Vue 前端调用） ====================

    @GetMapping("/api/admin/scores")
    @ResponseBody
    public List<Cet4Score> apiListScores() {
        return cet4ScoreService.findAll();
    }

    @GetMapping("/api/admin/scores/search")
    @ResponseBody
    public List<Cet4Score> apiSearchScores(
            @RequestParam(required = false) String idCard,
            @RequestParam(required = false) String admissionNo,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className) {
        return cet4ScoreService.findByCondition(idCard, admissionNo, school, college, major, className);
    }
}
