package onmouse.cn.wxpay.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BaseWxPayServiceTest {

    @Autowired
    private WxPayService payService;

//    @Test
//    public void testCreateOrder_native() throws Exception {
//        WxPayNativeOrderResult result = this.payService
//                .createOrder(WxPayUnifiedOrderRequest.newBuilder()
//                        .body("我去")
//                        .totalFee(1)
//                        .productId("aaa")
//                        .spbillCreateIp("11.1.11.1")
//                        .notifyUrl("111111")
//                        .tradeType(WxPayConstants.TradeType.NATIVE)
//                        .outTradeNo("111111290")
//                        .build());
//        log.info(result.toString());
//    }

    @Test
    public void testReport() throws Exception {
        System.out.println(System.currentTimeMillis());
    }
}
