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
    // QueryRunner 是 Apache DBUtils 的核心类，用于执行 SQL
    // 登录验证（使用 SHA-256 + 盐哈希验证，兼容无 salt 列的旧表）
    public User login(String idCardNumber, String password) throws SQLException {
        User user = null;
        //步骤 1：查询用户信息（先尝试查询含 salt 列）
        try {
            String sql = "SELECT id, id_card_number AS idCardNumber, name, password, salt, role FROM user WHERE id_card_number = ?";
            // BeanHandler：将查询结果自动映射到 User 对象
            user = runner.query(sql, new BeanHandler<>(User.class), idCardNumber);// 2. 执行查询并自动映射到 user 对象
        } catch (SQLException e) {
            // 如果 salt 列不存在（旧数据库），回退到不查 salt
            String sql = "SELECT id, id_card_number AS idCardNumber, name, password, role FROM user WHERE id_card_number = ?";
            user = runner.query(sql, new BeanHandler<>(User.class), idCardNumber);
        }
        //步骤 2：用户不存在
        if (user == null) {
            return null;
        }

        //  步骤 3：密码验证
        // 情况1：没有 salt（旧数据），直接比对明文密码
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            if (password.equals(user.getPassword())) {
                return user;// 密码正确
            }
            return null;// 密码错误
        }

        // 情况2：有 salt（新数据），使用 SHA-256 + 盐验证
        if (PasswordUtils.verify(password, user.getSalt(), user.getPassword())) {
            return user;// 密码正确
        }
        return null;// 密码错误
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