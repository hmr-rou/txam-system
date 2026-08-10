package hmr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hmr.javabean.Cet4Score;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Cet4ScoreMapper extends BaseMapper<Cet4Score> {

    /**
     * 真正的批量插入（单条 SQL，比 MyBatis-Plus 默认的逐条 saveBatch 更高效）
     */
    @Insert("<script>" +
            "INSERT INTO cet4_score (name, school, college, major, class_name, " +
            "id_card_number, admission_no, score, exam_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.name}, #{item.school}, #{item.college}, #{item.major}, #{item.className}, " +
            "#{item.idCardNumber}, #{item.admissionNo}, #{item.score}, #{item.examTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<Cet4Score> list);
}
