package onmouse.cn.wxpay.bean.result;

import onmouse.cn.wxpay.constant.WxPayConstants;
import onmouse.cn.wxpay.exception.WxPayException;
import onmouse.cn.wxpay.service.WxPayService;
import onmouse.cn.wxpay.utils.SignUtils;
import onmouse.cn.wxpay.utils.XmlConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.security.AnyTypePermission;
import lombok.Data;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.common.util.json.WxGsonBuilder;
import me.chanjar.weixin.common.util.xml.XStreamInitializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 微信支付结果通用属性类
 */
@Data
public abstract class BaseWxPayResult {
    /**
     * 返回状态码
     */
    @XStreamAlias("return_code")
    protected String returnCode;

    /**
     * 返回信息
     */
    @XStreamAlias("return_msg")
    protected String returnMsg;

    // 当return_code为true，还会返回以下字段
    /**
     * 业务结果
     */
    @XStreamAlias("result_code")
    private String resultCode;

    /**
     * 错误代码
     */
    @XStreamAlias("err_code")
    private String errCode;

    /**
     * 错误代码描述
     */
    @XStreamAlias("err_code_des")
    private String errCodeDes;

    /**
     * 公众账号ID.
     */
    @XStreamAlias("appid")
    private String appid;
    /**
     * 商户号.
     */
    @XStreamAlias("mch_id")
    private String mchId;
    /**
     * 服务商模式下的子公众账号ID.
     */
    @XStreamAlias("sub_appid")
    private String subAppId;
    /**
     * 服务商模式下的子商户号.
     */
    @XStreamAlias("sub_mch_id")
    private String subMchId;
    /**
     * 随机字符串.
     */
    @XStreamAlias("nonce_str")
    private String nonceStr;
    /**
     * 签名.
     */
    @XStreamAlias("sign")
    private String sign;

    // 以下为辅助属性
    /**
     * xml字符串
     */
    private String xmlString;

    /**
     * xml的document对象，用于解析xml文本
     */
    private transient Document xmlDoc;

    /**
     * Gets logger.
     *
     * @return the logger
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String toString() {
        return WxGsonBuilder.create().toJson(this);
    }


    /**
     * 将bean通过保存的xml字符串转换成map
     */
    public Map<String, String> toMap() {
        if (StringUtils.isBlank(this.xmlString)) {
            throw new WxRuntimeException("xml数据有问题，请核实!");
        }

        Map<String, String> result = Maps.newHashMap();
        Document doc = this.getXmlDoc();

        try {
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath()
                    .compile("/xml/*")
                    .evaluate(doc, XPathConstants.NODESET);
            int len = nodeList.getLength();
            for (int i = 0; i < len; i++) {
                result.put(nodeList.item(i).getNodeName(), nodeList.item(i).getTextContent());
            }
        } catch (XPathExpressionException e) {
            throw new WxRuntimeException("非法的xml文本内容" + xmlString);
        }

        return result;
    }

    /**
     * 将分转换成元
     */
    public static String fenToYuan(Integer fen) {
        return BigDecimal.valueOf(Double.valueOf(fen) / 100).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * 从xml字符串转换成bean对象
     */
    public static <T extends BaseWxPayResult> T fromXml(String xmlString, Class<T> clz) {
        if (XmlConfig.fastMode) {
            try {
                BaseWxPayResult baseWxPayResult = clz.newInstance();
                baseWxPayResult.setXmlString(xmlString);
                Document doc = baseWxPayResult.getXmlDoc();
                baseWxPayResult.loadBasicXML(doc);
                baseWxPayResult.loadXml(doc);
                return (T) baseWxPayResult;
            } catch (Exception e) {
                throw new WxRuntimeException("parse xml error", e);
            }
        }
        XStream xStream = XStreamInitializer.getInstance();
        xStream.addPermission(AnyTypePermission.ANY);
        xStream.processAnnotations(clz);
        xStream.setClassLoader(BaseWxPayResult.class.getClassLoader());
        T result = (T) xStream.fromXML(xmlString);
        result.setXmlString(xmlString);
        return result;
    }

    /**
     * 加载xml中属性，供子类覆盖加载额外的属性
     *
     * @param d
     */
    protected abstract void loadXml(Document d);

    /**
     * 从XML文档中加载基础属性
     *
     * @param d Document
     */
    private void loadBasicXML(Document d) {
        returnCode = readXmlString(d, "return_code");
        returnMsg = readXmlString(d, "return_msg");
        resultCode = readXmlString(d, "result_code");
        errCode = readXmlString(d, "err_code");
        errCodeDes = readXmlString(d, "err_code_des");
        appid = readXmlString(d, "appid");
        mchId = readXmlString(d, "mch_id");
        subAppId = readXmlString(d, "sub_appid");
        subMchId = readXmlString(d, "sub_mch_id");
        nonceStr = readXmlString(d, "nonce_str");
        sign = readXmlString(d, "sign");
    }

    /**
     * 读取xml中字符串
     */
    public static String readXmlString(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }

        Node node = nodeList.item(0).getFirstChild();
        if (node == null) {
            return null;
        }
        return node.getNodeValue();
    }

