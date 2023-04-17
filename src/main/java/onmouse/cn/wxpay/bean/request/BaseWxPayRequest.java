package onmouse.cn.wxpay.bean.request;

import onmouse.cn.wxpay.config.WxPayConfig;
import onmouse.cn.wxpay.exception.WxPayException;
import onmouse.cn.wxpay.utils.BeanUtils;
import onmouse.cn.wxpay.utils.SignUtils;
import onmouse.cn.wxpay.utils.XmlConfig;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.experimental.Accessors;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.common.util.xml.XStreamInitializer;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static onmouse.cn.wxpay.constant.WxPayConstants.SignType.ALL_SIGN_TYPES;

/**
 * 微信支付请求对象共用的参数存放类
 */
@Data
@Accessors(chain = true)
public abstract class BaseWxPayRequest implements Serializable {

    private static final long serialVersionUID = -5924414484097374911L;

    /**
     * 字段名：公众号id
     * 变量名：appid
     * 是否必填：是
     */
    @XStreamAlias("appid")
    protected String appid;

    /**
     * 字段名：商户号
     * 变量名：mch_id
     * 是否必填：是
     */
    @XStreamAlias("mch_id")
    protected String mchId;

    /**
     * 字段名：服务商模式下的子商户公众号id
     * 变量名：sub_appid
     * 是否必填：是
     */
    @XStreamAlias("sub_appid")
    protected String subAppId;

    /**
     * 字段名：服务商模式下的子商户号.
     * 变量名：sub_mch_id
     * 是否必填：是
     */
    @XStreamAlias("sub_mch_id")
    protected String subMchId;

    /**
     * 字段名：随机字符串.
     * 变量名：nonce_str
     * 是否必填：是
     */
    @XStreamAlias("nonce_str")
    protected String nonceStr;

    /**
     * 字段名：签名.
     * 变量名：sign
     * 是否必填：是
     */
    @XStreamAlias("sign")
    protected String sign;

    /**
     * 签名类型.
     * sign_type
     * 否
     */
    @XStreamAlias("sign_type")
    private String signType;

    /**
     * 将元转换成分
     */
    public static Integer yuanToFen(String yuan) {
        return new BigDecimal(yuan)
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .intValue();
    }

    private void checkFields() throws WxPayException {
        try {
            BeanUtils.checkRequiredFields(this);
        } catch (WxErrorException e) {
            throw new WxPayException(e.getError().getErrorMsg(), e);
        }

        this.checkConstraints();
    }

    /**
     * 检查约束情况.
     *
     * @throws WxPayException the wx pay exception
     */
    protected abstract void checkConstraints() throws WxPayException;

    /**
     * 是否需要nonce_str
     */
    protected boolean needNonceStr() {
        return true;
    }

    /**
     * 转换成xml字符串
     */
    public String toXML() {
        // 避免将空值传递给微信服务器
        this.setSubAppId(StringUtils.trimToNull(this.getSubAppId()));
        this.setSubMchId(StringUtils.trimToNull(this.getSubMchId()));
        if (XmlConfig.fastMode) {
            return toFastXml();
        }
        XStream xStream = XStreamInitializer.getInstance();
        xStream.processAnnotations(this.getClass());
        return xStream.toXML(this);
    }

    /**
     * 使用快速算法组装xml
     *
     * @return
     */
    private String toFastXml() {
        try {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement(xmlRootTagName());

            Map<String, String> map = getSignParams();
            map.put("sign", sign);
            for (Map.Entry<String, String> entry :
                    map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                Element element = root.addElement(entry.getKey());
                element.addText(entry.getValue());
            }
            return document.asXML();
        } catch (Exception e) {
            throw new WxRuntimeException("generate xml error", e);
        }
    }

    /**
     * 返回xml结构的根节点名称
     *
     * @return 默认返回"xml"，特殊情况可以在子类中覆盖
     */
    protected String xmlRootTagName() {
        return "xml";
    }

    /**
     * 签名时是否忽略appid
     */
    protected boolean ignoreAppid() {
        return false;
    }

    /**
     * 签名时，是否忽略sub_appid.
     *
     */
    protected boolean ignoreSubAppId() {
        return false;
    }

    protected boolean ignoreSubMchId() {
        return false;
    }

    /**
     * 签名时，忽略的参数.
     *
     * @return the string [ ]
     */
    protected String[] getIgnoredParamsForSign() {
        return new String[0];
    }

    /**
     * 获取签名时使用的参数
     */
    public Map<String, String> getSignParams() {
        Map<String, String> map = new HashMap<>(8);
        map.put("appid", appid);
        map.put("mch_id", mchId);
        map.put("sub_appid", subAppId);
        map.put("sub_mch_id", subMchId);
        map.put("nonce_str", nonceStr);
        map.put("sign_type", signType);

        storeMap(map);
        return map;
    }

    /**
     * 将属性组装到一个Map当中，供签名以及最终发送的xml时使用
     */
    protected abstract void storeMap(Map<String, String> map);

    /**
     * 检查参数，并设置签名
     * 1.检查参数（子类实现需要检查参数的功能时，请在调用父类的方法前进行相应判断）
     * 2.补充系统参数，如果未传入则从配置里读取
     * 3.生成签名，并设置进去
     */
    public void checkAndSign(WxPayConfig wxPayConfig) throws WxPayException {
        this.checkFields();

        if (!ignoreAppid()) {
            if (StringUtils.isBlank(getAppid())) {
                this.setAppid(wxPayConfig.getAppId());
            }
        }

        if (StringUtils.isBlank(getMchId())) {
            this.setMchId(wxPayConfig.getMchId());
        }

        if (!ignoreSubAppId()) {
            if (StringUtils.isBlank(getSubAppId())) {
                this.setSubAppId(wxPayConfig.getSubAppId());
            }
        }

        if (!ignoreSubMchId()) {
            if (StringUtils.isBlank(getSubMchId())) {
                this.setSubMchId(wxPayConfig.getSubMchId());
            }
        }

        if (StringUtils.isBlank(getSignType())) {
            if (wxPayConfig.getSignType() != null && !ALL_SIGN_TYPES.contains(wxPayConfig.getSignType())) {
                throw new WxPayException("非法的signType配置：" + wxPayConfig.getSignType() + "，请检查配置！");
            }
            this.setSignType(StringUtils.trimToNull(wxPayConfig.getSignType()));
        } else {
            if (!ALL_SIGN_TYPES.contains(this.getSignType())) {
                throw new WxPayException("非法的sign_type参数：" + this.getSignType());
            }
        }

        if (needNonceStr() && StringUtils.isBlank(getNonceStr())) {
            this.setNonceStr(String.valueOf(System.currentTimeMillis()));
        }

        //设置签名字段的值
        this.setSign(SignUtils.createSign(this, this.getSignType(), wxPayConfig.getMchKey(), this.getIgnoredParamsForSign()));
    }
}
