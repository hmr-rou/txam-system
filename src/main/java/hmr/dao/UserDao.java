package hmr.dao;

import hmr.javabean.User;
import hmr.utils.C3p0Utils;
import hmr.utils.PasswordUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;

public class UserDao {

    private QueryRunner runner = new QueryRunner(C3p0Utils.getDataSource());

    // 登录验证（使用 SHA-256 + 盐哈希验证，兼容无 salt 列的旧表）
    public User login(String idCardNumber, String password) throws SQLException {
        User user = null;
        // 先尝试查询含 salt 列（明确的列别名确保正确映射）
        try {
            String sql = "SELECT id, id_card_number AS idCardNumber, name, password, salt, role FROM user WHERE id_card_number = ?";
            user = runner.query(sql, new BeanHandler<>(User.class), idCardNumber);
        } catch (SQLException e) {
            // salt 列不存在，回退为不查 salt
            String sql = "SELECT id, id_card_number AS idCardNumber, name, password, role FROM user WHERE id_card_number = ?";
            user = runner.query(sql, new BeanHandler<>(User.class), idCardNumber);
        }

        if (user == null) {
            return null;
        }

        // 如果 salt 为空（旧数据或表中无此列），直接比对明文密码
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            if (password.equals(user.getPassword())) {
                return user;
            }
            return null;
        }

        // 使用盐 + SHA-256 验证密码
        if (PasswordUtils.verify(password, user.getSalt(), user.getPassword())) {
            return user;
        }
        return null;
    }

    // 检查用户是否存在（按身份证号）
    public boolean existsByIdCardNumber(String idCardNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE id_card_number = ?";
        Number count = (Number) runner.query(sql, new ScalarHandler<>(), idCardNumber);
        return count != null && count.intValue() > 0;
    }

    // 新增用户（默认角色 student，密码明文存储，后续修改密码时会自动转为哈希）
    public boolean addUser(String name, String idCardNumber, String password, String role) throws SQLException {
        String sql = "INSERT INTO user (name, id_card_number, password, role) VALUES (?, ?, ?, ?)";
        int rows = runner.update(sql, name, idCardNumber, password, role);
        return rows > 0;
    }

    // 修改密码（使用 SHA-256 + 盐哈希存储）
    public boolean updatePassword(String idCardNumber, String newPassword) throws SQLException {
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hash(newPassword, salt);
        // 先尝试写入 salt 列，如果表中尚无 salt 列，回退为只更新 password
        try {
            String sql = "UPDATE user SET password = ?, salt = ? WHERE id_card_number = ?";
            int rows = runner.update(sql, hashedPassword, salt, idCardNumber);
            return rows > 0;
        } catch (SQLException e) {
            // 如果 salt 列不存在，回退为只更新密码（明文）
            String sql = "UPDATE user SET password = ? WHERE id_card_number = ?";
            int rows = runner.update(sql, newPassword, idCardNumber);
            return rows > 0;
        }
    }
}