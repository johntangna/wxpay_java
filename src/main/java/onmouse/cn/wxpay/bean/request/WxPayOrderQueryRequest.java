package onmouse.cn.wxpay.bean.request;

import onmouse.cn.wxpay.exception.WxPayException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(builderClassName = "newBuilder")
@NoArgsConstructor
@AllArgsConstructor
@XStreamAlias("xml")
public class WxPayOrderQueryRequest extends BaseWxPayRequest{

    private static final long serialVersionUID = -1234934040557333967L;

    /**
     * 微信订单号
     */
    @XStreamAlias("transaction_id")
    private String transactionId;

    /**
     * 商户订单号
     */
    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @Override
    protected void checkConstraints() throws WxPayException {
        if((StringUtils.isBlank(transactionId) && StringUtils.isBlank(outTradeNo))
            || (StringUtils.isNotBlank(transactionId)) && StringUtils.isNotBlank(outTradeNo)){
            throw new WxPayException("transaction_id和out_trade_no不能同时为空或者同时存在，只能二选一");
        }
    }

    @Override
    protected void storeMap(Map<String, String> map) {
        map.put("transaction_id", transactionId);
        map.put("out_trade_no", outTradeNo);
    }

}
