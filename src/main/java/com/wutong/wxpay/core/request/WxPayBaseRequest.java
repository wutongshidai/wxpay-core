package com.wutong.wxpay.core.request;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.wutong.wxpay.core.config.WxPayConfig;
import com.wutong.wxpay.core.exception.WxErrorException;
import com.wutong.wxpay.core.exception.WxPayException;
import com.wutong.wxpay.core.util.BeanUtils;
import com.wutong.wxpay.core.util.SignUtils;
import com.wutong.wxpay.core.util.ToStringUtils;
import com.wutong.wxpay.core.util.xml.XStreamInitializer;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * <pre>
 *  微信支付请求对象共用的参数存放类
 * </pre>
 *
 */
public abstract class WxPayBaseRequest {
  /**
   * <pre>
   * 字段名：公众账号ID
   * 变量名：appid
   * 是否必填：是
   * 类型：String(32)
   * 示例值：wxd678efh567hg6787
   * 描述：微信分配的公众账号ID（企业号corpid即为此appId）
   * </pre>
   */
  @XStreamAlias("appid")
  protected String appid;
  /**
   * <pre>
   * 字段名：商户号
   * 变量名：mch_id
   * 是否必填：是
   * 类型：String(32)
   * 示例值：1230000109
   * 描述：微信支付分配的商户号
   * </pre>
   */
  @XStreamAlias("mch_id")
  protected String mchId;
  /**
   * <pre>
   * 字段名：服务商模式下的子商户公众账号ID
   * 变量名：sub_appid
   * 是否必填：是
   * 类型：String(32)
   * 示例值：wxd678efh567hg6787
   * 描述：微信分配的子商户公众账号ID
   * </pre>
   */
  @XStreamAlias("sub_appid")
  protected String subAppId;
  /**
   * <pre>
   * 字段名：服务商模式下的子商户号
   * 变量名：sub_mch_id
   * 是否必填：是
   * 类型：String(32)
   * 示例值：1230000109
   * 描述：微信支付分配的子商户号，开发者模式下必填
   * </pre>
   */
  @XStreamAlias("sub_mch_id")
  protected String subMchId;
  /**
   * <pre>
   * 字段名：随机字符串
   * 变量名：nonce_str
   * 是否必填：是
   * 类型：String(32)
   * 示例值：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
   * 描述：随机字符串，不长于32位。推荐随机数生成算法
   * </pre>
   */
  @XStreamAlias("nonce_str")
  protected String nonceStr;
  /**
   * <pre>
   * 字段名：签名
   * 变量名：sign
   * 是否必填：是
   * 类型：String(32)
   * 示例值：C380BEC2BFD727A4B6845133519F3AD6
   * 描述：签名，详见签名生成算法
   * </pre>
   */
  @XStreamAlias("sign")
  protected String sign;

  /**
   * <pre>
   * 签名类型
   * sign_type
   * 否
   * String(32)
   * HMAC-SHA256
   * 签名类型，目前支持HMAC-SHA256和MD5
   * </pre>
   */
  @XStreamAlias("sign_type")
  private String signType;

  /**
   * 将单位为元转换为单位为分
   *
   * @param yuan 将要转换的元的数值字符串
   */
  public static Integer yuanToFee(String yuan) {
    return new BigDecimal(yuan).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
  }

  /**
   * 检查请求参数内容，包括必填参数以及特殊约束
   */
  protected void checkFields() throws WxPayException {
    //check required fields
    try {
      BeanUtils.checkRequiredFields(this);
    } catch (WxErrorException e) {
      throw new WxPayException(e.getError().getErrorMsg(), e);
    }

    //check other parameters
    this.checkConstraints();
  }

  /**
   * 检查约束情况
   */
  protected abstract void checkConstraints() throws WxPayException;

  public String getAppid() {
    return this.appid;
  }

  /**
   * 如果配置中已经设置，可以不设置值
   *
   * @param appid 微信公众号appid
   */
  public void setAppid(String appid) {
    this.appid = appid;
  }

  public String getMchId() {
    return this.mchId;
  }

  /**
   * 如果配置中已经设置，可以不设置值
   *
   * @param mchId 微信商户号
   */
  public void setMchId(String mchId) {
    this.mchId = mchId;
  }

  public String getNonceStr() {
    return this.nonceStr;
  }

  /**
   * 默认采用时间戳为随机字符串，可以不设置
   *
   * @param nonceStr 随机字符串
   */
  public void setNonceStr(String nonceStr) {
    this.nonceStr = nonceStr;
  }

  public String getSign() {
    return this.sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }

  public String getSubAppId() {
    return subAppId;
  }

  public void setSubAppId(String subAppId) {
    this.subAppId = subAppId;
  }

  public String getSubMchId() {
    return subMchId;
  }

  public void setSubMchId(String subMchId) {
    this.subMchId = subMchId;
  }

  public String getSignType() {
    return signType;
  }

  public void setSignType(String signType) {
    this.signType = signType;
  }

  @Override
  public String toString() {
    return ToStringUtils.toSimpleString(this);
  }

  public String toXML() {
    XStream xstream = XStreamInitializer.getInstance();
    xstream.processAnnotations(this.getClass());
    return xstream.toXML(this);
  }

  /**
   * <pre>
   * 检查参数，并设置签名
   * 1、检查参数（注意：子类实现需要检查参数的而外功能时，请在调用父类的方法前进行相应判断）
   * 2、补充系统参数，如果未传入则从配置里读取
   * 3、生成签名，并设置进去
   * </pre>
   *
   * @param config 支付配置对象，用于读取相应系统配置信息
   */
  public void checkAndSign(WxPayConfig config) throws WxPayException {
    this.checkFields();

    if (StringUtils.isBlank(getAppid())) {
      this.setAppid(config.getAppId());
    }

    if (StringUtils.isBlank(getMchId())) {
      this.setMchId(config.getMchId());
    }

//    if (StringUtils.isBlank(getSubAppId())) {
//      this.setSubAppId(config.getSubAppId());
//    }
//
//    if (StringUtils.isBlank(getSubMchId())) {
//      this.setSubMchId(config.getSubMchId());
//    }

    if (StringUtils.isBlank(getNonceStr())) {
      this.setNonceStr(String.valueOf(System.currentTimeMillis()));
    }
    //设置签名字段的值
    this.setSign(SignUtils.createSign(this, config.getMchKey(), this.signType));
  }

}
