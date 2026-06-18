package hmr.dao;

import hmr.javabean.User;
import hmr.utils.C3p0Utils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.SQLException;

public class UserDao {

    private QueryRunner runner = new QueryRunner(C3p0Utils.getDataSource());

    // 登录验证
    public User login(String idCardNumber, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE id_card_number = ? AND password = ?";
        return runner.query(sql, new BeanHandler<>(User.class), idCardNumber, password);
    }

    // 修改密码
    public boolean updatePassword(String idCardNumber, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE id_card_number = ?";
        int rows = runner.update(sql, newPassword, idCardNumber);
        return rows > 0;
    }
}