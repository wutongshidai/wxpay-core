package com.wutong.wxpay.core.service.impl;

import com.google.common.collect.Maps;
import com.wutong.wxpay.core.bean.WxPayApiData;
import com.wutong.wxpay.core.bean.notify.WxPayOrderNotifyResult;
import com.wutong.wxpay.core.bean.notify.WxPayRefundNotifyResult;
import com.wutong.wxpay.core.config.WxPayConfig;
import com.wutong.wxpay.core.constants.WxPayConstants.SignType;
import com.wutong.wxpay.core.constants.WxPayConstants.TradeType;
import com.wutong.wxpay.core.exception.WxPayException;
import com.wutong.wxpay.core.order.WxPayNativeOrderResult;
import com.wutong.wxpay.core.request.WxEntPayQueryRequest;
import com.wutong.wxpay.core.request.WxEntPayRequest;
import com.wutong.wxpay.core.request.WxPayDownloadBillRequest;
import com.wutong.wxpay.core.request.WxPayOrderCloseRequest;
import com.wutong.wxpay.core.request.WxPayOrderQueryRequest;
import com.wutong.wxpay.core.request.WxPayOrderReverseRequest;
import com.wutong.wxpay.core.request.WxPayRefundQueryRequest;
import com.wutong.wxpay.core.request.WxPayRefundRequest;
import com.wutong.wxpay.core.request.WxPayReportRequest;
import com.wutong.wxpay.core.request.WxPayShorturlRequest;
import com.wutong.wxpay.core.request.WxPayUnifiedOrderRequest;
import com.wutong.wxpay.core.result.WxEntPayQueryResult;
import com.wutong.wxpay.core.result.WxEntPayResult;
import com.wutong.wxpay.core.result.WxPayBaseResult;
import com.wutong.wxpay.core.result.WxPayBillBaseResult;
import com.wutong.wxpay.core.result.WxPayBillResult;
import com.wutong.wxpay.core.result.WxPayCommonResult;
import com.wutong.wxpay.core.result.WxPayOrderCloseResult;
import com.wutong.wxpay.core.result.WxPayOrderQueryResult;
import com.wutong.wxpay.core.result.WxPayOrderReverseResult;
import com.wutong.wxpay.core.result.WxPayRefundQueryResult;
import com.wutong.wxpay.core.result.WxPayRefundResult;
import com.wutong.wxpay.core.result.WxPayShorturlResult;
import com.wutong.wxpay.core.result.WxPayUnifiedOrderResult;
import com.wutong.wxpay.core.service.WxPayService;
import com.wutong.wxpay.core.util.SignUtils;
import com.wutong.wxpay.core.util.qrcode.QrcodeUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;


/**
 * <pre>
 *  微信支付接口请求抽象实现类
 * </pre>
 *
 */
public abstract class WxPayServiceAbstractImpl implements WxPayService {
  private static final String PAY_BASE_URL = "https://api.mch.weixin.qq.com";
  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  protected static ThreadLocal<WxPayApiData> wxApiData = new ThreadLocal<>();

  protected WxPayConfig config;

  @Override
  public WxPayConfig getConfig() {
    return this.config;
  }

  @Override
  public void setConfig(WxPayConfig config) {
    this.config = config;
  }

  private String getPayBaseUrl() {
    return PAY_BASE_URL;
  }

  /**
   * 发送post请求
   *
   * @param url        请求地址
   * @param requestStr 请求信息
   * @param useKey     是否使用证书
   * @return 返回请求结果字符串
   */
  protected abstract String post(String url, String requestStr, boolean useKey) throws WxPayException;

  @Override
  public WxPayRefundResult refund(WxPayRefundRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/secapi/pay/refund";
    String responseContent = this.post(url, request.toXML(), true);
    WxPayRefundResult result = WxPayBaseResult.fromXML(responseContent, WxPayRefundResult.class);
    result.checkResult(this);
    return result;
  }

  @Override
  public WxPayRefundQueryResult refundQuery(String transactionId, String outTradeNo, String outRefundNo, String refundId)
    throws WxPayException {
    WxPayRefundQueryRequest request = new WxPayRefundQueryRequest();
    request.setOutTradeNo(StringUtils.trimToNull(outTradeNo));
    request.setTransactionId(StringUtils.trimToNull(transactionId));
    request.setOutRefundNo(StringUtils.trimToNull(outRefundNo));
    request.setRefundId(StringUtils.trimToNull(refundId));

    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/pay/refundquery";
    String responseContent = this.post(url, request.toXML(), false);
    WxPayRefundQueryResult result = WxPayBaseResult.fromXML(responseContent, WxPayRefundQueryResult.class);
    result.composeRefundRecords();
    result.checkResult(this);
    return result;
  }


