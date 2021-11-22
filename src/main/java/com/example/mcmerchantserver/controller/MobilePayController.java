package com.example.mcmerchantserver.controller;

import com.example.mcmerchantserver.util.HttpClientUtil;
import com.example.mcmerchantserver.util.RequestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/services/v1")
public class MobilePayController {

    private static Log log = LogFactory.getLog(MobilePayController.class);


    //PUBLIC_SECRET Error
    private static final String PUBLIC_SECRET_ERROR = "{\"code\": \"\",\"msg\": \"PUBLIC_SECRET is Error!\"}";

    //Your account PUBLIC_SECRET("Bearer "+PUBLIC_SECRET)
    private static final String PUBLIC_SECRET = "Bearer test_pu_1sWrsjQP9PJiCwGsYv3risSn8YBCIEMNoVFIo8eR6s";
    //Your account PRIVATE_SECRET("Bearer "+PRIVATE_SECRET)
    private static final String PRIVATE_SECRET = "Bearer test_pr_1sWrsjQP9PJiCwGsYv3rin6cs07xRDpeLXBbcpl0nPg";

    @Value(value = "${paymentgateway.mc_create_customer_url}")
    private String mcCreateCustomer_url;
    @Value(value = "${paymentgateway.mc_paymentmethod_attach_customer_url}")
    private String mcPaymentMethodAttachCustomer_url;
    @Value(value = "${paymentgateway.mc_create_payment_url}")
    private String mcCreatePayment_url;


    /**
     * mcconnect sdk
     * creat customer
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/customers/create")
    @ResponseBody
    public String mcCreateCustomer(HttpServletRequest request) throws Exception {
        String result = "";
        // 1. 获取ip,校验PUBLIC_SECRET
        String remoteIp = RequestUtil.getRemoteIP(request);
        log.info("remoteIp: " + remoteIp);
        String pu_secret = request.getHeader("Authorization");
        if (pu_secret.isEmpty() || !pu_secret.equals(PUBLIC_SECRET)) {
            return PUBLIC_SECRET_ERROR;
        }
        // 2. 获取请求参数、判空
        String requestData = RequestUtil.getReqData(request);

        // 3.拼接请求头
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", PRIVATE_SECRET);
        headersMap.put("Content-Type", "application/json");

        // 4. 请求moneyconnect server
        log.info("开始请求站moneyconnect server，请求参数：" + requestData + "，请求header：" + headersMap + "，请求url：" + mcCreateCustomer_url);
        result = HttpClientUtil.doMCPost(mcCreateCustomer_url, requestData, headersMap);
        log.info("收到moneyconnect server的响应：" + result);
        return result;
    }

    /**
     * mcconnect sdk
     * select AllPaymentMethods of customer
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/payment_methods/list/{customerId}")
    @ResponseBody
    public String selectAllPaymentMethods(HttpServletRequest request) throws Exception {
        String result = "";
        // 1. 获取ip,校验PUBLIC_SECRET
        String remoteIp = RequestUtil.getRemoteIP(request);
        log.info("remoteIp: " + remoteIp);
        String pu_secret = request.getHeader("Authorization");
        log.info("pu_secret1: " + pu_secret);
        if (pu_secret.isEmpty() || !pu_secret.equals(PUBLIC_SECRET)) {
            return PUBLIC_SECRET_ERROR;
        }

        // 2. 获取请求参数、判空
        String requestData = RequestUtil.getReqData(request);

        // 3.拼接请求头
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", PRIVATE_SECRET);
        headersMap.put("Content-Type", "application/json");

        // 4. 请求moneyconnect server
        log.info("开始请求站moneyconnect server，请求参数：" + requestData + "，请求header：" + headersMap + "，请求url：" + mcPaymentMethodAttachCustomer_url + request.getRequestURI());
        result = HttpClientUtil.doMCGet(mcPaymentMethodAttachCustomer_url + request.getRequestURI(), headersMap);
        log.info("收到moneyconnect server的响应：" + result);
        return result;
    }

    /**
     * mcconnect sdk
     * PaymentMethod Attach Customer
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/payment_methods/{paymentMethodId}/attach")
    @ResponseBody
    public String mcPaymentMethodAttachCustomer(HttpServletRequest request, @PathVariable String paymentMethodId) throws Exception {
        String result = "";
        // 1. 获取ip,校验PUBLIC_SECRET
        String remoteIp = RequestUtil.getRemoteIP(request);
        log.info("remoteIp: " + remoteIp);
        String pu_secret = request.getHeader("Authorization");
        if (pu_secret.isEmpty() || !pu_secret.equals(PUBLIC_SECRET)) {
            return PUBLIC_SECRET_ERROR;
        }

        // 2. 获取请求参数、判空
        String requestData = RequestUtil.getReqData(request);

        log.info("requestData: " + requestData);
        // 3.拼接请求头
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", PRIVATE_SECRET);
        // 4. 将请求参数转成map
        Map<String, String> requestMap = externalInfo2Map(requestData);

        // 4. 请求moneyconnect server
        log.info("开始请求站moneyconnect server，请求参数：" + requestMap + "，请求header：" + headersMap + "，请求url：" + mcPaymentMethodAttachCustomer_url + request.getRequestURI());
        result = HttpClientUtil.doMCPost(requestMap, mcPaymentMethodAttachCustomer_url + request.getRequestURI(), headersMap);
        log.info("收到moneyconnect server的响应：" + result);
        return result;
    }


    /**
     * mcconnect sdk
     * creat payment
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/payment/create")
    @ResponseBody
    public String mcCreatePayment(HttpServletRequest request) throws Exception {
        String result = "";
        // 1. 获取ip,校验PUBLIC_SECRET
        String remoteIp = RequestUtil.getRemoteIP(request);
        log.info("remoteIp: " + remoteIp);
        String pu_secret = request.getHeader("Authorization");
        if (pu_secret.isEmpty() || !pu_secret.equals(PUBLIC_SECRET)) {
            return PUBLIC_SECRET_ERROR;
        }

        // 2. 获取请求参数、判空
        String requestData = RequestUtil.getReqData(request);

        // 3.拼接请求头
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", PRIVATE_SECRET);
        headersMap.put("Content-Type", "application/json");

        // 4. 请求moneyconnect server
        log.info("开始请求moneyconnect server，请求参数：" + requestData + "，请求header：" + headersMap + "，请求url：" + mcCreatePayment_url);
        result = HttpClientUtil.doMCPost(mcCreatePayment_url, requestData, headersMap);
        log.info("收到moneyconnect server的响应：" + result);
        return result;
    }

    private Map<String, String> externalInfo2Map(String externalInfo) {
        return stringToMap(externalInfo);
    }

    /**
     * 将externalInfo(用&拼接的多个参数)字符串转成map
     *
     * @param externalInfo
     */
    private Map<String, String> stringToMap(String externalInfo) {
        Map<String, String> map = new HashMap<>();
        String[] params = externalInfo.split("&");
        if (params.length == 0) {
            return map;
        }
        for (int i = 0; i < params.length; i++) {
            String key = params[i].substring(0, params[i].indexOf("="));
            String value = params[i].substring(params[i].indexOf("=") + 1);
            map.put(key, value);
        }
        return map;
    }


}
