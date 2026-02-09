package com.oceanpay.filter;

import javax.servlet.*;
import java.io.IOException;

public class SetCharacterEncodingFilter implements Filter {
    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String encodingParam = filterConfig.getInitParameter("encoding");
        if (encodingParam != null) {
            encoding = encodingParam;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 设置请求和响应的字符编码
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html; charset=" + encoding);
        // 将请求/响应传递给过滤链中的下一个资源（Servlet/JSP）
        chain.doFilter(request, response);
    }
}
