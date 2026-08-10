package hmr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hmr.javabean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 按身份证号查询用户（含 salt 列）
     */
    @Select("SELECT id, id_card_number AS idCardNumber, name, password, salt, role " +
            "FROM user WHERE id_card_number = #{idCardNumber}")
    User selectByIdCardWithSalt(@Param("idCardNumber") String idCardNumber);

    /**
     * 按身份证号查询用户（不含 salt 列，兼容旧表）
     */
    @Select("SELECT id, id_card_number AS idCardNumber, name, password, role " +
            "FROM user WHERE id_card_number = #{idCardNumber}")
    User selectByIdCardNoSalt(@Param("idCardNumber") String idCardNumber);

    /**
     * 更新密码和盐
     */
    @Update("UPDATE user SET password = #{password}, salt = #{salt} WHERE id_card_number = #{idCardNumber}")
    int updatePasswordWithSalt(@Param("idCardNumber") String idCardNumber,
                                @Param("password") String password,
                                @Param("salt") String salt);

    /**
     * 仅更新密码（兼容无 salt 列旧表）
     */
    @Update("UPDATE user SET password = #{password} WHERE id_card_number = #{idCardNumber}")
    int updatePasswordOnly(@Param("idCardNumber") String idCardNumber,
                           @Param("password") String password);
}
