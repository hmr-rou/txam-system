<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="hmr.javabean.User" %>
<%@ page import="hmr.javabean.Cet4Score" %>
<%
    User user = (User) session.getAttribute("user");
    List<Cet4Score> scoreList = (List<Cet4Score>) request.getAttribute("scoreList");
    // 优先从 request 读取，再从 session 读取（redirect 过来的消息存在 session 中）
    String message = (String) request.getAttribute("message");
    if (message == null) {
        message = (String) session.getAttribute("message");
        session.removeAttribute("message");
    }
    String error = (String) request.getAttribute("error");
    if (error == null) {
        error = (String) session.getAttribute("error");
        session.removeAttribute("error");
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员主页 - 四级成绩管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
</head>
<body>
<div class="navbar">
    <div class="logo">
        <span>📚</span> 四级成绩管理系统 - 管理员
    </div>
    <div class="nav-right">
        <span class="user-name">👤 <%= user != null ? user.getName() : "系统管理员" %></span>
        <a href="javascript:void(0)" class="logout-btn" onclick="openChangePwdModal()">🔐 修改密码</a>
        <a href="${pageContext.request.contextPath}/LogoutServlet" class="logout-btn">退出登录</a>
    </div>
</div>

<div class="container">
    <% if (message != null && !message.isEmpty()) { %>
    <div class="alert-success">✓ <%= message %></div>
    <% } %>
    <% if (error != null && !error.isEmpty()) { %>
    <div class="alert-error">⚠️ <%= error %></div>
    <% } %>

    <div class="action-bar">
        <button class="btn btn-primary" onclick="openAddModal()">➕ 录入成绩</button>
        <button class="btn btn-success" onclick="toggleSearch()">🔍 高级查询</button>
    </div>

    <!-- 多条件查询面板 -->
    <div class="search-panel" id="searchPanel" style="display: none;">
        <div class="search-title">🔍 多条件查询</div>
        <div class="search-form">
            <div class="search-item">
                <label>身份证号</label>
                <input type="text" id="searchIdCard" placeholder="请输入身份证号">
            </div>
            <div class="search-item">
                <label>准考证号</label>
                <input type="text" id="searchAdmissionNo" placeholder="请输入准考证号">
            </div>
            <div class="search-item">
                <label>学校</label>
                <input type="text" id="searchSchool" placeholder="请输入学校名称">
            </div>
            <div class="search-item">
                <label>二级学院</label>
                <input type="text" id="searchCollege" placeholder="请输入学院名称">
            </div>
            <div class="search-item">
                <label>专业</label>
                <input type="text" id="searchMajor" placeholder="请输入专业名称">
            </div>
            <div class="search-item">
                <label>班级</label>
                <input type="text" id="searchClass" placeholder="请输入班级名称">
            </div>
            <div class="search-actions">
                <button class="btn btn-primary" onclick="searchScores()">🔍 查询</button>
                <button class="btn btn-outline" onclick="resetSearch()">重置</button>
            </div>
        </div>
    </div>

    <!-- 成绩列表 -->
    <div class="card">
        <div class="card-header">
            <span>📋 考生成绩列表</span>
            <span style="color:#8ea8b5; font-size:12px" id="recordCount">共 0 条记录</span>
        </div>
        <div class="card-body">
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>ID</th><th>姓名</th><th>身份证号</th><th>学校</th><th>学院</th>
                        <th>专业</th><th>班级</th><th>准考证号</th><th>成绩</th><th>考试时间</th><th>操作</th>
                    </tr>
                    </thead>
                    <tbody id="scoreTableBody">
                    <% if (scoreList != null && !scoreList.isEmpty()) {
                        for (Cet4Score s : scoreList) {
                            String scoreClass = s.getScore() >= 425 ? "score-pass" : "score-fail";
                    %>
                    <tr>
                        <td><%= s.getId() %></td>
                        <td><%= s.getName() %></td>
                        <td><%= s.getIdCardNumber() %></td>
                        <td><%= s.getSchool() %></td>
                        <td><%= s.getCollege() %></td>
                        <td><%= s.getMajor() %></td>
                        <td><%= s.getClassName() %></td>
                        <td><%= s.getAdmissionNo() %></td>
                        <td class="<%= scoreClass %>"><%= s.getScore() %></td>
                        <td><%= s.getExamTime() %></td>
                        <td class="action-links">
                            <a class="edit-link" onclick="editScore(<%= s.getId() %>)">修改</a>
                            <a class="delete-link" onclick="deleteScore(<%= s.getId() %>)">删除</a>
                        </td>
                    </tr>
                    <% }
                    } else { %>
                    <tr><td colspan="11" style="text-align:center; padding:40px; color:#a8c8d8;">暂无数据</td></tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- 录入/修改成绩模态框 -->
<div id="scoreModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <span id="modalTitle">录入成绩</span>
            <span class="close" onclick="closeModal()">&times;</span>
        </div>
        <div class="modal-body">
            <form id="scoreForm" action="${pageContext.request.contextPath}/AdminSaveScoreServlet" method="post" onsubmit="return false;">
                <input type="hidden" name="id" id="scoreId">
                <div class="form-row">
                    <div class="form-group">
                        <label>姓名 <span class="required">*</span></label>
                        <input type="text" name="name" id="name" required>
                    </div>
                    <div class="form-group">
                        <label>身份证号 <span class="required">*</span></label>
                        <input type="text" name="idCardNumber" id="idCardNumber" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>学校 <span class="required">*</span></label>
                        <input type="text" name="school" id="school" required>
                    </div>
                    <div class="form-group">
                        <label>二级学院 <span class="required">*</span></label>
                        <input type="text" name="college" id="college" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>专业 <span class="required">*</span></label>
                        <input type="text" name="major" id="major" required>
                    </div>
                    <div class="form-group">
                        <label>班级 <span class="required">*</span></label>
                        <input type="text" name="className" id="className" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>准考证号 <span class="required">*</span></label>
                        <input type="text" name="admissionNo" id="admissionNo" required>
                    </div>
                    <div class="form-group">
                        <label>成绩 <span class="required">*</span></label>
                        <input type="number" step="0.5" name="score" id="score" required>
                    </div>
                </div>
                <div class="form-group">
                    <label>考试时间 <span class="required">*</span></label>
                    <input type="date" name="examTime" id="examTime" required>
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeModal()">取消</button>
            <button type="button" class="btn btn-primary" onclick="submitForm()">保存</button>
        </div>
    </div>
</div>

<!-- 修改密码模态框 -->
<div id="changePwdModal" class="modal">
    <div class="modal-content" style="width: 400px;">
        <div class="modal-header">
            <span>🔐 修改密码</span>
            <span class="close" onclick="closeChangePwdModal()">&times;</span>
        </div>
        <div class="modal-body">
            <form id="changePwdForm" action="${pageContext.request.contextPath}/AdminChangePasswordServlet" method="post">
                <div class="form-group">
                    <label>原密码 <span class="required">*</span></label>
                    <input type="password" name="oldPassword" id="oldPassword" required placeholder="请输入原密码">
                </div>
                <div class="form-group">
                    <label>新密码 <span class="required">*</span></label>
                    <input type="password" name="newPassword" id="newPassword" required placeholder="请输入新密码">
                </div>
                <div class="form-group">
                    <label>确认新密码 <span class="required">*</span></label>
                    <input type="password" name="confirmPassword" id="confirmPassword" required placeholder="请再次输入新密码">
                </div>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeChangePwdModal()">取消</button>
            <button class="btn btn-primary" onclick="submitChangePwd()">确认修改</button>
        </div>
    </div>
</div>

<script>
    function submitForm() {
        var form = document.getElementById('scoreForm');
        var formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            body: formData
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            if (data.success) {
                alert(data.message);
                closeModal();
                // 刷新页面以显示最新数据
                window.location.href = '${pageContext.request.contextPath}/AdminHomeServlet';
            } else {
                alert(data.message);
            }
        })
        .catch(function(error) {
            alert('请求失败：' + error);
        });
    }

    function openAddModal() {
        document.getElementById('modalTitle').innerText = '录入成绩';
        document.getElementById('scoreForm').reset();
        document.getElementById('scoreId').value = '';
        document.getElementById('scoreModal').style.display = 'flex';
    }

    function editScore(id) {
        fetch('${pageContext.request.contextPath}/AdminGetScoreServlet?id=' + id)
            .then(response => response.json())
            .then(data => {
                document.getElementById('modalTitle').innerText = '修改成绩';
                document.getElementById('scoreId').value = data.id;
                document.getElementById('name').value = data.name;
                document.getElementById('idCardNumber').value = data.idCardNumber;
                document.getElementById('school').value = data.school;
                document.getElementById('college').value = data.college;
                document.getElementById('major').value = data.major;
                document.getElementById('className').value = data.className;
                document.getElementById('admissionNo').value = data.admissionNo;
                document.getElementById('score').value = data.score;

                // 处理日期
                var date = new Date(data.examTime);
                var year = date.getFullYear();
                var month = String(date.getMonth() + 1).padStart(2, '0');
                var day = String(date.getDate()).padStart(2, '0');
                document.getElementById('examTime').value = year + '-' + month + '-' + day;

                document.getElementById('scoreModal').style.display = 'flex';
            })
            .catch(error => {
                alert('获取数据失败：' + error);
            });
    }

    function deleteScore(id) {
        if (confirm('确定要删除这条成绩记录吗？')) {
            window.location.href = '${pageContext.request.contextPath}/AdminDeleteScoreServlet?id=' + id;
        }
    }

    function toggleSearch() {
        var panel = document.getElementById('searchPanel');
        panel.style.display = panel.style.display === 'none' ? 'block' : 'none';
    }

    function resetSearch() {
        document.getElementById('searchIdCard').value = '';
        document.getElementById('searchAdmissionNo').value = '';
        document.getElementById('searchSchool').value = '';
        document.getElementById('searchCollege').value = '';
        document.getElementById('searchMajor').value = '';
        document.getElementById('searchClass').value = '';
        searchScores();
    }

    function searchScores() {
        var idCard = document.getElementById('searchIdCard').value;
        var admissionNo = document.getElementById('searchAdmissionNo').value;
        var school = document.getElementById('searchSchool').value;
        var college = document.getElementById('searchCollege').value;
        var major = document.getElementById('searchMajor').value;
        var className = document.getElementById('searchClass').value;

        window.location.href = '${pageContext.request.contextPath}/AdminSearchServlet?idCard=' + encodeURIComponent(idCard)
            + '&admissionNo=' + encodeURIComponent(admissionNo)
            + '&school=' + encodeURIComponent(school)
            + '&college=' + encodeURIComponent(college)
            + '&major=' + encodeURIComponent(major)
            + '&className=' + encodeURIComponent(className);
    }

    function closeModal() {
        document.getElementById('scoreModal').style.display = 'none';
    }

    // 修改密码相关
    function openChangePwdModal() {
        document.getElementById('changePwdForm').reset();
        document.getElementById('changePwdModal').style.display = 'flex';
    }

    function closeChangePwdModal() {
        document.getElementById('changePwdModal').style.display = 'none';
    }

    function submitChangePwd() {
        var oldPwd = document.getElementById('oldPassword').value;
        var newPwd = document.getElementById('newPassword').value;
        var confirmPwd = document.getElementById('confirmPassword').value;

        if (!oldPwd || !newPwd || !confirmPwd) {
            alert('请填写完整信息');
            return;
        }

        if (newPwd !== confirmPwd) {
            alert('两次输入的新密码不一致');
            return;
        }

        if (newPwd.length < 6) {
            alert('新密码长度不能少于6位');
            return;
        }

        document.getElementById('changePwdForm').submit();
    }

    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    }

    function updateRecordCount() {
        var tbody = document.getElementById('scoreTableBody');
        var rows = tbody.getElementsByTagName('tr');
        var count = 0;
        for (var i = 0; i < rows.length; i++) {
            if (rows[i].cells.length > 1) count++;
        }
        document.getElementById('recordCount').innerText = '共 ' + count + ' 条记录';
    }
    updateRecordCount();
</script>
</body>
</html>