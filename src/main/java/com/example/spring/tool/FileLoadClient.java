package com.example.spring.tool;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileLoadClient {
    private static final Logger logger = Logger.getLogger(FileLoadClient.class);

    public static String fileUpload(String url, File file) {
        String body = "{}";
        if (url == null || url.equals("")) {
            return "参数不合法";
        }
        if (!file.exists()) {
            return "要上传的文件名不存在";
        }
        PostMethod postMethod = new PostMethod(url);
        try {

            // FilePart：用来上传文件的类,file即要上传的文件
            FilePart fp = new FilePart("file", file);
            Part[] parts = {fp};

            // 对于MIME类型的请求，httpclient建议全用MultiPartRequestEntity进行包装
            MultipartRequestEntity mre = new MultipartRequestEntity(parts, postMethod.getParams());
            postMethod.setRequestEntity(mre);

            HttpClient client = new HttpClient();
            // 由于要上传的文件可能比较大 , 因此在此设置最大的连接超时时间
            client.getHttpConnectionManager().getParams().setConnectionTimeout(50000);

            int status = client.executeMethod(postMethod);
            if (status == HttpStatus.SC_OK) {
                InputStream inputStream = postMethod.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuffer = new StringBuilder();
                String str;
                while ((str = br.readLine()) != null) {
                    stringBuffer.append(str);
                }

                body = stringBuffer.toString();

            } else {
                body = "fail";
            }
        } catch (Exception e) {
            logger.warn("上传文件异常", e);
        } finally {
            // 释放连接
            postMethod.releaseConnection();
        }

        return body;
    }

    public static void main(String[] args) {
        String body = fileUpload("http://localhost:8080/jsp_upload-servlet/fileload", new File("d:/test.txt"));
        System.out.println(body);
    }

}
