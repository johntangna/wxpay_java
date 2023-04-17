package onmouse.cn.wxpay.utils;

import onmouse.cn.wxpay.bean.request.WxPayOrderQueryRequest;
import onmouse.cn.wxpay.constant.WxPayConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

public class TestUtils {

    @Test
    public void testUtils() {
        String[] str = new String[0];
        WxPayOrderQueryRequest wxPayOrderQueryRequest = new WxPayOrderQueryRequest();
        wxPayOrderQueryRequest.setAppid("wx3077e7b6cc7421d2");
        wxPayOrderQueryRequest.setMchId("1613402222");
        wxPayOrderQueryRequest.setNonceStr(String.valueOf(System.currentTimeMillis()));
        wxPayOrderQueryRequest.setTransactionId("234234");
        wxPayOrderQueryRequest.setOutTradeNo("23213");
        System.out.println(SignUtils.createSign(WxPayOrderQueryRequest.class, "HMAC-SHA256", "1613402222", str));
    }

    @Test
    public void testBillTypes() {
        String[] BILL_TYPES = new String[]{
                WxPayConstants.BillType.ALL,
                WxPayConstants.BillType.SUCCESS,
                WxPayConstants.BillType.REFUND,
                WxPayConstants.BillType.RECHARGE_REFUND
        };
        assert ArrayUtils.contains(BILL_TYPES, "REFUND");
        System.out.println(ArrayUtils.contains(BILL_TYPES, "REFUND"));
    }
}
