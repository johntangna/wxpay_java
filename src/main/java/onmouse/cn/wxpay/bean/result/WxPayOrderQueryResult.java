package onmouse.cn.wxpay.bean.result;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import org.w3c.dom.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 查询订单，返回结果对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@XStreamAlias("xml")
public class WxPayOrderQueryResult extends BaseWxPayResult implements Serializable {

    private static final long serialVersionUID = 4544705643278079395L;

    /**
     * 字段名：营销详情.
     */
    @XStreamAlias("promotion_detail")
    private String promotionDetail;

    /**
     * 设备号
     */
    @XStreamAlias("device_info")
    private String deviceInfo;

    /**
     * 用户标识
     */
    @XStreamAlias("openid")
    private String openid;

    /**
     * 是否关注公众账号
     */
    @XStreamAlias("is_subscribe")
    private String isSubscribe;

    /**
     * 用户子标识	.
     */
    @XStreamAlias("sub_openid")
    private String subOpenid;

    /**
     * 是否关注子公众账号.
     */
    @XStreamAlias("sub_is_subscribe")
    private String isSubscribeSub;

    /**
     * 交易类型.
     */
    @XStreamAlias("trade_type")
    private String tradeType;

    /**
     * 交易状态.
     */
    @XStreamAlias("trade_state")
    private String tradeState;

    /**
     * 付款银行.
     */
    @XStreamAlias("bank_type")
    private String bankType;

    /**
     * 商品详情.
     * 商品详细列表，使用Json格式，传输签名前请务必使用CDATA标签将JSON文本串保护起来。如果使用了单品优惠，会有单品优惠信息返回
     * <p>
     * discount_detail []：
     * └ goods_id String 必填 32 商品的编号
     * └ goods_name String 必填 256 商品名称
     * └ coupon_batch_id String 必填 代金券批次ID
     * └ coupon_id String 必填 代金卷ID
     * └ coupon_fee Int 必填 代金券支付金额，单位为分
     * </pre>
     **/
    @XStreamAlias("detail")
    private String detail;

    /**
     * 订单金额.
     */
    @XStreamAlias("total_fee")
    private Integer totalFee;

    /**
     * 货币种类.
     */
    @XStreamAlias("fee_type")
    private String feeType;

    /**
     * 应结订单金额.
     */
    @XStreamAlias("settlement_total_fee")
    private Integer settlementTotalFee;

    /**
     * 现金支付金额.
     */
    @XStreamAlias("cash_fee")
    private Integer cashFee;

    /**
     * 现金支付货币类型.
     */
    @XStreamAlias("cash_fee_type")
    private String cashFeeType;

    /**
     * 代金券金额.
     */
    @XStreamAlias("coupon_fee")
    private Integer couponFee;

    /**
     * 代金券使用数量.
     */
    @XStreamAlias("coupon_count")
    private Integer couponCount;

    private List<Coupon> coupons;

    /**
     * 微信支付订单号.
     */
    @XStreamAlias("transaction_id")
    private String transactionId;

    /**
     * 商户订单号.
     */
    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    /**
     * 附加数据.
     */
    @XStreamAlias("attach")
    private String attach;

    /**
     * 支付完成时间.
     */
    @XStreamAlias("time_end")
    private String timeEnd;

    /**
     * 交易状态描述.
     */
    @XStreamAlias("trade_state_desc")
    private String tradeStateDesc;

    /**
     * 通过xml组装coupons属性内容
     */
    public void composeCoupons() {
        if (this.couponCount != null && this.couponCount > 0) {
            this.coupons = Lists.newArrayList();
            for (int i = 0; i < this.couponCount; i++) {
                this.coupons.add(new Coupon(this.getXmlValue("xml/coupon_type_" + i),
                        this.getXmlValue("xml/coupon_id_" + i),
                        this.getXmlValueAsInt("xml/coupon_fee_" + i)));
            }
        }
    }

    @Override
    protected void loadXml(Document d) {
        promotionDetail = readXmlString(d, "promotion_detail");
        deviceInfo = readXmlString(d, "device_info");
        openid = readXmlString(d, "openid");
        isSubscribe = readXmlString(d, "is_subscribe");
        tradeType = readXmlString(d, "trade_type");
        tradeState = readXmlString(d, "trade_state");
        bankType = readXmlString(d, "bank_type");
        totalFee = readXmlInteger(d, "total_fee");
        settlementTotalFee = readXmlInteger(d, "settlement_total_fee");
        feeType = readXmlString(d, "fee_type");
        cashFee = readXmlInteger(d, "cash_fee");
        cashFeeType = readXmlString(d, "cash_fee_type");
        couponFee = readXmlInteger(d, "coupon_fee");
        couponCount = readXmlInteger(d, "coupon_count");
        this.transactionId = readXmlString(d, "transaction_id");
        this.outTradeNo = readXmlString(d, "out_trade_no");
        this.attach = readXmlString(d, "attach");
        this.timeEnd = readXmlString(d, "time_end");
        this.tradeStateDesc = readXmlString(d, "trade_state_desc");
    }

    @Data
    @Builder(builderMethodName = "newBuilder")
    @AllArgsConstructor
    public static class Coupon implements Serializable {

        private static final long serialVersionUID = 4798572569224130663L;

        /**
         * 代金券类型.
         */
        private String couponType;

        /**
         * 代金券ID.
         */
        private String couponId;

        /**
         * 单个代金券支付金额.
         */
        private Integer couponFee;
    }
}
