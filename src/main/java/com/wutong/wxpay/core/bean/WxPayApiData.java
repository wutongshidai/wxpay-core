package com.wutong.wxpay.core.bean;

/**
 * 微信支付请求数据封装对象
 * @author Y.H
 *
 */
public class WxPayApiData {

	/**
	 * 请求地址
	 */
	private String url;
	
	/**
	 * 请求数据
	 */
	private String requestData;
	
	/**
	 * 响应地址
	 */
	private String responseData;
	
	/**
	 * 错误信息
	 */
	private String exceptionMsg;
	
	public WxPayApiData(String url, String requestData, String responseData, String exceptionMsg) {
		this.url = url;
		this.requestData = requestData;
		this.responseData = responseData;
		this.exceptionMsg = exceptionMsg;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}
	
}
