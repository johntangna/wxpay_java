package onmouse.cn.wxpay.service;

import onmouse.cn.wxpay.bean.notify.WxPayOrderNotifyResult;
import onmouse.cn.wxpay.bean.notify.WxPayRefundNotifyResult;
import onmouse.cn.wxpay.bean.notify.WxScanPayNotifyResult;
import onmouse.cn.wxpay.bean.order.WxPayNativeOrderResult;
import onmouse.cn.wxpay.bean.request.*;
import onmouse.cn.wxpay.bean.result.*;
import onmouse.cn.wxpay.config.WxPayConfig;
import onmouse.cn.wxpay.exception.WxPayException;

import java.util.Map;

public interface WxPayService {

    /**
     * 获取微信支付请求URL前缀，沙箱环境不一样
     */
    String getWxPayBaseUrl();

    /**
     * 获取配置
     */
    WxPayConfig getConfig();

    /**
     * 设置配置对象
     */
    void setConfig(WxPayConfig wxPayConfig);

    /**
     * 注入多个wxPayConfigMap的对象，并为每个WxPayConfig赋予不同的值
     * 随机采用一个mchId进行http初始化
     * @param wxPayConfigMap
     */
    void setMultiConfig(Map<String, WxPayConfig> wxPayConfigMap);

    /**
     * 注入多个wxPayConfig的对象，并为每个wxPayConfig赋予不同的值
     * 设置一个WxPayConfig所对应的defaultMchId进行http初始化
     */
    void setMultiConfig(Map<String, WxPayConfig> wxPayConfigMap, String defaultMchId);

    /**
     * 进行相应的商户切换.
     *
     * @param mchId 商户标识
     * @return 切换是否成功 boolean
     */
    boolean switchover(String mchId);

    /**
     * 发送post请求，得到响应字符串
     *
     * @param url 请求地址
     * @param requestStr 请求信息
     * @param useKey 是否使用证书
     * @return 返回结果字符串
     * @throws WxPayException 封装的微信支付异常处理
     */
    String post(String url, String requestStr, boolean useKey) throws WxPayException;

    /**
     * 发送post请求，得到响应字节数组.
     *
     * @param url        请求地址
     * @param requestStr 请求信息
     * @param useKey     是否使用证书
     * @return 返回请求结果字节数组 byte [ ]
     * @throws WxPayException the wx pay exception
     */
    byte[] postForBytes(String url, String requestStr, boolean useKey) throws WxPayException;

    /**
     * 查询订单
     */
    WxPayOrderQueryResult queryOrder(String transactionId, String outTradeNo) throws WxPayException;

    /**
     * 查询订单
     * 适用于自定义子商户号和子商户appid的情形
     */
    WxPayOrderQueryResult queryOrder(WxPayOrderQueryRequest wxPayOrderQueryRequest) throws WxPayException;

    /**
     * 调用统一下单接口，并组装生成支付所需参数对象.
     */
    WxPayNativeOrderResult createOrder(WxPayUnifiedOrderRequest request) throws WxPayException;

    /**
     * 统一下单(详见https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1)
     * 在发起微信支付前，需要调用统一下单接口，获取"预支付交易会话标识"
     * 接口地址：https://api.mch.weixin.qq.com/pay/unifiedorder
     *
     * @param request 请求对象，注意一些参数如appid、mchid等不用设置，方法内会自动从配置对象中获取到（前提是对应配置中已经设置）
     * @return the wx pay unified order result
     * @throws WxPayException the wx pay exception
     */
    WxPayUnifiedOrderResult unifiedOrder(WxPayUnifiedOrderRequest request) throws WxPayException;

    /**
     * 解析扫码支付回调通知
     * 详见https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_4
     *
     * @param xmlData  the xml data
     * @param signType 签名类型
     * @return the wx scan pay notify result
     * @throws WxPayException the wx pay exception
     */
    WxScanPayNotifyResult parseScanPayNotifyResult(String xmlData, String signType) throws WxPayException;

