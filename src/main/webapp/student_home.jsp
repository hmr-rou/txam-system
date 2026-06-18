<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="hmr.javabean.Cet4Score" %>
<%@ page import="hmr.javabean.User" %>
<%
    User user = (User) session.getAttribute("user");
    List<Cet4Score> scoreList = (List<Cet4Score>) request.getAttribute("scoreList");
    String error = (String) request.getAttribute("error");
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
        <a href="${pageContext.request.contextPath}/logout" class="logout-btn">退出登录</a>
    </div>
</div>

<div class="student-container">
    <div class="card">
        <div class="card-header">查询我的成绩</div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/student/queryScore" method="post">
                <div class="form-group">
                    <label>身份证号</label>
                    <input type="text" name="idCardNumber" placeholder="请输入身份证号">
                </div>
                <button type="submit" class="btn btn-primary">查询成绩</button>
            </form>
        </div>
    </div>

    <% if (error != null && !error.isEmpty()) { %>
    <div class="empty-state">
        <div style="font-size: 48px; margin-bottom: 12px;"></div>
        <p><%= error %></p>
    </div>
    <% } else if (scoreList != null && !scoreList.isEmpty()) { %>
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
            <form id="changePwdForm" action="${pageContext.request.contextPath}/student/changePassword" method="post">
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
</body>
</html>