package hmr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hmr.javabean.User;
import hmr.mapper.UserMapper;
import hmr.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserMapper userMapper;

    // 登录验证（SHA-256 + 盐，兼容无 salt 列的旧数据）
    public User login(String idCardNumber, String password) {
        User user = null;

        // 先尝试查含 salt 列
        try {
            user = userMapper.selectByIdCardWithSalt(idCardNumber);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "查询含 salt 列失败，降级为无 salt 查询: {0}", e.getMessage());
        }

        // 降级：查不含 salt 列
        if (user == null) {
            user = userMapper.selectByIdCardNoSalt(idCardNumber);
        }

        if (user == null) {
            return null;
        }

        // 密码验证
        if (user.getSalt() == null || user.getSalt().isEmpty()) {
            // 旧数据：明文比对
            return password.equals(user.getPassword()) ? user : null;
        }

        // 新数据：SHA-256 + 盐
        return PasswordUtils.verify(password, user.getSalt(), user.getPassword()) ? user : null;
    }

    // 修改密码
    public boolean changePassword(String idCardNumber, String oldPassword, String newPassword) {
        User user = login(idCardNumber, oldPassword);
        if (user == null) {
            return false;
        }
        return updatePassword(idCardNumber, newPassword);
    }

    private boolean updatePassword(String idCardNumber, String newPassword) {
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hash(newPassword, salt);
        try {
            int rows = userMapper.updatePasswordWithSalt(idCardNumber, hashedPassword, salt);
            return rows > 0;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "写入 salt 列失败，降级为明文存储: {0}", e.getMessage());
            int rows = userMapper.updatePasswordOnly(idCardNumber, newPassword);
            return rows > 0;
        }
    }

    // 同步用户：不存在则自动创建（默认密码 123456，角色 student）
    public boolean syncUser(String name, String idCardNumber) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getIdCardNumber, idCardNumber));
        if (count != null && count > 0) {
            return true;
        }
        User user = new User();
        user.setName(name);
        user.setIdCardNumber(idCardNumber);
        user.setPassword("123456");
        user.setRole("student");
        return userMapper.insert(user) > 0;
    }
}