  @Override
  public WxPayOrderNotifyResult parseOrderNotifyResult(String xmlData) throws WxPayException {
    try {
      log.debug("微信支付异步通知请求参数：{}", xmlData);
      WxPayOrderNotifyResult result = WxPayOrderNotifyResult.fromXML(xmlData);
      log.debug("微信支付异步通知请求解析后的对象：{}", result);
      result.checkResult(this);
      return result;
    } catch (WxPayException e) {
      log.error(e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new WxPayException("发生异常，" + e.getMessage(), e);
    }
  }

  @Override
  public WxPayRefundNotifyResult parseRefundNotifyResult(String xmlData) throws WxPayException {
    try {
      log.debug("微信支付退款异步通知参数：{}", xmlData);
      WxPayRefundNotifyResult result = WxPayRefundNotifyResult.fromXML(xmlData, this.getConfig().getMchKey());
      log.debug("微信支付退款异步通知解析后的对象：{}", result);
      return result;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new WxPayException("发生异常，" + e.getMessage(), e);
    }
  }


  @Override
  public WxPayOrderQueryResult queryOrder(String transactionId, String outTradeNo) throws WxPayException {
    WxPayOrderQueryRequest request = new WxPayOrderQueryRequest();
    request.setOutTradeNo(StringUtils.trimToNull(outTradeNo));
    request.setTransactionId(StringUtils.trimToNull(transactionId));
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/pay/orderquery";
    String responseContent = this.post(url, request.toXML(), false);
    if (StringUtils.isBlank(responseContent)) {
      throw new WxPayException("无响应结果");
    }

    WxPayOrderQueryResult result = WxPayBaseResult.fromXML(responseContent, WxPayOrderQueryResult.class);
    result.composeCoupons();
    result.checkResult(this);
    return result;
  }

  @Override
  public WxPayOrderCloseResult closeOrder(String outTradeNo) throws WxPayException {
    if (StringUtils.isBlank(outTradeNo)) {
      throw new WxPayException("out_trade_no不能为空");
    }

    WxPayOrderCloseRequest request = new WxPayOrderCloseRequest();
    request.setOutTradeNo(StringUtils.trimToNull(outTradeNo));
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/pay/closeorder";
    String responseContent = this.post(url, request.toXML(), false);
    WxPayOrderCloseResult result = WxPayBaseResult.fromXML(responseContent, WxPayOrderCloseResult.class);
    result.checkResult(this);

    return result;
  }

  public <T> T createOrder(WxPayUnifiedOrderRequest request) throws WxPayException {
    WxPayUnifiedOrderResult unifiedOrderResult = this.unifiedOrder(request);
    String prepayId = unifiedOrderResult.getPrepayId();
    if (StringUtils.isBlank(prepayId)) {
      throw new RuntimeException(String.format("无法获取prepay id，错误代码： '%s'，信息：%s。",
        unifiedOrderResult.getErrCode(), unifiedOrderResult.getErrCodeDes()));
    }

    Object payResult = WxPayNativeOrderResult.newBuilder().codeUrl(unifiedOrderResult.getCodeURL())
            .build();

    return (T) payResult;
  }

  @Override
  public WxPayUnifiedOrderResult unifiedOrder(WxPayUnifiedOrderRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/pay/unifiedorder";
    String responseContent = this.post(url, request.toXML(), false);
    WxPayUnifiedOrderResult result = WxPayBaseResult.fromXML(responseContent, WxPayUnifiedOrderResult.class);
    result.checkResult(this);
    return result;
  }

  @Override
  public Map<String, String> getPayInfo(WxPayUnifiedOrderRequest request) throws WxPayException {
    WxPayUnifiedOrderResult unifiedOrderResult = this.unifiedOrder(request);
    String prepayId = unifiedOrderResult.getPrepayId();
    if (StringUtils.isBlank(prepayId)) {
      throw new RuntimeException(String.format("无法获取prepay id，错误代码： '%s'，信息：%s。",
        unifiedOrderResult.getErrCode(), unifiedOrderResult.getErrCodeDes()));
    }

    Map<String, String> payInfo = new HashMap<>();
    payInfo.put("codeUrl", unifiedOrderResult.getCodeURL());

    return payInfo;
  }

  @Override
  public WxEntPayResult entPay(WxEntPayRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());
    String url = this.getPayBaseUrl() + "/mmpaymkttransfers/promotion/transfers";

    String responseContent = this.post(url, request.toXML(), true);
    WxEntPayResult result = WxPayBaseResult.fromXML(responseContent, WxEntPayResult.class);
    result.checkResult(this);
    return result;
  }

