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
        <button class="btn btn-primary" onclick="openImportModal()" style="background:#c49b7a;">📥 批量导入</button>
        <a href="${pageContext.request.contextPath}/AdminDownloadTemplateServlet" class="btn btn-outline btn-sm" style="padding:8px 16px;">📋 下载导入模板</a>
    </div>

    <!-- 多条件查询面板 -->
    <div class="search-panel" id="searchPanel" style="display: none;">
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
                <button class="btn btn-success" onclick="exportScores()">📤 导出Excel</button>
            </div>
        </div>
    </div>

    <!-- 成绩列表 -->
    <div class="card">
        <div class="card-header">
            <span>考生成绩列表</span>
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

<!-- 批量导入模态框 -->
<div id="importModal" class="modal">
    <div class="modal-content" style="width: 550px;">
        <div class="modal-header">
            <span>📥 批量导入成绩</span>
            <span class="close" onclick="closeImportModal()">&times;</span>
        </div>
        <div class="modal-body">
            <div style="background:#f5fafc; border:1px solid #d4e0e5; border-radius:8px; padding:12px; margin-bottom:16px; font-size:13px; color:#5a8a9a;">
                <strong>📋 导入说明：</strong>
                <ol style="margin:8px 0 0 16px; line-height:1.8;">
                    <li>请先<a href="${pageContext.request.contextPath}/AdminDownloadTemplateServlet" style="color:#7ab3c8;">下载导入模板</a>，按模板格式填写数据</li>
                    <li>支持 .xlsx 和 .xls 格式的 Excel 文件</li>
                    <li>文件大小不超过 10MB</li>
                    <li>考试时间格式：yyyy-MM-dd（如 2024-06-15）</li>
                    <li>成绩范围：0-710</li>
                    <li>导入成功后会自动为学生创建登录账号（默认密码：123456）</li>
                </ol>
            </div>
            <form id="importForm" enctype="multipart/form-data">
                <div class="form-group">
                    <label>选择 Excel 文件 <span class="required">*</span></label>
                    <input type="file" id="excelFile" name="excelFile"
                           accept=".xlsx,.xls"
                           style="max-width:100%; padding:6px;"
                           onchange="updateFileName()">
                </div>
                <div id="fileNameDisplay" style="font-size:12px; color:#8ea8b5; margin-top:-8px;"></div>
            </form>
            <!-- 导入进度/结果区域 -->
            <div id="importResult" style="display:none;"></div>
        </div>
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeImportModal()">取消</button>
            <button class="btn btn-primary" id="importBtn" onclick="submitImport()" style="background:#c49b7a;">开始导入</button>
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
            <span>修改密码</span>
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
    // ========== 批量导入相关 ==========

    function openImportModal() {
        document.getElementById('importForm').reset();
        document.getElementById('fileNameDisplay').innerText = '';
        document.getElementById('importResult').style.display = 'none';
        document.getElementById('importResult').innerHTML = '';
        document.getElementById('importBtn').disabled = false;
        document.getElementById('importModal').style.display = 'flex';
    }

    function closeImportModal() {
        document.getElementById('importModal').style.display = 'none';
    }

    function updateFileName() {
        var fileInput = document.getElementById('excelFile');
        var display = document.getElementById('fileNameDisplay');
        if (fileInput.files && fileInput.files.length > 0) {
            display.innerText = '已选择：' + fileInput.files[0].name
                + '（' + formatFileSize(fileInput.files[0].size) + '）';
        } else {
            display.innerText = '';
        }
    }

    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }

    function submitImport() {
        var fileInput = document.getElementById('excelFile');
        if (!fileInput.files || fileInput.files.length === 0) {
            alert('请先选择 Excel 文件');
            return;
        }

        var file = fileInput.files[0];
        if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
            alert('请选择 .xlsx 或 .xls 格式的文件');
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            alert('文件大小不能超过 10MB');
            return;
        }

        var importBtn = document.getElementById('importBtn');
        importBtn.disabled = true;
        importBtn.innerText = '导入中...';

        var formData = new FormData();
        formData.append('excelFile', file);

        fetch('${pageContext.request.contextPath}/AdminImportServlet', {
            method: 'POST',
            credentials: 'same-origin',
            body: formData
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            importBtn.disabled = false;
            importBtn.innerText = '开始导入';

            // 登录过期处理
            if (data.message === '无权限操作' || data.message === '未登录') {
                alert('登录已过期，请重新登录');
                window.location.href = '${pageContext.request.contextPath}/login.jsp';
                return;
            }

            var resultDiv = document.getElementById('importResult');
            resultDiv.style.display = 'block';

            if (data.success) {
                var html = '<div class="alert-success" style="margin-top:12px;">✓ ' + data.message + '</div>';
                if (data.errors && data.errors.length > 0) {
                    html += '<div style="margin-top:8px; max-height:200px; overflow-y:auto; font-size:12px; color:#d9877a;">';
                    html += '<strong>以下行存在格式问题：</strong><ul style="margin:4px 0 0 16px;">';
                    for (var i = 0; i < data.errors.length; i++) {
                        html += '<li>' + data.errors[i] + '</li>';
                    }
                    html += '</ul></div>';
                }
                resultDiv.innerHTML = html;
                // 刷新列表
                setTimeout(function() {
                    window.location.href = '${pageContext.request.contextPath}/AdminHomeServlet';
                }, 2000);
            } else {
                var html = '<div class="alert-error" style="margin-top:12px;">⚠️ ' + data.message + '</div>';
                if (data.errors && data.errors.length > 0) {
                    html += '<div style="margin-top:8px; max-height:200px; overflow-y:auto; font-size:12px; color:#d9877a;">';
                    html += '<strong>错误详情：</strong><ul style="margin:4px 0 0 16px;">';
                    for (var i = 0; i < data.errors.length; i++) {
                        html += '<li>' + data.errors[i] + '</li>';
                    }
                    html += '</ul></div>';
                }
                resultDiv.innerHTML = html;
            }
        })
        .catch(function(error) {
            importBtn.disabled = false;
            importBtn.innerText = '开始导入';
            alert('导入请求失败：' + error);
        });
    }

    // ========== 批量导出相关 ==========

    function exportScores() {
        // 使用当前搜索条件
        var idCard = document.getElementById('searchIdCard').value;
        var admissionNo = document.getElementById('searchAdmissionNo').value;
        var school = document.getElementById('searchSchool').value;
        var college = document.getElementById('searchCollege').value;
        var major = document.getElementById('searchMajor').value;
        var className = document.getElementById('searchClass').value;

        // 检查是否有搜索面板可见（用户正在使用搜索条件）
        var params = [];
        if (idCard) params.push('idCard=' + encodeURIComponent(idCard));
        if (admissionNo) params.push('admissionNo=' + encodeURIComponent(admissionNo));
        if (school) params.push('school=' + encodeURIComponent(school));
        if (college) params.push('college=' + encodeURIComponent(college));
        if (major) params.push('major=' + encodeURIComponent(major));
        if (className) params.push('className=' + encodeURIComponent(className));

        var url = '${pageContext.request.contextPath}/AdminExportServlet';
        if (params.length > 0) {
            url += '?' + params.join('&');
        }

        // 确认导出
        var confirmMsg = params.length > 0
            ? '将导出当前查询条件下的所有成绩，确定继续？'
            : '将导出全部成绩，确定继续？';
        if (confirm(confirmMsg)) {
            window.location.href = url;
        }
    }

    // ========== 原有函数 ==========

    function submitForm() {
        var form = document.getElementById('scoreForm');
        var formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            credentials: 'same-origin',
            body: formData
        })
        .then(function(response) { return response.json(); })
        .then(function(data) {
            if (data.success) {
                alert(data.message);
                closeModal();
                // 刷新页面以显示最新数据
                window.location.href = '${pageContext.request.contextPath}/AdminHomeServlet';
            } else if (data.message === '无权限操作') {
                alert('登录已过期，请重新登录');
                window.location.href = '${pageContext.request.contextPath}/login.jsp';
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