    /**
     * 解析扫码支付回调通知
     * 详见https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_4
     *
     * @param xmlData the xml data
     * @return the wx scan pay notify result
     * @throws WxPayException the wx pay exception
     */
    WxScanPayNotifyResult parseScanPayNotifyResult(String xmlData) throws WxPayException;

    /**
     * 解析支付结果通知
     *
     * @param xmlData
     * @return
     * @throws WxPayException
     */
    WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData) throws WxPayException;

    /**
     * 解析支付结果通知.
     * 详见https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_7
     *
     * @param xmlData  the xml data
     * @param signType 签名类型
     * @return the wx pay order notify result
     * @throws WxPayException the wx pay exception
     */
    WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData, String signType) throws WxPayException;


    /**
     * 微信支付-申请退款.
     * 详见 <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4"></a>
     * 接口链接：https://api.mch.weixin.qq.com/secapi/pay/refund
     *
     * @param request 请求对象
     * @return 退款操作结果 wx pay refund result
     * @throws WxPayException the wx pay exception
     */
    WxPayRefundResult refund(WxPayRefundRequest request) throws WxPayException;

    /**
     *  解析退款结果通知
     *  详见
     */
    WxPayRefundNotifyResult parseNotifyRefundResult(String xmlData) throws WxPayException;

    /**
     * 微信支付-查询退款.
     * 应用场景：
     *  提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，用零钱支付的退款20分钟内到账，
     *  银行卡支付的退款3个工作日后重新查询退款状态。
     * 详见 <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_5">https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_5</a>
     * 接口链接：https://api.mch.weixin.qq.com/pay/refundquery
     * </pre>
     * 以下四个参数四选一
     *
     * @param transactionId 微信订单号
     * @param outTradeNo    商户订单号
     * @param outRefundNo   商户退款单号
     * @param refundId      微信退款单号
     * @return 退款信息 wx pay refund query result
     * @throws WxPayException the wx pay exception
     */
    WxPayRefundQueryResult refundQuery(String transactionId, String outTradeNo, String outRefundNo, String refundId)
            throws WxPayException;

    /**
     * <pre>
     * 微信支付-查询退款（适合于需要自定义子商户号和子商户appid的情形）.
     * 应用场景：
     *  提交退款申请后，通过调用该接口查询退款状态。退款有一定延时，用零钱支付的退款20分钟内到账，
     *  银行卡支付的退款3个工作日后重新查询退款状态。
     * 详见 <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_5">https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_5</a>
     * 接口链接：https://api.mch.weixin.qq.com/pay/refundquery
     * </pre>
     *
     * @param request 微信退款单号
     * @return 退款信息 wx pay refund query result
     * @throws WxPayException the wx pay exception
     */
    WxPayRefundQueryResult refundQuery(WxPayRefundQueryRequest request) throws WxPayException;

    /**
     * <pre>
     * 下载对账单.
     * 商户可以通过该接口下载历史交易清单。比如掉单、系统错误等导致商户侧和微信侧数据不一致，通过对账单核对后可校正支付状态。
     * 注意：
     * 1、微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致，bill_type为REVOKED；
     * 2、微信在次日9点启动生成前一天的对账单，建议商户10点后再获取；
     * 3、对账单中涉及金额的字段单位为“元”。
     * 4、对账单接口只能下载三个月以内的账单。
     * 接口链接：<a href="https://api.mch.weixin.qq.com/pay/downloadbill">https://api.mch.weixin.qq.com/pay/downloadbill</a>
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_6">下载对账单</a>
     * </pre>
     *
     * @param billDate   对账单日期 bill_date 下载对账单的日期，格式：20140603
     * @param billType   账单类型 bill_type ALL，返回当日所有订单信息，默认值，SUCCESS，返回当日成功支付的订单，REFUND，返回当日退款订单
     * @param tarType    压缩账单 tar_type 非必传参数，固定值：GZIP，返回格式为.gzip的压缩包账单。不传则默认为数据流形式。
     * @return 对账内容原始字符串 string
     * @throws WxPayException the wx pay exception
     */
    String downloadRawBill(String billDate, String billType, String tarType) throws WxPayException;

    /**
     * <pre>
     * 下载对账单（适合于需要自定义子商户号和子商户appid的情形）.
     * 商户可以通过该接口下载历史交易清单。比如掉单、系统错误等导致商户侧和微信侧数据不一致，通过对账单核对后可校正支付状态。
     * 注意：
     * 1、微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致，bill_type为REVOKED；
     * 2、微信在次日9点启动生成前一天的对账单，建议商户10点后再获取；
     * 3、对账单中涉及金额的字段单位为“元”。
     * 4、对账单接口只能下载三个月以内的账单。
     * 接口链接：https://api.mch.weixin.qq.com/pay/downloadbill
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_6">下载对账单</a>
     * </pre>
     *
     * @param request 下载对账单请求
     * @return 对账内容原始字符串 string
     * @throws WxPayException the wx pay exception
     */
    String downloadRawBill(WxPayDownloadBillRequest request) throws WxPayException;

    /**
     * <pre>
     * 下载对账单.
     * 商户可以通过该接口下载历史交易清单。比如掉单、系统错误等导致商户侧和微信侧数据不一致，通过对账单核对后可校正支付状态。
     * 注意：
     * 1、微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致，bill_type为REVOKED；
     * 2、微信在次日9点启动生成前一天的对账单，建议商户10点后再获取；
     * 3、对账单中涉及金额的字段单位为“元”。
     * 4、对账单接口只能下载三个月以内的账单。
     * 接口链接：https://api.mch.weixin.qq.com/pay/downloadbill
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_6">下载对账单</a>
     * </pre>
     *
     * @param billDate   对账单日期 bill_date 下载对账单的日期，格式：20140603
     * @param billType   账单类型 bill_type ALL，返回当日所有订单信息，默认值，SUCCESS，返回当日成功支付的订单，REFUND，返回当日退款订单
     * @param tarType    压缩账单 tar_type 非必传参数，固定值：GZIP，返回格式为.gzip的压缩包账单。不传则默认为数据流形式。
     * @return WxPayBillResult对象 wx pay bill result
     * @throws WxPayException the wx pay exception
     */
    WxPayBillResult downloadBill(String billDate, String billType, String tarType) throws WxPayException;

    /**
     * <pre>
     * 下载对账单（适合于需要自定义子商户号和子商户appid的情形）.
     * 商户可以通过该接口下载历史交易清单。比如掉单、系统错误等导致商户侧和微信侧数据不一致，通过对账单核对后可校正支付状态。
     * 注意：
     * 1、微信侧未成功下单的交易不会出现在对账单中。支付成功后撤销的交易会出现在对账单中，跟原支付单订单号一致，bill_type为REVOKED；
     * 2、微信在次日9点启动生成前一天的对账单，建议商户10点后再获取；
     * 3、对账单中涉及金额的字段单位为“元”。
     * 4、对账单接口只能下载三个月以内的账单。
     * 接口链接：https://api.mch.weixin.qq.com/pay/downloadbill
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_6">下载对账单</a>
     * </pre>
     *
     * @param request 下载对账单请求
     * @return WxPayBillResult对象 wx pay bill result
     * @throws WxPayException the wx pay exception
     */
    WxPayBillResult downloadBill(WxPayDownloadBillRequest request) throws WxPayException;

    /**
     * <pre>
     * 下载资金账单.
     * 商户可以通过该接口下载自2017年6月1日起 的历史资金流水账单。
     * 注意：
     * 1、资金账单中的数据反映的是商户微信账户资金变动情况；
     * 2、当日账单在次日上午9点开始生成，建议商户在上午10点以后获取；
     * 3、资金账单中涉及金额的字段单位为“元”。
     * 接口链接：https://api.mch.weixin.qq.com/pay/downloadfundflow
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_18">下载对账单</a>
     * </pre>
     *
     * @param billDate    资金账单日期 bill_date 下载对账单的日期，格式：20140603
     * @param accountType 资金账户类型 account_type Basic，基本账户，Operation，运营账户，Fees，手续费账户
     * @param tarType     压缩账单 tar_type 非必传参数，固定值：GZIP，返回格式为.gzip的压缩包账单。不传则默认为数据流形式。
     * @return WxPayFundFlowResult对象 wx pay fund flow result
     * @throws WxPayException the wx pay exception
     */
    WxPayFundFlowResult downloadFundFlow(String billDate, String accountType, String tarType) throws WxPayException;

    /**
     * <pre>
     * 下载资金账单.
     * 商户可以通过该接口下载自2017年6月1日起 的历史资金流水账单。
     * 注意：
     * 1、资金账单中的数据反映的是商户微信账户资金变动情况；
     * 2、当日账单在次日上午9点开始生成，建议商户在上午10点以后获取；
     * 3、资金账单中涉及金额的字段单位为“元”。
     * 接口链接：https://api.mch.weixin.qq.com/pay/downloadfundflow
     * 详情请见: <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_18">下载对账单</a>
     * </pre>
     *
     * @param request 下载资金流水请求
     * @return WxPayFundFlowResult对象 wx pay fund flow result
     * @throws WxPayException the wx pay exception
     */
    WxPayFundFlowResult downloadFundFlow(WxPayDownloadFundFlowRequest request) throws WxPayException;

    /**
     * <pre>
     * 关闭订单.
     * 应用场景
     * 以下情况需要调用关单接口：
     * 1. 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
     * 2. 系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
     * 注意：订单生成后不能马上调用关单接口，最短调用时间间隔为5分钟。
     * 接口地址：https://api.mch.weixin.qq.com/pay/closeorder
     * 是否需要证书：   不需要。
     * </pre>
     *
     * @param outTradeNo 商户系统内部的订单号
     * @return the wx pay order close result
     * @throws WxPayException the wx pay exception
     */
    WxPayOrderCloseResult closeOrder(String outTradeNo) throws WxPayException;

    /**
     * <pre>
     * 关闭订单（适合于需要自定义子商户号和子商户appid的情形）.
     * 应用场景
     * 以下情况需要调用关单接口：
     * 1. 商户订单支付失败需要生成新单号重新发起支付，要对原订单号调用关单，避免重复支付；
     * 2. 系统下单后，用户支付超时，系统退出不再受理，避免用户继续，请调用关单接口。
     * 注意：订单生成后不能马上调用关单接口，最短调用时间间隔为5分钟。
     * 接口地址：https://api.mch.weixin.qq.com/pay/closeorder
     * 是否需要证书：   不需要。
     * </pre>
     *
     * @param request 关闭订单请求对象
     * @return the wx pay order close result
     * @throws WxPayException the wx pay exception
     */
    WxPayOrderCloseResult closeOrder(WxPayOrderCloseRequest request) throws WxPayException;

    /**
     * 交易保障
     * 请求体类型
     * 应用场景：商户在调用微信支付提供的相关接口时，
     * 会得到微信支付返回的相关信息以及获得整个接口的响应时间。
     * 为提高整体的服务水平，协助商户一起提高服务质量，
     * 微信支付提供了相关接口调用耗时和返回信息的主动上报接口，
     * 微信支付可以根据商户侧上报的数据进一步优化网络部署，
     * 完善服务监控，和商户更好的协作为用户提供更好的业务体验
     * 是否需要证书：不需要
     * @param wxPayReportRequest 交易保障请求对象
     * @return 返回信息
     * @throws WxPayException 微信支付异常
     */
    WxPayCommonResult report(WxPayReportRequest wxPayReportRequest) throws WxPayException;
}