    /**
     * 读取xml中整数类型
     */
    public static Integer readXmlInteger(Document document, String tagName) {
        String content = readXmlString(document, tagName);
        if (content == null || content.trim().length() == 0) {
            return null;
        }
        return Integer.parseInt(content);
    }

    protected Document openXML(String content) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new WxRuntimeException("非法的xml文本内容：\n" + this.xmlString, e);
        }
    }

    /**
     * 将xml字符串转换成Document对象，以便读取其元素值.
     */
    private Document getXmlDoc() {
        if (this.xmlDoc != null) {
            return this.xmlDoc;
        }
        xmlDoc = openXML(xmlString);
        return xmlDoc;
    }

    /**
     * 获取xml中元素的值.
     *
     * @param path the path
     * @return the xml value
     */
    protected String getXmlValue(String... path) {
        Document doc = this.getXmlDoc();
        String expression = String.format("/%s//text()", Joiner.on("/").join(path));
        try {
            return (String) XPathFactory
                    .newInstance()
                    .newXPath()
                    .compile(expression)
                    .evaluate(doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new WxRuntimeException("未找到相应路径的文本：" + expression);
        }
    }

    /**
     * 获取xml中元素的值，作为int值返回.
     *
     * @param path the path
     * @return the xml value as int
     */
    protected Integer getXmlValueAsInt(String... path) {
        String result = this.getXmlValue(path);
        if (StringUtils.isBlank(result)) {
            return null;
        }

        return Integer.valueOf(result);
    }

    /**
     * 校验返回结果签名
     */
    public void checkResult(WxPayService wxPayService, String signType, boolean checkSuccess) throws WxPayException {
        Map<String, String> map = toMap();
        if (getSign() != null && !SignUtils.checkSign(map, signType, wxPayService.getConfig().getMchKey())) {
            this.getLogger().debug("校验结果签名失败，参数：{}", map);
            throw new WxPayException("参数格式校验错误！");
        }

        /**
         * 检验结果是否成功
         */
        if (checkSuccess) {
            List<String> successString = Lists.newArrayList(WxPayConstants.ResultCode.SUCCESS, "");
            if (!successString.contains(StringUtils.trimToNull(getReturnCode()).toUpperCase())
                    || !successString.contains(StringUtils.trimToNull(getResultCode()).toUpperCase())) {
                StringBuilder errorMessage = new StringBuilder();
                if (getReturnCode() != null) {
                    errorMessage.append("返回代码：").append(getReturnCode());
                }
                if (getReturnMsg() != null) {
                    errorMessage.append(",返回信息：").append(getReturnMsg());
                }
                if (getResultCode() != null) {
                    errorMessage.append(",结果代码：").append(getResultCode());
                }
                if (getErrCode() != null) {
                    errorMessage.append(",错误代码：").append(getErrCode());
                }
                if (getErrCodeDes() != null) {
                    errorMessage.append(",错误详情：").append(getErrCodeDes());
                }
                this.getLogger().error("\n结果业务代码异常，返回结果：{}, \n{}", map, errorMessage.toString());
                throw WxPayException.from(this);
            }
        }
    }
}
