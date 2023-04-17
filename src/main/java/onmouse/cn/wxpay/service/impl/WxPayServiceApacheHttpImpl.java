package onmouse.cn.wxpay.service.impl;

import onmouse.cn.wxpay.exception.WxPayException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信支付请求类，使用apache httpclient实现
 */
public class WxPayServiceApacheHttpImpl extends BaseWxPayServiceImpl {

    private void initSSLContent(HttpClientBuilder httpClientBuilder) throws WxPayException {
        SSLContext sslContext = this.getConfig().getSslContext();
        if (sslContext == null) {
            sslContext = this.getConfig().initSSLContext();
        }

        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());
        httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
    }

    private HttpClientBuilder createHttpClientBuilder(boolean useKey) throws WxPayException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (useKey) {
            this.initSSLContent(httpClientBuilder);
        }

        if (StringUtils.isNotBlank(this.getConfig().getHttpProxyHost()) && this.getConfig().getHttpProxyPort() > 0) {
            if (StringUtils.isEmpty(this.getConfig().getHttpProxyUsername())) {
                this.getConfig().setHttpProxyUsername("whatever");
            }

            // 使用代理服务器，需要用户认证的代理服务器
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(this.getConfig().getHttpProxyHost(), this.getConfig().getHttpProxyPort()),
                    new UsernamePasswordCredentials(this.getConfig().getHttpProxyUsername(), this.getConfig().getHttpProxyPassword()));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            httpClientBuilder.setProxy(new HttpHost(this.getConfig().getHttpProxyHost(), this.getConfig().getHttpProxyPort()));
        }
        return httpClientBuilder;
    }

    private StringEntity createEntry(String requestStr) {
        return new StringEntity(requestStr, ContentType.create("application/json", "utf-8"));
    }

    private HttpPost createHttpPost(String url, String requestStr) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(this.createEntry(requestStr));

        httpPost.setConfig(RequestConfig
                .custom()
                .setConnectionRequestTimeout(this.getConfig().getHttpConnectionTimeout())
                .setConnectTimeout(this.getConfig().getHttpConnectionTimeout())
                .setSocketTimeout(this.getConfig().getHttpTimeout())
                .build());

        return httpPost;
    }

    @Override
    public String post(String url, String requestStr, boolean useKey) throws WxPayException {
        try {
            HttpClientBuilder httpClientBuilder = this.createHttpClientBuilder(useKey);
            HttpPost httpPost = this.createHttpPost(url, requestStr);
            try(CloseableHttpClient httpClient = httpClientBuilder.build()) {
                try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                    String responseString = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
                    this.log.info("\n【请求地址】：{}\n【请求数据】：{}\n【响应数据】：{}", url, requestStr, responseString);
                    return responseString;
                }
            } finally {
                httpPost.releaseConnection();
            }
        } catch (Exception e) {
            this.log.info("\n【请求地址】：{}\n【请求数据】：{}\n【异常信息】：{}", url, requestStr, e.getMessage());
            throw new WxPayException(e.getMessage(), e);
        }

    }

    @Override
    public byte[] postForBytes(String url, String requestStr, boolean useKey) throws WxPayException {
        try {
            HttpClientBuilder httpClientBuilder = createHttpClientBuilder(useKey);
            HttpPost httpPost = this.createHttpPost(url, requestStr);
            try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    final byte[] bytes = EntityUtils.toByteArray(response.getEntity());
                    final String responseData = Base64.getEncoder().encodeToString(bytes);
                    this.log.info("\n【请求地址】：{}\n【请求数据】：{}\n【响应数据(Base64编码后)】：{}", url, requestStr, responseData);
                    return bytes;
                }
            } finally {
                httpPost.releaseConnection();
            }
        } catch (Exception e) {
            this.log.error("\n【请求地址】：{}\n【请求数据】：{}\n【异常信息】：{}", url, requestStr, e.getMessage());
            throw new WxPayException(e.getMessage(), e);
        }
    }

}
