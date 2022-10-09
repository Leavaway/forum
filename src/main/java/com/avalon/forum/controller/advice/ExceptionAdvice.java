package com.avalon.forum.controller.advice;


import com.avalon.forum.util.ForumTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void exceptionHandler(Exception e, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        logger.error("服务异常: " + e.getMessage());
        for (StackTraceElement element:
             e.getStackTrace()) {
            logger.error(element.toString());
        }

        if(httpServletRequest.getHeader("x-requested-with").equals("XMLHttpRequest")){
            httpServletResponse.setContentType("application/plain;charset=utf-8");
            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.write(ForumTools.getJSONString(1,"服务异常"));
        }else {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/error");
        }
    }

}
