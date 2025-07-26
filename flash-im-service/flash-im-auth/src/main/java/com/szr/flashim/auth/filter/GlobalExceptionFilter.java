package com.szr.flashim.auth.filter;

import com.alibaba.fastjson2.JSON;
import com.szr.flashim.general.exception.ServiceException;
import com.szr.flashim.general.model.ResponseResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class GlobalExceptionFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServiceException e) {
            writeResponse(response, HttpStatus.OK, e.getCode(), e.getMessage());
        } catch (Exception e) {
            writeResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, ResponseResult.FAIL, "发生错误，请联系管理员查看！");
        }
    }

    private void writeResponse(HttpServletResponse response, HttpStatus httpStatus, int code, String message) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String result = JSON.toJSONString(new ResponseResult<>(code, message));

        PrintWriter out = response.getWriter();
        out.write(result);
        out.flush();
        out.close();
    }
}
