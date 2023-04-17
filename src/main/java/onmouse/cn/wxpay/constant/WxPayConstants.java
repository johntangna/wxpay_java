package onmouse.cn.wxpay.constant;

import com.google.common.collect.Lists;

import java.util.List;

public class WxPayConstants {

    /**
     * 账户类型
     */
    public static class AccountType {
        /**
         * 基本账户
         */
        public static final String BASIC = "Basic";
        /**
         * 运营账户
         */
        public static final String OPERATION = "Operation";
        /**
         * Fees
         */
        public static final String FEES = "Fees";
    }

    /**
     * 压缩账单的类型.
     */
    public static class TarType {
        /**
         * 固定值：GZIP，返回格式为.gzip的压缩包账单.
         */
        public static final String GZIP = "GZIP";
    }

    /**
     * 交易账单类型
     */
    public static class BillType {
        /**
         * 所有订单（不含充值退款）
         */
        public static final String ALL = "ALL";

        /**
         * 成功支付的订单（不含充值退款）
         */
        public static final String SUCCESS = "SUCCESS";

        /**
         * 退款订单（不含充值退款）
         */
        public static final String REFUND = "REFUND";

        /**
         * 返回充值退款订单
         */
        public static final String RECHARGE_REFUND = "RECHARGE_REFUND";
    }

    /**
     * 交易类型.
     */
    public static class TradeType {
        /**
         * 原生扫码支付.
         */
        public static final String NATIVE = "NATIVE";

        /**
         * App支付.
         */
        public static final String APP = "APP";

        /**
         * 公众号支付/小程序支付.
         */
        public static final String JSAPI = "JSAPI";

        /**
         * H5支付.
         */
        public static final String MWEB = "MWEB";

        /**
         * 刷卡支付.
         * 刷卡支付有单独的支付接口，不调用统一下单接口
         */
        public static final String MICROPAY = "MICROPAY";

    }

    /**
     * 业务结果代码
     */
    public static class ResultCode {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAIL = "FAIL";
    }

    /**
     * 退款资金来源.
     */
    public static class RefundAccountSource {
        /**
         * 可用余额退款/基本账户.
         */
        public static final String RECHARGE_FUNDS = "REFUND_SOURCE_RECHARGE_FUNDS";

        /**
         * 未结算资金退款.
         */
        public static final String UNSETTLED_FUNDS = "REFUND_SOURCE_UNSETTLED_FUNDS";

    }

    /**
     * 签名类型
     */
    public static class SignType {
        public static final String HMAC_SHA256 = "HMAC-SHA256";

        public static final String MD5 = "MD5";

        public static final List<String> ALL_SIGN_TYPES = Lists.newArrayList(HMAC_SHA256, MD5);
    }
}