  @Override
  public WxEntPayQueryResult queryEntPay(String partnerTradeNo) throws WxPayException {
    WxEntPayQueryRequest request = new WxEntPayQueryRequest();
    request.setPartnerTradeNo(partnerTradeNo);
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/mmpaymkttransfers/gettransferinfo";
    String responseContent = this.post(url, request.toXML(), true);
    WxEntPayQueryResult result = WxPayBaseResult.fromXML(responseContent, WxEntPayQueryResult.class);
    result.checkResult(this);
    return result;
  }

  @Override
  public byte[] createScanPayQrcodeMode1(String productId, File logoFile, Integer sideLength) {
    String content = this.createScanPayQrcodeMode1(productId);
    return this.createQrcode(content, logoFile, sideLength);
  }

  @Override
  public String createScanPayQrcodeMode1(String productId) {
    //weixin://wxpay/bizpayurl?sign=XXXXX&appid=XXXXX&mch_id=XXXXX&product_id=XXXXXX&time_stamp=XXXXXX&nonce_str=XXXXX
    StringBuilder codeUrl = new StringBuilder("weixin://wxpay/bizpayurl?");
    Map<String, String> params = Maps.newHashMap();
    params.put("appid", this.getConfig().getAppId());
    params.put("mch_id", this.getConfig().getMchId());
    params.put("product_id", productId);
    params.put("time_stamp", String.valueOf(System.currentTimeMillis() / 1000));//这里需要秒，10位数字
    params.put("nonce_str", String.valueOf(System.currentTimeMillis()));

    String sign = SignUtils.createSign(params, this.getConfig().getMchKey(), null);
    params.put("sign", sign);

    for (String key : params.keySet()) {
      codeUrl.append(key + "=" + params.get(key) + "&");
    }

    String content = codeUrl.toString().substring(0, codeUrl.length() - 1);
    log.debug("扫码支付模式一生成二维码的URL:{}", content);
    return content;
  }

  @Override
  public byte[] createScanPayQrcodeMode2(String codeUrl, File logoFile, Integer sideLength) {
    return this.createQrcode(codeUrl, logoFile, sideLength);
  }

  private byte[] createQrcode(String content, File logoFile, Integer sideLength) {
    if (sideLength == null || sideLength < 1) {
      return QrcodeUtils.createQrcode(content, logoFile);
    }

    return QrcodeUtils.createQrcode(content, sideLength, logoFile);
  }

  public void report(WxPayReportRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/payitil/report";
    String responseContent = this.post(url, request.toXML(), false);
    WxPayCommonResult result = WxPayBaseResult.fromXML(responseContent, WxPayCommonResult.class);
    result.checkResult(this);
  }

  @Override
  public WxPayBillResult downloadBill(String billDate, String billType, String tarType, String deviceInfo) throws WxPayException {
    WxPayDownloadBillRequest request = new WxPayDownloadBillRequest();
    request.setBillType(billType);
    request.setBillDate(billDate);
    request.setTarType(tarType);
    request.setDeviceInfo(deviceInfo);

    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/pay/downloadbill";
    String responseContent = this.post(url, request.toXML(), false);
    if (responseContent.startsWith("<")) {
      throw WxPayException.from(WxPayBaseResult.fromXML(responseContent, WxPayCommonResult.class));
    } else {
      return this.handleBillInformation(responseContent);
    }
  }

