package onmouse.cn.wxpay.config;

import onmouse.cn.wxpay.constant.WxPayConstants;
import onmouse.cn.wxpay.exception.WxPayException;
import onmouse.cn.wxpay.utils.ResourcesUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;

@Data
@ToString
@EqualsAndHashCode
public class WxPayConfig {
    public static final String DEFAULT_PAY_BASEURL = "https://api.mch.weixin.qq.com";
    private static final String PROBLEM_MSG = "证书文件【%s】有问题，请核实！";
    private static final String NOT_FOUND_MSG = "证书文件【%s】不存在，请核实！";
    private static final String[] NOTIFY_CALLBACK = new String[] {
            ""
    };

    /**
     * 微信支付请求域名部分
     */
    private String payBaseUrl = DEFAULT_PAY_BASEURL;

    /**
     * http请求数据读取超时时间
     */
    private int httpConnectionTimeout = 5000;

    /**
     * http请求数据读取等待时间
     */
    private int httpTimeout = 10000;

    /**
     * 公众号APPID
     */
    private String appId;

    /**
     * 服务商模式下的子商户公众号id
     */
    private String subAppId;

    /**
     * 服务商模式下的子商户号
     */
    private String subMchId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * 商户密钥
     */
    private String mchKey;

    /**
     * 微信支付异步回调地址，通知url必须为直接可访问的url，不能携带参数
     */
    private String notifyUrl;

    /**
     * 交易类型
     * <pre>
     *     JSAPI 公众号支付
     *     NATIVE 原生扫码支付
     *     APP app支付
     * </pre>
     */
    private String tradeType;

    /**
     * 签名方式
     *
     * @see WxPayConstants.SignType
     */
    private String signType;

    private SSLContext sslContext;

    /**
     * p12证书和base64编码
     */
    private String keyString;

    /**
     * p12证书的绝对路径或者以classpath:开头的类路径
     */
    private String keyPath;

    /**
     * apiclient_key.pem证书和base64编码
     */
    private String privateKeyString;

    /**
     * apiclient_key.pem证书或者以classpath:开头的类路径
     */
    private String privateKeyPath;

    /**
     * apiclient_cert.pem证书和base64编码
     */
    private String privateCertString;

    /**
     * apiclient_cert.pem证书或者以classpath:开头的类路径
     */
    private String privateCertPath;

    /**
     * apiclient_key.pem证书文件内容的字节数组
     */
    private byte[] privateKeyContent;

    /**
     * apiclient_cert.pem证书文件内容的字节数组
     */
    private byte[] privateCertContent;

    /**
     * 私钥信息
     */
    private PrivateKey privateKey;

    /**
     * 证书自动更新时间差，默认1min
     */
    private int certAutoUpdateTime = 60;

    /**
     * p12证书文件内容的字节数组
     */
    private byte[] keyContent;

    /**
     * 微信支付是否使用沙箱仿真系统
     * 默认不使用
     */
    private boolean useSandboxEnv = false;

    /**
     * 是否将接口请求日志信息保存在threadLocal中
     * 默认不保存
     */
    private boolean ifSaveApiData = false;

    private String httpProxyHost;
    private Integer httpProxyPort;
    private String httpProxyUsername;
    private String httpProxyPassword;

    /**
     * 返回所设置的微信支付接口请求地址域名
     */
    public String getPayBaseUrl() {
        if (StringUtils.isEmpty(this.payBaseUrl)) {
            return DEFAULT_PAY_BASEURL;
        }

        return this.payBaseUrl;
    }

    /**
     * 初始化ssl
     *
     * @return the ssl context
     * @throws WxPayException the wx pay exception
     */
    public SSLContext initSSLContext() throws WxPayException {
        if (StringUtils.isBlank(this.getMchId())) {
            throw new WxPayException("请确保商户号mchId已设置");
        }

        InputStream inputStream = this.loadConfigInputStream(this.keyString, this.getKeyPath(), this.keyContent, "p12证书");

        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            char[] partnerId2charArray = this.getMchId().toCharArray();
            keystore.load(inputStream, partnerId2charArray);
            this.sslContext = SSLContexts.custom().loadKeyMaterial(keystore, partnerId2charArray).build();
            return this.sslContext;
        } catch (Exception e) {
            throw new WxPayException("证书文件有问题，请核实！", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 初始化一个WxHttpProxy对象
     *
     * @return 返回封装的wxpayhttpProxy对象，如未指定代理主机和端口，则默认返回为null
     */
    public WxPayHttpProxy getWxPayHttpProxy() {
        if (StringUtils.isNotBlank(this.getHttpProxyHost()) && this.getHttpProxyPort() > 0) {
            return new WxPayHttpProxy(this.getHttpProxyHost(), this.getHttpProxyPort(), this.getHttpProxyUsername(), this.getHttpProxyPassword());
        }
        return null;
    }

    public InputStream loadConfigInputStream(String configString, String configPath, byte[] configContent, String fileName) throws WxPayException {
        InputStream inputStream;
        if (configContent != null) {
            inputStream = new ByteArrayInputStream(configContent);
        } else if (StringUtils.isNotEmpty(configString)) {
            configContent = Base64.getDecoder().decode(configString);
            inputStream = new ByteArrayInputStream(configContent);
        } else {
            if (StringUtils.isBlank(configPath)) {
                throw new WxPayException("请确保证书文件地址【" + fileName + "】或者内容已配置");
            }
            inputStream = this.loadConfigInputStream(configPath);
        }
        return inputStream;
    }

    public InputStream loadConfigInputStream(String configPath) throws WxPayException {
        InputStream inputStream;
        final String prefix = "classpath:";
        String fileHasProblemMsg = String.format(PROBLEM_MSG, configPath);
        String fileNotFoundMsg = String.format(NOT_FOUND_MSG, configPath);
        if(configPath.startsWith(prefix)) {
            String path = RegExUtils.removeFirst(configPath, prefix);
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            try {
                inputStream = ResourcesUtils.getResourceAsStream(path);
                if (inputStream == null) {
                    throw new WxPayException(fileNotFoundMsg);
                }
            } catch (Exception exception) {
                throw new WxPayException(fileNotFoundMsg, exception);
            }
        } else if (configPath.startsWith("http://") || configPath.startsWith("https://")) {
            try {
                inputStream = new URL(configPath).openStream();
                if(inputStream == null) {
                    throw new WxPayException(fileNotFoundMsg);
                }
            } catch (IOException ioException) {
                throw new WxPayException(fileNotFoundMsg, ioException);
            }
        } else {
            try {
                File file = new File(configPath);
                if(!file.exists()) {
                    throw new WxPayException(fileNotFoundMsg);
                }
                inputStream = new FileInputStream(file);
            } catch (IOException ioException) {
                throw new WxPayException(fileHasProblemMsg, ioException);
            }
        }
        return inputStream;
    }
}
