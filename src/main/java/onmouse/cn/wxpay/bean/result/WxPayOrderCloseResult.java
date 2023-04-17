package onmouse.cn.wxpay.bean.result;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;

import java.io.Serializable;

/**
 * 关闭订单结果对象类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XStreamAlias("xml")
public class WxPayOrderCloseResult extends BaseWxPayResult implements Serializable {
    private static final long serialVersionUID = 800873502890274834L;
    /**
     * 业务结果描述
     */
    @XStreamAlias("result_msg")
    private String resultMsg;

    /**
     * 从XML结构中加载额外的熟悉
     *
     * @param d Document
     */
    @Override
    protected void loadXml(Document d) {
        resultMsg = readXmlString(d, "result_msg");
    }

}

