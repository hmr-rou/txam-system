package hmr.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hmr.javabean.Cet4Score;
import hmr.mapper.Cet4ScoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 成绩服务层单元测试
 */
@ExtendWith(MockitoExtension.class)
class Cet4ScoreServiceTest {

    @Mock
    private Cet4ScoreMapper mapper;

    @InjectMocks
    private Cet4ScoreService service;

    private Cet4Score sampleScore;

    @BeforeEach
    void setUp() {
        sampleScore = new Cet4Score();
        sampleScore.setId(1);
        sampleScore.setName("张三");
        sampleScore.setIdCardNumber("110101199001011234");
        sampleScore.setScore(500);
        sampleScore.setExamTime(Date.valueOf("2024-06-15"));
    }

    @Test
    @DisplayName("新增成绩 — 成功")
    void add_shouldReturnTrue_whenInsertSucceeds() {
        when(mapper.insert(sampleScore)).thenReturn(1);
        assertTrue(service.add(sampleScore));
        verify(mapper).insert(sampleScore);
    }

    @Test
    @DisplayName("新增成绩 — 失败返回 false")
    void add_shouldReturnFalse_whenInsertFails() {
        when(mapper.insert(sampleScore)).thenReturn(0);
        assertFalse(service.add(sampleScore));
    }

    @Test
    @DisplayName("按身份证号查询成绩")
    void findByIdCard_shouldReturnList() {
        List<Cet4Score> scores = Arrays.asList(sampleScore);
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(scores);
        List<Cet4Score> result = service.findByIdCard("110101199001011234");
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
    }

    @Test
    @DisplayName("按条件查询 — 所有条件为空时也能查询")
    void findByCondition_shouldWorkWithAllNulls() {
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(sampleScore));
        List<Cet4Score> result = service.findByCondition(null, null, null, null, null, null);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("按条件查询 — 传入关键词应调用 LIKE 查询")
    void findByCondition_shouldFilterByKeyword() {
        when(mapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());
        service.findByCondition("101", "", "", "", "", "");
        // 验证 mapper 被调用了一次，条件由 LambdaQueryWrapper 封装
        verify(mapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新成绩 — 成功返回 true")
    void update_shouldReturnTrue_whenUpdateSucceeds() {
        when(mapper.updateById(sampleScore)).thenReturn(1);
        assertTrue(service.update(sampleScore));
    }

    @Test
    @DisplayName("删除成绩 — 成功返回 true")
    void delete_shouldReturnTrue_whenDeleteSucceeds() {
        when(mapper.deleteById(1)).thenReturn(1);
        assertTrue(service.delete(1));
    }

    @Test
    @DisplayName("批量导入 — 调用批量插入方法")
    void batchAdd_shouldCallBatchInsert() {
        List<Cet4Score> list = Arrays.asList(sampleScore);
        when(mapper.batchInsert(list)).thenReturn(1);
        assertEquals(1, service.batchAdd(list));
        verify(mapper).batchInsert(list);
    }
}
