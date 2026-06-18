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

    // 同步用户：如果 user 表中不存在该身份证号，则自动创建，默认密码 123456，角色 student
    public boolean syncUser(String name, String idCardNumber) throws SQLException {
        if (userDao.existsByIdCardNumber(idCardNumber)) {
            return true; // 已存在，无需创建
        }
        return userDao.addUser(name, idCardNumber, "123456", "student");
    }
}