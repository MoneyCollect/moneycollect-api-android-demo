package com.example.mcmerchantserver.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * http请求客户端
 *
 * @author Administrator
 */
public class HttpClientUtil {

    public static final String RESPONSE_HEADER = "responseHeader";
    public static final String RESPONSE_BODY = "responseBody";

    private static Log log = LogFactory.getLog(HttpClientUtil.class);
    /**
     *  connect timeout
     */
    private static int CONN_TIMEOUT = 40000;

    /**
     * read timeout
     */
    private static int SO_TIMEOUT = 40000;

    /**
     * version
     */
    private static final String DEFAULT_TLSV = "TLSv1.2";

    public static CloseableHttpClient generateHttpClient(String tlsVersion) {
        // ssl check
        SSLContext sslContext = null;
        if (tlsVersion == null || TextUtils.isEmpty(tlsVersion)) {
            tlsVersion = DEFAULT_TLSV;
        }
        try {
            sslContext = SSLContext.getInstance(tlsVersion);
            X509TrustManager trustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws java.security.cert.CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
            sslContext.init(null, new X509TrustManager[]{trustManager}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // connect manager
        ConnectionSocketFactory plainConnectionSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainConnectionSocketFactory)
                .register("https", new SSLConnectionSocketFactory(sslContext)).build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                registry);

        // set connect timeout
        RequestConfig config = RequestConfig.custom().setConnectTimeout(CONN_TIMEOUT).setSocketTimeout(SO_TIMEOUT)
                .setConnectionRequestTimeout(SO_TIMEOUT).build();
        return HttpClientBuilder.create().setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .setRetryHandler(new StandardHttpRequestRetryHandler())
                .setConnectionManager(poolingHttpClientConnectionManager).setSSLContext(sslContext)
                .setDefaultRequestConfig(config).build();
    }

    /**
     * get request
     */
    public static String doMCGet(String url, Map<String, String> header) {
        HttpClient httpClient = generateHttpClient(DEFAULT_TLSV);
        HttpResponse response = null;
        String responseBody = "";
        try {
            HttpGet httpGet = new HttpGet(url);
            if (header != null && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            response = httpClient.execute(httpGet);
            log.info("received MCServer：" + response);
            Map<String, Object> headersMap = new HashMap<>();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                responseBody = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            try {
                httpClient.getConnectionManager().shutdown();
                return responseBody;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return responseBody;
    }

    /**
     * post request
     */
    public static String doMCPost(String url, String jsonBody, Map<String, String> header) {
        HttpClient httpClient = generateHttpClient(DEFAULT_TLSV);
        HttpResponse response = null;
        String responseBody = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            if (header != null && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            log.info("received MCServer：" + response);
            Map<String, Object> headersMap = new HashMap<>();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                responseBody = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            try {
                httpClient.getConnectionManager().shutdown();
                return responseBody;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return responseBody;
    }

    /**
     * post request ( @Field @FormUrlEncoded)
     */
    public static String doMCPost(Map<String, String> params, String url, Map<String, String> header) {
        HttpClient httpClient = null;
        HttpResponse response = null;
        String resultString = "";
        try {
            httpClient = generateHttpClient(DEFAULT_TLSV);
            HttpPost httpPost = new HttpPost(url);
            if (header != null && !header.isEmpty()) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry entry : params.entrySet()) {
                    if (entry.getValue() != null) {
                        paramList.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                resultString = EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return resultString;
    }

}