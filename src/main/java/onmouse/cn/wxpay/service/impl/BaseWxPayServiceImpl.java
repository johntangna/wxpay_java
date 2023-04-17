package onmouse.cn.wxpay.service.impl;

import onmouse.cn.wxpay.bean.notify.WxPayOrderNotifyResult;
import onmouse.cn.wxpay.bean.notify.WxPayRefundNotifyResult;
import onmouse.cn.wxpay.bean.notify.WxScanPayNotifyResult;
import onmouse.cn.wxpay.bean.request.*;
import onmouse.cn.wxpay.bean.result.*;
import onmouse.cn.wxpay.constant.WxPayConstants;
import onmouse.cn.wxpay.constant.WxPayConstants.TradeType;
import onmouse.cn.wxpay.bean.order.WxPayNativeOrderResult;
import onmouse.cn.wxpay.config.WxPayConfig;
import onmouse.cn.wxpay.config.WxPayConfigHolder;
import onmouse.cn.wxpay.exception.WxPayException;
import onmouse.cn.wxpay.service.WxPayService;
import onmouse.cn.wxpay.utils.XmlConfig;
import onmouse.cn.wxpay.utils.ZipUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

public abstract class BaseWxPayServiceImpl implements WxPayService {

    private static final String TOTAL_FUND_COUNT = "资金流水总笔数";

    final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Map<String, WxPayConfig> configMap;

    @Override
    public String getWxPayBaseUrl() {
        return this.getConfig().isUseSandboxEnv()
                ? this.getConfig().getPayBaseUrl() + "/sandboxnew"
                : this.getConfig().getPayBaseUrl();
    }

    @Override
    public WxPayConfig getConfig() {
        if (this.configMap.size() == 1) {
            return this.configMap.values().iterator().next();
        }
        return this.configMap.get(WxPayConfigHolder.get());
    }

    @Override
    public void setConfig(WxPayConfig wxPayConfig) {
        final String defaultMchId = wxPayConfig.getMchId();
        this.setMultiConfig(ImmutableMap.of(defaultMchId, wxPayConfig), defaultMchId);
    }

    @Override
    public void setMultiConfig(Map<String, WxPayConfig> wxPayConfigMap, String defaultMchId) {
        this.configMap = Maps.newHashMap(wxPayConfigMap);
        WxPayConfigHolder.set(defaultMchId);
    }

    @Override
    public void setMultiConfig(Map<String, WxPayConfig> wxPayConfigMap) {
        this.setMultiConfig(wxPayConfigMap, wxPayConfigMap.keySet().iterator().next());
    }

    @Override
    public boolean switchover(String mchId) {
        if (this.configMap.containsKey(mchId)) {
            WxPayConfigHolder.set(mchId);
            return true;
        }
        log.error("无法找到对应【{}】的商户号配置信息，请核实！", mchId);
        return false;
    }

    @Override
    public WxPayOrderQueryResult queryOrder(String transactionId, String outTradeNo) throws WxPayException {
        WxPayOrderQueryRequest wxPayOrderQueryRequest = new WxPayOrderQueryRequest();
        wxPayOrderQueryRequest.setTransactionId(StringUtils.trimToNull(transactionId));
        wxPayOrderQueryRequest.setOutTradeNo(StringUtils.trimToNull(outTradeNo));

        return this.queryOrder(wxPayOrderQueryRequest);
    }

    @Override
    public WxPayOrderQueryResult queryOrder(WxPayOrderQueryRequest wxPayOrderQueryRequest) throws WxPayException {
        wxPayOrderQueryRequest.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/orderquery";
        String responseContent = this.post(url, wxPayOrderQueryRequest.toXML(), false);
        if (StringUtils.isBlank(responseContent)) {
            throw new WxPayException("无响应结果");
        }
        WxPayOrderQueryResult wxPayOrderQueryResult = BaseWxPayResult.fromXml(responseContent, WxPayOrderQueryResult.class);
        wxPayOrderQueryResult.composeCoupons();
        wxPayOrderQueryResult.checkResult(this, wxPayOrderQueryRequest.getSignType(), true);
        return wxPayOrderQueryResult;
    }

