package onmouse.cn.wxpay.bean.notify;

import onmouse.cn.wxpay.bean.result.BaseWxPayResult;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XStreamAlias("xml")
public class WxScanPayNotifyResult extends BaseWxPayResult implements Serializable {
    private static final long serialVersionUID = 3381324564266118986L;

    /**
     * 用户标识.
     */
    @XStreamAlias("openid")
    private String openid;

    /**
     * <pre>
     * 是否关注公众账号.
     * 仅在公众账号类型支付有效，取值范围：Y或N;Y-关注;N-未关注
     * </pre>
     */
    @XStreamAlias("is_subscribe")
    private String isSubscribe;

    /**
     * <pre>
     * 商品ID.
     * 商户定义的商品id 或者订单号
     * </pre>
     */
    @XStreamAlias("product_id")
    private String productId;

    @Override
    protected void loadXml(Document d) {
        openid = readXmlString(d, "openid");
        isSubscribe = readXmlString(d, "is_subscribe");
        productId = readXmlString(d, "product_id");
    }

}
