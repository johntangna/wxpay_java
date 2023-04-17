package onmouse.cn.wxpay.bean.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 微信支付统一下单后
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxPayNativeOrderResult implements Serializable {

    private static final long serialVersionUID = 7871828559927994877L;

    private String codeUrl;

}
