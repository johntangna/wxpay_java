package onmouse.cn.wxpay.bean.request;

import onmouse.cn.wxpay.exception.WxPayException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import me.chanjar.weixin.common.annotation.Required;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(builderMethodName = "newBuilder")
@NoArgsConstructor
@AllArgsConstructor
@XStreamAlias("xml")
public class WxPayReportRequest extends BaseWxPayRequest{
    private static final long serialVersionUID = -4488912463466094898L;

    /**
     * <pre>
     *     字段名：接口URL
     *     变量名：interface_url
     *     是否必填：为必填
     *     数据类型：String(127)
     *     示例值：https://api.mch.weixin.qq.com/pay/unifiedorder
     *     描述：报对应的接口的完整URL，类似：
     *          https://api.mch.weixin.qq.com/pay/unifiedorder
     *          对于刷卡支付，为更好的和商户共同分析一次业务行为的整体耗时情况，对于两种接入模式，请都在门店侧对一次刷卡支付进行一次单独的整体上报，上报URL指定为：
     *          https://api.mch.weixin.qq.com/pay/micropay/total
     *          关于两种接入模式具体可参考本文档章节：刷卡支付商户接入模式
     *          其它接口调用仍然按照调用一次，上报一次来进行
     * </pre>
     */
    @Required
    @XStreamAlias("interface_url")
    private String interfaceUrl;

    /**
     * 接口耗时
     * 必填
     * 接口耗时情况，单位为毫秒
     * 注意：该参数最后带有下划线“_”，参数设计如此，非文档问题
     */
    @Required
    @XStreamAlias("execute_time_")
    private Integer executeTime;

    @Required
    @XStreamAlias("return_code")
    private String returnCode;

    @Required
    @XStreamAlias("return_msg")
    private String returnMsg;

    @Required
    @XStreamAlias("result_code")
    private String resultCode;

    @Required
    @XStreamAlias("user_ip")
    private String userIp;

    @XStreamAlias("err_code")
    private String errCode;

    @XStreamAlias("err_code_des")
    private String errCodeDes;

    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @XStreamAlias("time")
    private String time;


    @Override
    protected void checkConstraints() throws WxPayException {

    }

    @Override
    protected void storeMap(Map<String, String> map) {
        map.put("interface_url", interfaceUrl);
        map.put("execute_time_", executeTime.toString());
        map.put("return_code", returnCode);
        map.put("return_msg", returnMsg);
        map.put("result_code", resultCode);
        map.put("user_ip", userIp);
        map.put("err_code", errCode);
        map.put("err_code_des", errCodeDes);
        map.put("out_trade_no", outTradeNo);
        map.put("time", time);
    }
}
