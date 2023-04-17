package onmouse.cn.wxpay.bean.request;

import onmouse.cn.wxpay.constant.WxPayConstants;
import onmouse.cn.wxpay.exception.WxPayException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.*;
import me.chanjar.weixin.common.annotation.Required;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * 微信下载交易账单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder(builderMethodName = "newBuilder")
@AllArgsConstructor
@NoArgsConstructor
@XStreamAlias("xml")
public class WxPayDownloadBillRequest extends BaseWxPayRequest {
    private static final long serialVersionUID = 9190849552319938706L;

    private static final String[] BILL_TYPES = new String[]{
            WxPayConstants.BillType.ALL,
            WxPayConstants.BillType.SUCCESS,
            WxPayConstants.BillType.REFUND,
            WxPayConstants.BillType.RECHARGE_REFUND
    };

    private static final String TAR_TYPE = "GZIP";

    @Required
    @XStreamAlias("bill_date")
    private String billDate;

    @Required
    @XStreamAlias("bill_type")
    private String billType;

    @XStreamAlias("tar_type")
    private String tarType;

    @Override
    protected void checkConstraints() throws WxPayException {
        if (StringUtils.isNotBlank(this.getTarType()) && !TAR_TYPE.equals(this.getTarType())) {
            throw new WxPayException("tar_type如果存在，只能为GZIP");
        }

        if (!ArrayUtils.contains(BILL_TYPES, this.getBillType())) {
            throw new WxPayException(
                    String.format("bill_type目前必须为%s其中之一，实际值：%s",
                            Arrays.toString(BILL_TYPES),
                            this.getBillType()
                    )
            );
        }
    }

    @Override
    protected void storeMap(Map<String, String> map) {
        map.put("bill_date", billDate);
        map.put("bill_type", billType);
        map.put("tar_type", tarType);
    }
}
