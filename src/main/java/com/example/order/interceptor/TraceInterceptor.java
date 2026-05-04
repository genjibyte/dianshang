package com.example.order.interceptor;

import com.example.order.common.util.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceIdUtil.generate();
        }
        TraceIdUtil.set(traceId);
        response.setHeader("X-Trace-Id", traceId);

        log.info("请求开始: {} {} traceId={}", request.getMethod(), request.getRequestURI(), traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        log.info("请求结束: {} {} status={}", request.getMethod(), request.getRequestURI(), response.getStatus());
        TraceIdUtil.clear();
    }
}
