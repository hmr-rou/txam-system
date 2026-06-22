package hmr.service;

import hmr.dao.Cet4ScoreDao;
import hmr.javabean.Cet4Score;

import java.sql.SQLException;
import java.util.List;

public class Cet4ScoreService {

    private Cet4ScoreDao cet4ScoreDao = new Cet4ScoreDao();

    public List<Cet4Score> findAll() throws SQLException {
        return cet4ScoreDao.findAll();
    }

    public List<Cet4Score> findByIdCard(String idCardNumber) throws SQLException {
        return cet4ScoreDao.findByIdCard(idCardNumber);
    }

    public Cet4Score findById(int id) throws SQLException {
        return cet4ScoreDao.findById(id);
    }

    public List<Cet4Score> findByCondition(String idCard, String admissionNo, String school,
                                           String college, String major, String className) throws SQLException {
        return cet4ScoreDao.findByCondition(idCard, admissionNo, school, college, major, className);
    }

    public boolean add(Cet4Score score) throws SQLException {
        return cet4ScoreDao.add(score);
    }

    public boolean update(Cet4Score score) throws SQLException {
        return cet4ScoreDao.update(score);
    }

    public boolean delete(int id) throws SQLException {
        return cet4ScoreDao.delete(id);
    }

    public int batchAdd(List<Cet4Score> scoreList) throws SQLException {
        return cet4ScoreDao.batchAdd(scoreList);
    }
}