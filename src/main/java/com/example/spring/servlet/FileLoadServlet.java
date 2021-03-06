package com.example.spring.servlet;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

// @WebServlet(name = "FileLoadServlet", urlPatterns = {"/fileUpload"})
public class FileLoadServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(FileLoadServlet.class);

    /**
     *
     */
    private static final long serialVersionUID = 1302377908285976972L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("------------ FileLoadServlet ------------");

        if (request.getContentLength() > 0) {

            InputStream inputStream = null;
            FileOutputStream outputStream = null;

            try {

                inputStream = request.getInputStream();
                // 给新文件拼上时间毫秒，防止重名
                long now = System.currentTimeMillis();
                File file = new File("d:/", now + ".txt");
                file.createNewFile();

                outputStream = new FileOutputStream(file);

                byte[] temp = new byte[1024];
                int size;
                while ((size = inputStream.read(temp)) != -1) { // 每次读取1KB，直至读完
                    outputStream.write(temp, 0, size);
                }

                logger.info("File load success.");
            } catch (IOException e) {
                logger.warn("File load fail.", e);
                request.getRequestDispatcher("/fail.jsp").forward(request, response);
            } finally {
                assert outputStream != null;
                outputStream.close();
                inputStream.close();
            }
        }

        request.getRequestDispatcher("/succ.jsp").forward(request, response);
    }

}
