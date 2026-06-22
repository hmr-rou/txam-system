package hmr.utils;

import hmr.javabean.Cet4Score;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 导入导出工具类
 */
public class ExcelUtil {

    // 表头列名
    private static final String[] HEADERS = {
            "姓名", "身份证号", "学校", "二级学院", "专业", "班级", "准考证号", "成绩", "考试时间"
    };

    /**
     * 从 Excel 输入流解析成绩列表
     *
     * @param inputStream Excel 文件输入流
     * @return 解析结果（包含成功列表和错误信息）
     */
    public static ParseResult parseExcel(InputStream inputStream) throws IOException {
        List<Cet4Score> scoreList = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                errors.add("Excel 文件中没有数据行（至少需要表头行和一行数据）");
                return new ParseResult(scoreList, errors);
            }

            // 读取表头行，验证列顺序
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("未找到表头行");
                return new ParseResult(scoreList, errors);
            }

            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 跳过全空行
                if (isRowEmpty(row)) continue;

                try {
                    Cet4Score score = new Cet4Score();
                    score.setName(getCellString(row, 0));
                    score.setIdCardNumber(getCellString(row, 1));
                    score.setSchool(getCellString(row, 2));
                    score.setCollege(getCellString(row, 3));
                    score.setMajor(getCellString(row, 4));
                    score.setClassName(getCellString(row, 5));
                    score.setAdmissionNo(getCellString(row, 6));

                    // 成绩：数字
                    String scoreStr = getCellString(row, 7);
                    if (scoreStr.isEmpty()) {
                        errors.add("第" + (i + 1) + "行：成绩为空");
                        continue;
                    }
                    double scoreValue;
                    try {
                        scoreValue = Double.parseDouble(scoreStr);
                    } catch (NumberFormatException e) {
                        errors.add("第" + (i + 1) + "行：成绩格式不正确（" + scoreStr + "）");
                        continue;
                    }
                    if (scoreValue < 0 || scoreValue > 710) {
                        errors.add("第" + (i + 1) + "行：成绩超出范围（0-710），当前值：" + scoreValue);
                        continue;
                    }
                    score.setScore(scoreValue);

                    // 考试时间：日期
                    String dateStr = getCellString(row, 8);
                    if (dateStr.isEmpty()) {
                        errors.add("第" + (i + 1) + "行：考试时间为空");
                        continue;
                    }
                    Date examTime = parseDate(dateStr);
                    if (examTime == null) {
                        errors.add("第" + (i + 1) + "行：考试时间格式不正确（" + dateStr + "），请使用 yyyy-MM-dd 格式");
                        continue;
                    }
                    score.setExamTime(examTime);

                    // 必填字段验证
                    if (score.getName().isEmpty()) {
                        errors.add("第" + (i + 1) + "行：姓名为空");
                        continue;
                    }
                    if (score.getIdCardNumber().isEmpty()) {
                        errors.add("第" + (i + 1) + "行：身份证号为空");
                        continue;
                    }

                    scoreList.add(score);
                } catch (Exception e) {
                    errors.add("第" + (i + 1) + "行：解析错误 - " + e.getMessage());
                }
            }
        }

        return new ParseResult(scoreList, errors);
    }

    /**
     * 将成绩列表写入 Excel 输出流
     */
    public static void exportToExcel(List<Cet4Score> scoreList, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("四级成绩");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256); // 设置列宽
            }
            // 特殊列宽
            sheet.setColumnWidth(1, 22 * 256); // 身份证号宽一些
            sheet.setColumnWidth(8, 14 * 256); // 考试时间

            // 创建日期格式化器
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            // 写入数据行
            for (int i = 0; i < scoreList.size(); i++) {
                Cet4Score s = scoreList.get(i);
                Row row = sheet.createRow(i + 1);

                createCell(row, 0, s.getName(), dataStyle);
                createCell(row, 1, s.getIdCardNumber(), dataStyle);
                createCell(row, 2, s.getSchool(), dataStyle);
                createCell(row, 3, s.getCollege(), dataStyle);
                createCell(row, 4, s.getMajor(), dataStyle);
                createCell(row, 5, s.getClassName(), dataStyle);
                createCell(row, 6, s.getAdmissionNo(), dataStyle);
                createCell(row, 7, String.valueOf(s.getScore()), dataStyle);
                createCell(row, 8, s.getExamTime() != null ? dateFormat.format(s.getExamTime()) : "", dataStyle);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 创建模板 Excel 并写入输出流（含表头 + 示例行）
     */
    public static void createTemplate(OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("四级成绩导入模板");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256);
            }
            sheet.setColumnWidth(1, 22 * 256);
            sheet.setColumnWidth(8, 14 * 256);

            // 添加一条示例数据
            Row exampleRow = sheet.createRow(1);
            CellStyle exampleStyle = workbook.createCellStyle();
            exampleStyle.setAlignment(HorizontalAlignment.CENTER);
            exampleRow.createCell(0).setCellValue("张三");
            exampleRow.createCell(1).setCellValue("110101199001011234");
            exampleRow.createCell(2).setCellValue("XX大学");
            exampleRow.createCell(3).setCellValue("计算机学院");
            exampleRow.createCell(4).setCellValue("软件工程");
            exampleRow.createCell(5).setCellValue("软件2101班");
            exampleRow.createCell(6).setCellValue("CET202406001");
            exampleRow.createCell(7).setCellValue("500");
            exampleRow.createCell(8).setCellValue("2024-06-15");
            for (int i = 0; i < HEADERS.length; i++) {
                exampleRow.getCell(i).setCellStyle(exampleStyle);
            }

            workbook.write(outputStream);
        }
    }

    // ---- 辅助方法 ----

    private static String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 避免科学计数法
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private static Date parseDate(String dateStr) {
        // 尝试多种日期格式
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyy年MM月dd日", "yyyyMMdd"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                java.util.Date parsed = sdf.parse(dateStr.trim());
                return new Date(parsed.getTime());
            } catch (Exception ignored) {
            }
        }
        // 如果是纯数字（Excel 日期序列号），尝试转换
        try {
            double dateSerial = Double.parseDouble(dateStr.trim());
            // Excel 日期从 1900-01-01 开始计数（有 1900 闰年 bug，偏移 -2 而非 -1）
            long millis = ((long) ((dateSerial - 25569) * 86400000L));
            return new Date(millis);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = 0; i < 9; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellString(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void createCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Excel 解析结果
     */
    public static class ParseResult {
        private final List<Cet4Score> scoreList;
        private final List<String> errors;

        public ParseResult(List<Cet4Score> scoreList, List<String> errors) {
            this.scoreList = scoreList;
            this.errors = errors;
        }

        public List<Cet4Score> getScoreList() {
            return scoreList;
        }

        public List<String> getErrors() {
            return errors;
        }

        public int getSuccessCount() {
            return scoreList.size();
        }

        public int getErrorCount() {
            return errors.size();
        }
    }
}
