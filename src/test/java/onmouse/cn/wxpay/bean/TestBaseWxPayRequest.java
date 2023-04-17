package onmouse.cn.wxpay.bean;

import onmouse.cn.wxpay.bean.request.WxPayOrderQueryRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TestBaseWxPayRequest {

    @Test
    public void testYuanToFen() {
        System.out.println(new BigDecimal(233).setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue());
    }

    @Test
    public void testToXML() {
        WxPayOrderQueryRequest baseWxPayRequest = new WxPayOrderQueryRequest();
        System.out.println(baseWxPayRequest.toXML());
    }

    @Test
    public void testCreateNonceStr(){
        System.out.println(String.valueOf(System.currentTimeMillis()));
    }
}
