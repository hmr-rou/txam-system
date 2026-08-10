package hmr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hmr.javabean.Cet4Score;
import hmr.mapper.Cet4ScoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Cet4ScoreService {

    @Autowired
    private Cet4ScoreMapper cet4ScoreMapper;

    public List<Cet4Score> findAll() {
        return cet4ScoreMapper.selectList(
                new LambdaQueryWrapper<Cet4Score>().orderByAsc(Cet4Score::getId));
    }

    public List<Cet4Score> findByIdCard(String idCardNumber) {
        return cet4ScoreMapper.selectList(
                new LambdaQueryWrapper<Cet4Score>()
                        .eq(Cet4Score::getIdCardNumber, idCardNumber)
                        .orderByDesc(Cet4Score::getExamTime));
    }

    public Cet4Score findById(int id) {
        return cet4ScoreMapper.selectById(id);
    }

    public List<Cet4Score> findByCondition(String idCard, String admissionNo, String school,
                                           String college, String major, String className) {
        LambdaQueryWrapper<Cet4Score> wrapper = new LambdaQueryWrapper<>();
        if (idCard != null && !idCard.trim().isEmpty())
            wrapper.like(Cet4Score::getIdCardNumber, idCard);
        if (admissionNo != null && !admissionNo.trim().isEmpty())
            wrapper.like(Cet4Score::getAdmissionNo, admissionNo);
        if (school != null && !school.trim().isEmpty())
            wrapper.like(Cet4Score::getSchool, school);
        if (college != null && !college.trim().isEmpty())
            wrapper.like(Cet4Score::getCollege, college);
        if (major != null && !major.trim().isEmpty())
            wrapper.like(Cet4Score::getMajor, major);
        if (className != null && !className.trim().isEmpty())
            wrapper.like(Cet4Score::getClassName, className);
        wrapper.orderByAsc(Cet4Score::getId);
        return cet4ScoreMapper.selectList(wrapper);
    }

    public boolean add(Cet4Score score) {
        return cet4ScoreMapper.insert(score) > 0;
    }

    public boolean update(Cet4Score score) {
        return cet4ScoreMapper.updateById(score) > 0;
    }

    public boolean delete(int id) {
        return cet4ScoreMapper.deleteById(id) > 0;
    }

    /**
     * 批量导入（一条 SQL 批量插入，高效）
     */
    public int batchAdd(List<Cet4Score> scoreList) {
        return cet4ScoreMapper.batchInsert(scoreList);
    }
}
