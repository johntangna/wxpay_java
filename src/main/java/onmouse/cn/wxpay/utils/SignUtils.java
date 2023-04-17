package onmouse.cn.wxpay.utils;

import onmouse.cn.wxpay.bean.request.BaseWxPayRequest;
import onmouse.cn.wxpay.bean.result.BaseWxPayResult;
import onmouse.cn.wxpay.constant.WxPayConstants.SignType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 签名相关工具类
 */
public class SignUtils {

    private static Logger log = LoggerFactory.getLogger(BeanUtils.class);

    /**
     * 签名时不携带的参数
     */
    private static final List<String> NO_SIGN_PARAMS = Lists.newArrayList("sign", "key", "xmlString", "xmlDoc", "couponList");

    /**
     * 微信支付签名算法
     */
    public static String createSign(Object xmlBean, String signType, String signKey, String[] ignoredParams) {
        Map<String, String> map = null;

        if (XmlConfig.fastMode) {
            if (xmlBean instanceof BaseWxPayRequest) {
                map = ((BaseWxPayRequest) xmlBean).getSignParams();
            }
        }

        if (map == null) {
            map = xmlBean2Map(xmlBean);
        }

        return createSign(map, signType, signKey, ignoredParams);
    }

    /**
     * 微信支付签名算法
     */
    public static String createSign(Map<String, String> params, String signType, String signKey, String[] ignoredParams) {
        StringBuilder toSign = new StringBuilder();
        for (String key:
             new TreeMap<>(params).keySet()) {
            String value = params.get(key);
            boolean shouldSign = StringUtils.isNotEmpty(value) && !ArrayUtils.contains(ignoredParams, key)
                    && !NO_SIGN_PARAMS.contains(key);

            if(shouldSign) {
                toSign.append(key).append("=").append(value).append("&");
            }
        }

        toSign.append("key=").append(signKey);
        if (SignType.HMAC_SHA256.equals(signType)) {
            return me.chanjar.weixin.common.util.SignUtils.createHmacSha256Sign(toSign.toString(), signKey);
        } else {
            return DigestUtils.md5Hex(toSign.toString()).toUpperCase();
        }
    }

    /**
     * 检验签名是否正确
     */
    public static boolean checkSign(Map<String, String> params, String signType, String signKey) {
        String sign = createSign(params, signType, signKey, new String[0]);
        return sign.equals(params.get("sign"));
    }

    /**
     * 将bean按照@XStreamAlias标识的字符串内容生成以之为key的map对象
     */
    public static Map<String, String> xmlBean2Map(Object bean) {
        Map<String, String> result = Maps.newHashMap();
        List<Field> fields = new ArrayList<>(Arrays.asList(bean.getClass().getDeclaredFields()));

        fields.addAll(Arrays.asList(bean.getClass().getSuperclass().getDeclaredFields()));
        if (bean.getClass().getSuperclass().getSuperclass() == BaseWxPayRequest.class) {
            fields.addAll(Arrays.asList(BaseWxPayRequest.class.getDeclaredFields()));
        }

        if (bean.getClass().getSuperclass().getSuperclass() == BaseWxPayResult.class) {
            fields.addAll(Arrays.asList(BaseWxPayResult.class.getDeclaredFields()));
        }

        for (Field field:
             fields) {
            try {
                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                if (field.get(bean) == null) {
                    field.setAccessible(isAccessible);
                    continue;
                }

                if (field.isAnnotationPresent(XStreamAlias.class)) {
                    result.put(field.getAnnotation(XStreamAlias.class).value(), field.get(bean).toString());
                } else if (!Modifier.isStatic(field.getModifiers())) {
                    //忽略掉静态成员变量
                    result.put(field.getName(), field.get(bean).toString());
                }

                field.setAccessible(isAccessible);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        return result;
    }
}
