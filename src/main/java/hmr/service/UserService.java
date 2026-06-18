package hmr.service;

import hmr.dao.UserDao;
import hmr.javabean.User;

import java.sql.SQLException;

public class UserService {

    private UserDao userDao = new UserDao();

    // 登录验证
    public User login(String idCardNumber, String password) throws SQLException {
        return userDao.login(idCardNumber, password);
    }

    // 修改密码
    public boolean changePassword(String idCardNumber, String oldPassword, String newPassword) throws SQLException {
        // 验证原密码
        User user = userDao.login(idCardNumber, oldPassword);
        if (user == null) {
            return false;
        }
        return userDao.updatePassword(idCardNumber, newPassword);
    }
}