    @Override
    public WxPayNativeOrderResult createOrder(WxPayUnifiedOrderRequest request) throws WxPayException {
        WxPayUnifiedOrderResult unifiedOrderResult = this.unifiedOrder(request);
        /**
         * 该值有效期为2小时
         */
        String prepayId = unifiedOrderResult.getPrepayId();
        if (StringUtils.isBlank(prepayId)) {
            throw new WxPayException(String.format("无法获取prepay id，错误代码： '%s'，信息：%s。",
                    unifiedOrderResult.getErrCode(), unifiedOrderResult.getErrCodeDes()));
        }

//        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
//        String nonceStr = unifiedOrderResult.getNonceStr();
        switch (request.getTradeType()) {
            case TradeType.MWEB:

            case TradeType.APP:

            case TradeType.JSAPI: {
                return null;
            }

            case TradeType.NATIVE: {
                return new WxPayNativeOrderResult(unifiedOrderResult.getCodeURL());
            }

            default: {
                throw new WxPayException("该交易类型暂不支持");
            }
        }

    }

    @Override
    public WxPayUnifiedOrderResult unifiedOrder(WxPayUnifiedOrderRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/unifiedorder";
        String responseContent = this.post(url, request.toXML(), false);
        WxPayUnifiedOrderResult result = BaseWxPayResult.fromXml(responseContent, WxPayUnifiedOrderResult.class);
        result.checkResult(this, request.getSignType(), true);
        return result;
    }

    @Override
    public WxScanPayNotifyResult parseScanPayNotifyResult(String xmlData, @Deprecated String signType) throws WxPayException {
        try {
            log.info("扫码支付回调通知请求参数：{}", xmlData);
            WxScanPayNotifyResult result = BaseWxPayResult.fromXml(xmlData, WxScanPayNotifyResult.class);
            this.switchover(result.getMchId());
            log.info("扫码支付回调通知解析后的对象：{}", result);
            result.checkResult(this, this.getConfig().getSignType(), false);
            return result;
        } catch (WxPayException e) {
            throw e;
        } catch (Exception e) {
            throw new WxPayException("发生异常，" + e.getMessage(), e);
        }
    }

    @Override
    public WxScanPayNotifyResult parseScanPayNotifyResult(String xmlData) throws WxPayException {
//    final String signType = this.getConfig().getSignType();
        return this.parseScanPayNotifyResult(xmlData, null);
    }

