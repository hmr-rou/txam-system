<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>大学英语四级考试成绩管理系统 - 登录</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
</head>
<body class="login-bg">
<div class="login-container">
    <div class="login-header">
        <div class="logo-icon"></div>
        <h1>大学英语四级考试</h1>
        <p>成绩管理系统</p>
    </div>

    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="input-group" style="margin-bottom: 20px;">
            <label>身份证号</label>
            <input type="text" name="username" placeholder="请输入身份证号" required
                   style="width:100%; padding:10px 12px; border:1px solid #d4e0e5; border-radius:8px; font-size:14px;">
        </div>

        <div class="input-group" style="margin-bottom: 20px;">
            <label>密码</label>
            <input type="password" name="password" placeholder="请输入密码" required
                   style="width:100%; padding:10px 12px; border:1px solid #d4e0e5; border-radius:8px; font-size:14px;">
        </div>

        <button type="submit" class="btn btn-primary" style="width:100%; padding:10px; font-size:16px; margin-top:8px;">登 录</button>

        <c:if test="${not empty error}">
            <div class="alert-error" style="margin-top:16px; text-align:center;">${error}</div>
        </c:if>
    </form>
</div>
</body>
</html>