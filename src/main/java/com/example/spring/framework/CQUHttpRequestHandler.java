package com.example.spring.framework;

import com.example.spring.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
//MVC框架
public class CQUHttpRequestHandler {

    public static void doService(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("req:"+req.getRequestURI());
        System.out.println("resp:"+resp.toString());
        //设置返回头及字符集
        resp.setHeader("Content-Type","text/html; charset=utf-8");
        resp.setCharacterEncoding("utf-8");
        PrintWriter writer = null;
        try {
            //request 参数封装 object数组
            Object[] paramValue = getParamsValue(req);
            //请求路径
            String requestURI = req.getRequestURI();
            writer = resp.getWriter();
            //获取具体处理方法
            Method method = CQUApplication.methodMapperFactory.get(requestURI);
            if (null == method) {//为null return
                writer.write("404 notFound :" + requestURI);
                return;
            }
            // 默认注册名称查找实例
            String beanName = StringUtils.toFirstByteLowerCase(method.getDeclaringClass().getSimpleName());
            //获取方法持有controller对象
            Object obj = CQUApplication.beanFactory.get(beanName);
            //invoke 执行
            System.out.println("hhhhh:"+paramValue.toString());
            System.out.println("hhhhh:"+paramValue.length);
            Object invoke = method.invoke(obj,paramValue);
            //写回返回值
            writer.write(invoke.toString());
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("500:" + e.getStackTrace().toString());
        }finally {
            if(writer != null){
                writer.close();
            }
        }
    }

    private static Object[] getParamsValue(HttpServletRequest req){
        //request 参数key集合
        List<String> list = Collections.list(req.getParameterNames());
        Object[] paramValue = new Object[list.size()];

        for (int i = 0; i < list.size(); i++) {
            //迭代获取值
            paramValue[i] = req.getParameter(list.get(i));
        }
        return paramValue;
    }

}