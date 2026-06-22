package hmr.dao;

import hmr.javabean.Cet4Score;
import hmr.utils.C3p0Utils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Cet4ScoreDao {

    private QueryRunner runner = new QueryRunner(C3p0Utils.getDataSource());

    // 查询所有成绩
    public List<Cet4Score> findAll() throws SQLException {
        String sql = "SELECT id, name, school, college, major, " +
                "class_name AS className, " +
                "id_card_number AS idCardNumber, " +
                "admission_no AS admissionNo, " +
                "score, exam_time AS examTime " +
                "FROM cet4_score ORDER BY exam_time DESC, id DESC";
        return runner.query(sql, new BeanListHandler<>(Cet4Score.class));
    }

    // 根据身份证号查询
    public List<Cet4Score> findByIdCard(String idCardNumber) throws SQLException {
        String sql = "SELECT id, name, school, college, major, " +
                "class_name AS className, " +
                "id_card_number AS idCardNumber, " +
                "admission_no AS admissionNo, " +
                "score, exam_time AS examTime " +
                "FROM cet4_score WHERE id_card_number = ? ORDER BY exam_time DESC";
        return runner.query(sql, new BeanListHandler<>(Cet4Score.class), idCardNumber);
    }

    // 根据ID查询
    public Cet4Score findById(int id) throws SQLException {
        String sql = "SELECT id, name, school, college, major, " +
                "class_name AS className, " +
                "id_card_number AS idCardNumber, " +
                "admission_no AS admissionNo, " +
                "score, exam_time AS examTime " +
                "FROM cet4_score WHERE id = ?";
        return runner.query(sql, new BeanHandler<>(Cet4Score.class), id);
    }

    // 多条件查询
    public List<Cet4Score> findByCondition(String idCard, String admissionNo, String school,
                                           String college, String major, String className) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, school, college, major, " +
                        "class_name AS className, " +
                        "id_card_number AS idCardNumber, " +
                        "admission_no AS admissionNo, " +
                        "score, exam_time AS examTime " +
                        "FROM cet4_score WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (idCard != null && !idCard.trim().isEmpty()) {
            sql.append(" AND id_card_number LIKE ?");
            params.add("%" + idCard + "%");
        }
        if (admissionNo != null && !admissionNo.trim().isEmpty()) {
            sql.append(" AND admission_no LIKE ?");
            params.add("%" + admissionNo + "%");
        }
        if (school != null && !school.trim().isEmpty()) {
            sql.append(" AND school LIKE ?");
            params.add("%" + school + "%");
        }
        if (college != null && !college.trim().isEmpty()) {
            sql.append(" AND college LIKE ?");
            params.add("%" + college + "%");
        }
        if (major != null && !major.trim().isEmpty()) {
            sql.append(" AND major LIKE ?");
            params.add("%" + major + "%");
        }
        if (className != null && !className.trim().isEmpty()) {
            sql.append(" AND class_name LIKE ?");
            params.add("%" + className + "%");
        }
        sql.append(" ORDER BY exam_time DESC");

        return runner.query(sql.toString(), new BeanListHandler<>(Cet4Score.class), params.toArray());
    }

    // 添加成绩
    public boolean add(Cet4Score score) throws SQLException {
        String sql = "INSERT INTO cet4_score (name, school, college, major, class_name, id_card_number, admission_no, score, exam_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int rows = runner.update(sql, score.getName(), score.getSchool(), score.getCollege(),
                score.getMajor(), score.getClassName(), score.getIdCardNumber(),
                score.getAdmissionNo(), score.getScore(), score.getExamTime());
        return rows > 0;
    }

    // 修改成绩
    public boolean update(Cet4Score score) throws SQLException {
        String sql = "UPDATE cet4_score SET name=?, school=?, college=?, major=?, class_name=?, id_card_number=?, admission_no=?, score=?, exam_time=? WHERE id=?";
        int rows = runner.update(sql, score.getName(), score.getSchool(), score.getCollege(),
                score.getMajor(), score.getClassName(), score.getIdCardNumber(),
                score.getAdmissionNo(), score.getScore(), score.getExamTime(), score.getId());
        return rows > 0;
    }

    // 删除成绩
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM cet4_score WHERE id = ?";
        int rows = runner.update(sql, id);
        return rows > 0;
    }

    // 批量导入成绩
    public int batchAdd(List<Cet4Score> scoreList) throws SQLException {
        String sql = "INSERT INTO cet4_score (name, school, college, major, class_name, id_card_number, admission_no, score, exam_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int count = 0;
        for (Cet4Score score : scoreList) {
            int rows = runner.update(sql, score.getName(), score.getSchool(), score.getCollege(),
                    score.getMajor(), score.getClassName(), score.getIdCardNumber(),
                    score.getAdmissionNo(), score.getScore(), score.getExamTime());
            if (rows > 0) count++;
        }
        return count;
    }
}