package onmouse.cn.wxpay.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 微信支付http proxy 正向代理配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxPayHttpProxy implements Serializable {
    /**
     * 代理主机
     */
    private String httpProxyHost;

    /**
     * 代理端口
     */
    private Integer httpProxyPort;

    /**
     * 代理用户名
     */
    private String httpProxyUsername;

    /**
     * 代理密码
     */
    private String httpProxyPassword;
}
