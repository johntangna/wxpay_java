package onmouse.cn.wxpay.bean;

import onmouse.cn.wxpay.bean.result.BaseWxPayResult;
import onmouse.cn.wxpay.bean.result.WxPayOrderQueryResult;
import org.junit.jupiter.api.Test;

class TestBaseWxPayResult {
    @Test
    public void test_fentoyuan() {
        System.out.println(BaseWxPayResult.fenToYuan(233));
    }

    @Test
    public void testFromXml() {
        String str = "<xml><return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<return_msg><![CDATA[OK]]></return_msg>\n" +
                "<result_code><![CDATA[SUCCESS]]></result_code>\n" +
                "<mch_id><![CDATA[1613402222]]></mch_id>\n" +
                "<appid><![CDATA[wx3077e7b6cc7421d2]]></appid>\n" +
                "<openid><![CDATA[oJCJw5PeXIi3PlmBNjagymjCAe_c]]></openid>\n" +
                "<is_subscribe><![CDATA[Y]]></is_subscribe>\n" +
                "<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "<trade_state><![CDATA[SUCCESS]]></trade_state>\n" +
                "<bank_type><![CDATA[OTHERS]]></bank_type>\n" +
                "<total_fee>1</total_fee>\n" +
                "<fee_type><![CDATA[CNY]]></fee_type>\n" +
                "<cash_fee>1</cash_fee>\n" +
                "<cash_fee_type><![CDATA[CNY]]></cash_fee_type>\n" +
                "<transaction_id><![CDATA[4200001753202303298031899723]]></transaction_id>\n" +
                "<out_trade_no><![CDATA[wx372344261984649216]]></out_trade_no>\n" +
                "<attach><![CDATA[product]]></attach>\n" +
                "<time_end><![CDATA[20230329112321]]></time_end>\n" +
                "<trade_state_desc><![CDATA[支付成功]]></trade_state_desc>\n" +
                "<nonce_str><![CDATA[HF0wAyQ4rUFk5yKP]]></nonce_str>\n" +
                "<sign><![CDATA[1E5F5582E6456FCA6076C08B64506149]]></sign>\n" +
                "</xml>";
        WxPayOrderQueryResult wxPayOrderQueryResult = BaseWxPayResult.fromXml(str, WxPayOrderQueryResult.class);
        System.out.println(wxPayOrderQueryResult);
    }
}
