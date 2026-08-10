package hmr.interceptor;

import hmr.javabean.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器：替代原来的 AuthFilter
 * - /admin/** → 需要管理员角色
 * - /student/** → 需要登录
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        String path = request.getRequestURI();

        // 管理员路径
        if (path.contains("/admin/")) {
            if (user == null || !"admin".equals(user.getRole())) {
                if (isAjax(request)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"无权限操作\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/login");
                }
                return false;
            }
        }

        // 学生路径
        if (path.contains("/student/")) {
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }

        return true;
    }

    private boolean isAjax(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(xrw)
                || "fetch".equalsIgnoreCase(request.getHeader("Sec-Fetch-Mode"));
    }
}
