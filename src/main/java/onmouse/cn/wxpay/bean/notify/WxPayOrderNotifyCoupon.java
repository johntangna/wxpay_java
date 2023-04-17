package onmouse.cn.wxpay.bean.notify;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.common.util.json.WxGsonBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * 支付异步通知代金券通知
 */
@Data
@NoArgsConstructor
public class WxPayOrderNotifyCoupon implements Serializable {
    private static final long serialVersionUID = 769344972909931297L;

    private String CouponId;
    private String CouponType;
    private Integer CouponFee;

    public Map<String, String> toMap(int index) {
        Map<String, String> map = Maps.newHashMap();
        map.put("coupon_id_" + index, this.getCouponId());
        map.put("coupon_type_" + index, this.getCouponType());
        map.put("coupon_fee_" + index, this.getCouponFee() + "");
        return map;
    }

    @Override
    public String toString() {
        return WxGsonBuilder.create().toJson(this);
    }
}
