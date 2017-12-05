package com.wutong.wxpay.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ssl.SSLContexts;

import com.wutong.wxpay.core.exception.WxPayException;

/**
 * 微信支付配置
 * 
 * @author Y.H
 *
 */
public class WxPayConfig {

	/**
	 * 连接超时时间
	 */
	private int httpConnectionTimeout = 5000;

	/**
	 * 获取响应超时时间
	 */
	private int httpTimeout = 10000;

	/**
	 * appid
	 */
	private String appId;

	/**
	 * 商户id
	 */
	private String mchId;
	/**
	 * 商户密钥
	 */
	private String mchKey;
	/**
	 * 通知回调地址
	 */
	private String notifyUrl;

	/**
	 * 交易类型
	 */
	private String tradeType;

	private SSLContext sslContext;
	/**
	 * 证书地址
	 */
	private String keyPath;

	public int getHttpConnectionTimeout() {
		return httpConnectionTimeout;
	}

	public void setHttpConnectionTimeout(int httpConnectionTimeout) {
		this.httpConnectionTimeout = httpConnectionTimeout;
	}

	public int getHttpTimeout() {
		return httpTimeout;
	}

	public void setHttpTimeout(int httpTimeout) {
		this.httpTimeout = httpTimeout;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMchId() {
		return mchId;
	}

	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	public String getMchKey() {
		return mchKey;
	}

	public void setMchKey(String mchKey) {
		this.mchKey = mchKey;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}
	public SSLContext initSSLContext() throws WxPayException {
	    if (StringUtils.isBlank(this.getMchId())) {
	      throw new WxPayException("请确保商户号mchId已设置");
	    }

	    if (StringUtils.isBlank(this.getKeyPath())) {
	      throw new WxPayException("请确保证书文件地址keyPath已配置");
	    }

	    InputStream inputStream;
	    final String prefix = "classpath:";
	    String fileHasProblemMsg = "证书文件【" + this.getKeyPath() + "】有问题，请核实！";
	    String fileNotFoundMsg = "证书文件【" + this.getKeyPath() + "】不存在，请核实！";
	    if (this.getKeyPath().startsWith(prefix)) {
	      String path = StringUtils.removeFirst(this.getKeyPath(), prefix);
	      if (!path.startsWith("/")) {
	        path = "/" + path;
	      }
	      inputStream = WxPayConfig.class.getResourceAsStream(path);
	      if (inputStream == null) {
	        throw new WxPayException(fileNotFoundMsg);
	      }
	    } else {
	      try {
	        File file = new File(this.getKeyPath());
	        if (!file.exists()) {
	          throw new WxPayException(fileNotFoundMsg);
	        }

	        inputStream = new FileInputStream(file);
	      } catch (IOException e) {
	        throw new WxPayException(fileHasProblemMsg, e);
	      }
	    }

	    try {
	      KeyStore keystore = KeyStore.getInstance("PKCS12");
	      char[] partnerId2charArray = this.getMchId().toCharArray();
	      keystore.load(inputStream, partnerId2charArray);
	      this.sslContext = SSLContexts.custom().loadKeyMaterial(keystore, partnerId2charArray).build();
	      return this.sslContext;
	    } catch (Exception e) {
	      throw new WxPayException(fileHasProblemMsg, e);
	    } finally {
	      IOUtils.closeQuietly(inputStream);
	    }
	  }
}
