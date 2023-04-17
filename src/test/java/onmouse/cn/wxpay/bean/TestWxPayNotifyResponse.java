package onmouse.cn.wxpay.bean;

import onmouse.cn.wxpay.bean.notify.WxPayNotifyResponse;
import org.junit.jupiter.api.Test;

public class TestWxPayNotifyResponse {

    @Test
    public void testSuccess() {
        final String xml = WxPayNotifyResponse.success("OK");

        System.out.println(xml);
    }
}
