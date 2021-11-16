package com.example.mcmerchantserver.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RequestUtil {

    private static Log log = LogFactory.getLog(RequestUtil.class);

    /**
     * 获取客户端ip
     * @param request
     * @return
     */
    public static String getRemoteIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    /**
     * 获取请求参数
     * @param req
     * @return
     * @throws Exception
     */
    public static String getReqData(HttpServletRequest req){
        InputStream in = null;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream out = null;
        try {
            in = req.getInputStream();
            bufferedInputStream = new BufferedInputStream(in);
            out = new ByteArrayOutputStream();
            int buffSize = 1024;
            byte[] temp = new byte[buffSize];
            int size = 0;
            while ((size = bufferedInputStream.read(temp)) != -1) {
                out.write(temp, 0, size);
            }
            out.flush();
            bufferedInputStream.close();
            in.close();
            out.close();
            return out.toString("UTF-8");
        } catch (Exception e){
            log.error(e.getMessage());
            return null;
        } finally {
            try {
                if (out != null) out.close();
                if (bufferedInputStream != null) bufferedInputStream.close();
                if (in != null) in.close();
            } catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }
}
