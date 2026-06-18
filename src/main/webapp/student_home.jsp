<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="hmr.javabean.Cet4Score" %>
<%@ page import="hmr.javabean.User" %>
<%
    User user = (User) session.getAttribute("user");
    List<Cet4Score> scoreList = (List<Cet4Score>) request.getAttribute("scoreList");
    String error = (String) request.getAttribute("error");
    if (error == null) {
        error = (String) session.getAttribute("error");
        session.removeAttribute("error");
    }
    String message = (String) request.getAttribute("message");
    if (message == null) {
        message = (String) session.getAttribute("message");
        session.removeAttribute("message");
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>考生主页 - 四级成绩管理系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
</head>
<body>
<div class="navbar">
    <div class="logo">
        <span></span> 四级成绩管理系统
    </div>
    <div class="nav-right">
        <span class="user-name"><%= user != null ? user.getName() : "考生" %> 同学</span>
        <a href="javascript:void(0)" class="logout-btn" onclick="openChangePwdModal()">🔐 修改密码</a>
        <a href="${pageContext.request.contextPath}/LogoutServlet" class="logout-btn">退出登录</a>
    </div>
</div>

<div class="student-container">
    <div class="card">
        <div class="card-header">查询我的成绩</div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/StudentQueryScoreServlet" method="post">
                <div class="form-group">
                    <label>身份证号</label>
                    <input type="text" name="idCardNumber" placeholder="请输入身份证号">
                </div>
                <button type="submit" class="btn btn-primary">查询成绩</button>
            </form>
        </div>
    </div>

    <% if (message != null && !message.isEmpty()) { %>
    <div class="alert-success" style="max-width:1200px; margin:20px auto 0;"><%= message %></div>
    <% } %>
    <% if (error != null && !error.isEmpty()) { %>
    <div class="alert-error" style="max-width:1200px; margin:20px auto 0;"><%= error %></div>
    <% } %>
    <% if (scoreList != null && !scoreList.isEmpty()) { %>
    <div class="card">
        <div class="card-header">我的成绩单</div>
        <div class="card-body">
            <div class="table-wrapper">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>考试时间</th>
                        <th>准考证号</th>
                        <th>学校</th>
                        <th>二级学院</th>
                        <th>专业</th>
                        <th>班级</th>
                        <th>姓名</th>
                        <th>身份证号</th>
                        <th>成绩</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (Cet4Score s : scoreList) { %>
                    <%
                        String scoreClass = s.getScore() >= 425 ? "score-pass" : "score-fail";
                    %>
                    <tr>
                        <td><%= s.getExamTime() %></td>
                        <td><%= s.getAdmissionNo() %></td>
                        <td><%= s.getSchool() %></td>
                        <td><%= s.getCollege() %></td>
                        <td><%= s.getMajor() %></td>
                        <td><%= s.getClassName() %></td>
                        <td><%= s.getName() %></td>
                        <td><%= s.getIdCardNumber() %></td>
                        <td class="<%= scoreClass %>"><%= s.getScore() %></td>
                    </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } else if (scoreList != null && scoreList.isEmpty()) { %>
    <div class="empty-state">
        <div style="font-size: 48px; margin-bottom: 12px;"></div>
        <p>暂无成绩数据</p>
        <p style="font-size: 12px; margin-top: 8px;">请先输入身份证号查询</p>
    </div>
    <% } %>
</div>
<div id="changePwdModal" class="modal">
    <div class="modal-content" style="width: 400px;">
        <div class="modal-header">
            <span>🔐 修改密码</span>
            <span class="close" onclick="closeChangePwdModal()">&times;</span>
        </div>
        <div class="modal-body">
            <form id="changePwdForm" action="${pageContext.request.contextPath}/StudentChangePasswordServlet" method="post">
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

    // 点击模态框遮罩关闭
    window.onclick = function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    }
</script>
</body>
</html>