    @Override
    public WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData) throws WxPayException {
        return this.parseOrderNotifyResult(xmlData, null);
    }

    @Override
    public WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData, String signType) throws WxPayException {
        try {
            log.info("微信支付异步通知参数: {}", xmlData);
            WxPayOrderNotifyResult wxPayOrderNotifyResult = WxPayOrderNotifyResult.fromXml(xmlData);
            if (signType == null) {
                if (wxPayOrderNotifyResult.getSignType() != null) {
                    signType = wxPayOrderNotifyResult.getSignType();
                } else if (configMap.get(wxPayOrderNotifyResult.getMchId()).getSignType() != null) {
                    signType = configMap.get(wxPayOrderNotifyResult.getMchId()).getSignType();
                    this.switchover(wxPayOrderNotifyResult.getMchId());
                }
            }

            log.info("微信支付异步通知请求解析后的对象: {}", wxPayOrderNotifyResult);
            wxPayOrderNotifyResult.checkResult(this, signType, false);
            return wxPayOrderNotifyResult;
        } catch (WxPayException wxPayException) {
            throw wxPayException;
        } catch (Exception exception) {
            throw new WxPayException("发生异常!", exception);
        }
    }

    @Override
    public WxPayRefundResult refund(WxPayRefundRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/secapi/pay/refund";
        if (this.getConfig().isUseSandboxEnv()) {
            url = this.getConfig().getPayBaseUrl() + "/sandboxnew/pay/refund";
        }

        String responseContent = this.post(url, request.toXML(), true);
        WxPayRefundResult result = BaseWxPayResult.fromXml(responseContent, WxPayRefundResult.class);
        result.composeRefundCoupons();
        result.checkResult(this, request.getSignType(), true);
        return result;
    }

    @Override
    public WxPayRefundNotifyResult parseNotifyRefundResult(String xmlData) throws WxPayException {
        try {
            log.info("微信支付退款异步通知参数：{}", xmlData);
            WxPayRefundNotifyResult result;
            if (XmlConfig.fastMode) {
                result = BaseWxPayResult.fromXml(xmlData, WxPayRefundNotifyResult.class);
                this.switchover(result.getMchId());
                result.decryptReqInfo(this.getConfig().getMchKey());
            } else {
                result = WxPayRefundNotifyResult.fromXML(xmlData, this.getConfig().getMchKey());
            }
            log.info("微信支付退款异步通知解析后的对象：{}", result);
            return result;
        } catch (Exception e) {
            throw new WxPayException("发生异常，" + e.getMessage(), e);
        }
    }

    @Override
    public WxPayRefundQueryResult refundQuery(String transactionId, String outTradeNo, String outRefundNo, String refundId) throws WxPayException {
        WxPayRefundQueryRequest wxPayRefundQueryRequest = new WxPayRefundQueryRequest();
        wxPayRefundQueryRequest.setTransactionId(transactionId);
        wxPayRefundQueryRequest.setOutTradeNo(outTradeNo);
        wxPayRefundQueryRequest.setOutRefundNo(outRefundNo);
        wxPayRefundQueryRequest.setRefundId(refundId);
        return this.refundQuery(wxPayRefundQueryRequest);
    }

    @Override
    public WxPayRefundQueryResult refundQuery(WxPayRefundQueryRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/refundquery";
        String responseContent = this.post(url, request.toXML(), false);
        WxPayRefundQueryResult wxPayRefundQueryResult = BaseWxPayResult.fromXml(responseContent, WxPayRefundQueryResult.class);
        wxPayRefundQueryResult.composeRefundRecords();
        wxPayRefundQueryResult.checkResult(this, request.getSignType(), true);
        return wxPayRefundQueryResult;
    }



    @Override
    public WxPayBillResult downloadBill(String billDate, String billType, String tarType)
            throws WxPayException {
        return this.downloadBill(this.buildDownloadBillRequest(billDate, billType, tarType));
    }

    @Override
    public WxPayBillResult downloadBill(WxPayDownloadBillRequest request) throws WxPayException {
        String responseContent = this.downloadRawBill(request);

        if (StringUtils.isEmpty(responseContent)) {
            return null;
        }

        return this.handleBill(request.getBillType(), responseContent);
    }

    @Override
    public String downloadRawBill(String billDate, String billType, String tarType)
            throws WxPayException {
        return this.downloadRawBill(this.buildDownloadBillRequest(billDate, billType, tarType));
    }

    @Override
    public String downloadRawBill(WxPayDownloadBillRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/downloadbill";

        String responseContent;
        if (WxPayConstants.TarType.GZIP.equals(request.getTarType())) {
            responseContent = this.handleGzipBill(url, request.toXML());
        } else {
            responseContent = this.post(url, request.toXML(), false);
            if (responseContent.startsWith("<")) {
                throw WxPayException.from(BaseWxPayResult.fromXml(responseContent, WxPayCommonResult.class));
            }
        }
        return responseContent;
    }

    @Override
    public WxPayFundFlowResult downloadFundFlow(String billDate, String accountType, String tarType) throws WxPayException {

        WxPayDownloadFundFlowRequest request = new WxPayDownloadFundFlowRequest();
        request.setBillDate(billDate);
        request.setAccountType(accountType);
        request.setTarType(tarType);

        return this.downloadFundFlow(request);
    }

    @Override
    public WxPayFundFlowResult downloadFundFlow(WxPayDownloadFundFlowRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/downloadfundflow";

        String responseContent;
        if (WxPayConstants.TarType.GZIP.equals(request.getTarType())) {
            responseContent = this.handleGzipFundFlow(url, request.toXML());
        } else {
            responseContent = this.post(url, request.toXML(), true);
            if (responseContent.startsWith("<")) {
                throw WxPayException.from(BaseWxPayResult.fromXml(responseContent, WxPayCommonResult.class));
            }
        }

        return this.handleFundFlow(responseContent);
    }

    @Override
    public WxPayOrderCloseResult closeOrder(String outTradeNo) throws WxPayException {
        if (StringUtils.isBlank(outTradeNo)) {
            throw new WxPayException("out_trade_no不能为空");
        }

        WxPayOrderCloseRequest wxPayOrderCloseRequest = new WxPayOrderCloseRequest();
        wxPayOrderCloseRequest.setOutTradeNo(StringUtils.trimToNull(outTradeNo));

        return this.closeOrder(wxPayOrderCloseRequest);
    }

    @Override
    public WxPayOrderCloseResult closeOrder(WxPayOrderCloseRequest request) throws WxPayException {
        request.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/pay/closeorder";
        String responseContent = this.post(url, request.toXML(), false);
        WxPayOrderCloseResult wxPayOrderCloseResult = BaseWxPayResult.fromXml(responseContent, WxPayOrderCloseResult.class);

        wxPayOrderCloseResult.checkResult(this, request.getSignType(), true);
        return wxPayOrderCloseResult;
    }

    @Override
    public WxPayCommonResult report(WxPayReportRequest wxPayReportRequest) throws WxPayException {
        wxPayReportRequest.checkAndSign(this.getConfig());

        String url = this.getWxPayBaseUrl() + "/payitil/report";
        String responseResult = this.post(url, wxPayReportRequest.toXML(), false);
        WxPayCommonResult wxPayCommonResult = BaseWxPayResult.fromXml(responseResult, WxPayCommonResult.class);
        wxPayCommonResult.checkResult(this, wxPayReportRequest.getSignType(), true);
        return wxPayCommonResult;
    }

    private String handleGzipFundFlow(String url, String requestStr) throws WxPayException {
        try {
            byte[] responseBytes = this.postForBytes(url, requestStr, true);
            Path tempDirectory = Files.createTempDirectory("fundFlow");
            Path path = Paths.get(tempDirectory.toString(), System.currentTimeMillis() + ".gzip");
            Files.write(path, responseBytes);

            try {
                List<String> allLines = Files.readAllLines(ZipUtils.unGzip(path.toFile()).toPath(), StandardCharsets.UTF_8);
                return Joiner.on("\n").join(allLines);
            } catch (ZipException e) {
                if (e.getMessage().contains("Not in GZIP format")) {
                    throw WxPayException.from(BaseWxPayResult.fromXml(new String(responseBytes, StandardCharsets.UTF_8),
                            WxPayCommonResult.class));
                } else {
                    throw new WxPayException("解压zip文件出错", e);
                }
            }
        } catch (WxPayException wxPayException) {
            throw wxPayException;
        } catch (Exception e) {
            throw new WxPayException("解压zip文件出错", e);
        }
    }

    private WxPayFundFlowResult handleFundFlow(String responseContent) {
        WxPayFundFlowResult wxPayFundFlowResult = new WxPayFundFlowResult();

        String listStr = "";
        String objStr = "";

        if (StringUtils.isNotBlank(responseContent) && responseContent.contains(TOTAL_FUND_COUNT)) {
            listStr = responseContent.substring(0, responseContent.indexOf(TOTAL_FUND_COUNT));
            objStr = responseContent.substring(responseContent.indexOf(TOTAL_FUND_COUNT));
        }
        /*
         * 记账时间:2018-02-01 04:21:23 微信支付业务单号:50000305742018020103387128253 资金流水单号:1900009231201802015884652186 业务名称:退款
         * 业务类型:退款 收支类型:支出 收支金额（元）:0.02 账户结余（元）:0.17 资金变更提交申请人:system 备注:缺货 业务凭证号:REF4200000068201801293084726067
         * 参考以上格式进行取值
         */
        List<WxPayFundFlowBaseResult> wxPayFundFlowBaseResultList = new LinkedList<>();
        // 去空格
        String newStr = listStr.replaceAll(",", " ");
        // 数据分组
        String[] tempStr = newStr.split("`");
        // 分组标题
        String[] t = tempStr[0].split(" ");
        // 计算循环次数
        int j = tempStr.length / t.length;
        // 纪录数组下标
        int k = 1;
        for (int i = 0; i < j; i++) {
            WxPayFundFlowBaseResult wxPayFundFlowBaseResult = new WxPayFundFlowBaseResult();

            wxPayFundFlowBaseResult.setBillingTime(tempStr[k].trim());
            wxPayFundFlowBaseResult.setBizTransactionId(tempStr[k + 1].trim());
            wxPayFundFlowBaseResult.setFundFlowId(tempStr[k + 2].trim());
            wxPayFundFlowBaseResult.setBizName(tempStr[k + 3].trim());
            wxPayFundFlowBaseResult.setBizType(tempStr[k + 4].trim());
            wxPayFundFlowBaseResult.setFinancialType(tempStr[k + 5].trim());
            wxPayFundFlowBaseResult.setFinancialFee(tempStr[k + 6].trim());
            wxPayFundFlowBaseResult.setAccountBalance(tempStr[k + 7].trim());
            wxPayFundFlowBaseResult.setFundApplicant(tempStr[k + 8].trim());
            wxPayFundFlowBaseResult.setMemo(tempStr[k + 9].trim());
            wxPayFundFlowBaseResult.setBizVoucherId(tempStr[k + 10].trim());

            wxPayFundFlowBaseResultList.add(wxPayFundFlowBaseResult);
            k += t.length;
        }
        wxPayFundFlowResult.setWxPayFundFlowBaseResultList(wxPayFundFlowBaseResultList);

        /*
         * 资金流水总笔数,收入笔数,收入金额,支出笔数,支出金额 `20.0,`17.0,`0.35,`3.0,`0.18
         * 参考以上格式进行取值
         */
        String totalStr = objStr.replaceAll(",", " ");
        String[] totalTempStr = totalStr.split("`");
        wxPayFundFlowResult.setTotalRecord(totalTempStr[1]);
        wxPayFundFlowResult.setIncomeRecord(totalTempStr[2]);
        wxPayFundFlowResult.setIncomeAmount(totalTempStr[3]);
        wxPayFundFlowResult.setExpenditureRecord(totalTempStr[4]);
        wxPayFundFlowResult.setExpenditureAmount(totalTempStr[5]);

        return wxPayFundFlowResult;

    }


    private String handleGzipBill(String url, String requestStr) throws WxPayException {
        try {
            byte[] responseBytes = this.postForBytes(url, requestStr, false);
            Path tempDirectory = Files.createTempDirectory("bill");
            Path path = Paths.get(tempDirectory.toString(), System.currentTimeMillis() + ".gzip");
            Files.write(path, responseBytes);
            try {
                List<String> allLines = Files.readAllLines(ZipUtils.unGzip(path.toFile()).toPath(), StandardCharsets.UTF_8);
                return Joiner.on("\n").join(allLines);
            } catch (ZipException e) {
                if (e.getMessage().contains("Not in GZIP format")) {
                    throw WxPayException.from(BaseWxPayResult.fromXml(new String(responseBytes, StandardCharsets.UTF_8),
                            WxPayCommonResult.class));
                } else {
                    throw new WxPayException("解压zip文件出错！", e);
                }
            }
        } catch (Exception e) {
            throw new WxPayException("解析对账单文件时出错！", e);
        }
    }

    private WxPayDownloadBillRequest buildDownloadBillRequest(String billDate, String billType, String tarType) {
        WxPayDownloadBillRequest request = new WxPayDownloadBillRequest();
        request.setBillType(billType);
        request.setBillDate(billDate);
        request.setTarType(tarType);
        return request;
    }

    private WxPayBillResult handleBill(String billType, String responseContent) {
        return WxPayBillResult.fromRawBillResultString(responseContent, billType);
    }

}