  private WxPayBillResult handleBillInformation(String responseContent) {
    WxPayBillResult wxPayBillResult = new WxPayBillResult();

    String listStr = "";
    String objStr = "";
    if (responseContent.contains("总交易单数")) {
      listStr = responseContent.substring(0, responseContent.indexOf("总交易单数"));
      objStr = responseContent.substring(responseContent.indexOf("总交易单数"));
    }

    /*
     * 交易时间:2017-04-06 01:00:02 公众账号ID: 商户号: 子商户号:0 设备号:WEB 微信订单号: 商户订单号:2017040519091071873216 用户标识: 交易类型:NATIVE
     * 交易状态:REFUND 付款银行:CFT 货币种类:CNY 总金额:0.00 企业红包金额:0.00 微信退款单号: 商户退款单号:20170406010000933 退款金额:0.01 企业红包退款金额:0.00
     * 退款类型:ORIGINAL 退款状态:SUCCESS 商品名称: 商户数据包: 手续费:0.00000 费率 :0.60%
     * 参考以上格式进行取值
     */
    List<WxPayBillBaseResult> wxPayBillBaseResultLst = new LinkedList<>();
    String newStr = listStr.replaceAll(",", " "); // 去空格
    String[] tempStr = newStr.split("`"); // 数据分组
    String[] t = tempStr[0].split(" ");// 分组标题
    int j = tempStr.length / t.length; // 计算循环次数
    int k = 1; // 纪录数组下标
    for (int i = 0; i < j; i++) {
      WxPayBillBaseResult wxPayBillBaseResult = new WxPayBillBaseResult();

      wxPayBillBaseResult.setTradeTime(tempStr[k].trim());
      wxPayBillBaseResult.setAppId(tempStr[k + 1].trim());
      wxPayBillBaseResult.setMchId(tempStr[k + 2].trim());
      wxPayBillBaseResult.setSubMchId(tempStr[k + 3].trim());
      wxPayBillBaseResult.setDeviceInfo(tempStr[k + 4].trim());
      wxPayBillBaseResult.setTransationId(tempStr[k + 5].trim());
      wxPayBillBaseResult.setOutTradeNo(tempStr[k + 6].trim());
      wxPayBillBaseResult.setOpenId(tempStr[k + 7].trim());
      wxPayBillBaseResult.setTradeType(tempStr[k + 8].trim());
      wxPayBillBaseResult.setTradeState(tempStr[k + 9].trim());
      wxPayBillBaseResult.setBankType(tempStr[k + 10].trim());
      wxPayBillBaseResult.setFeeType(tempStr[k + 11].trim());
      wxPayBillBaseResult.setTotalFee(tempStr[k + 12].trim());
      wxPayBillBaseResult.setCouponFee(tempStr[k + 13].trim());
      wxPayBillBaseResult.setRefundId(tempStr[k + 14].trim());
      wxPayBillBaseResult.setOutRefundNo(tempStr[k + 15].trim());
      wxPayBillBaseResult.setSettlementRefundFee(tempStr[k + 16].trim());
      wxPayBillBaseResult.setCouponRefundFee(tempStr[k + 17].trim());
      wxPayBillBaseResult.setRefundChannel(tempStr[k + 18].trim());
      wxPayBillBaseResult.setRefundState(tempStr[k + 19].trim());
      wxPayBillBaseResult.setBody(tempStr[k + 20].trim());
      wxPayBillBaseResult.setAttach(tempStr[k + 21].trim());
      wxPayBillBaseResult.setPoundage(tempStr[k + 22].trim());
      wxPayBillBaseResult.setPoundageRate(tempStr[k + 23].trim());
      wxPayBillBaseResultLst.add(wxPayBillBaseResult);
      k += t.length;
    }
    wxPayBillResult.setWxPayBillBaseResultLst(wxPayBillBaseResultLst);

    /*
     * 总交易单数,总交易额,总退款金额,总代金券或立减优惠退款金额,手续费总金额 `2,`0.02,`0.0,`0.0,`0
     * 参考以上格式进行取值
     */
    String totalStr = objStr.replaceAll(",", " ");
    String[] totalTempStr = totalStr.split("`");
    wxPayBillResult.setTotalRecord(totalTempStr[1]);
    wxPayBillResult.setTotalFee(totalTempStr[2]);
    wxPayBillResult.setTotalRefundFee(totalTempStr[3]);
    wxPayBillResult.setTotalCouponFee(totalTempStr[4]);
    wxPayBillResult.setTotalPoundageFee(totalTempStr[5]);

    return wxPayBillResult;
  }


  @Override
  public WxPayOrderReverseResult reverseOrder(WxPayOrderReverseRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/secapi/pay/reverse";
    String responseContent = this.post(url, request.toXML(), true);
    WxPayOrderReverseResult result = WxPayBaseResult.fromXML(responseContent, WxPayOrderReverseResult.class);
    result.checkResult(this);
    return result;
  }

  @Override
  public String shorturl(WxPayShorturlRequest request) throws WxPayException {
    request.checkAndSign(this.getConfig());

    String url = this.getPayBaseUrl() + "/tools/shorturl";
    String responseContent = this.post(url, request.toXML(), false);
    WxPayShorturlResult result = WxPayBaseResult.fromXML(responseContent, WxPayShorturlResult.class);
    result.checkResult(this);
    return result.getShortUrl();
  }

  @Override
  public String shorturl(String longUrl) throws WxPayException {
    return this.shorturl(new WxPayShorturlRequest(longUrl));
  }


  @Override
  public WxPayApiData getWxApiData() {
    try {
      return wxApiData.get();
    } finally {
      //一般来说，接口请求会在一个线程内进行，这种情况下，每个线程get的会是之前所存入的数据，
      // 但以防万一有同一线程多次请求的问题，所以每次获取完数据后移除对应数据
      wxApiData.remove();
    }
  }
}
