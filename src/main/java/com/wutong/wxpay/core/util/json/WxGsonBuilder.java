package com.wutong.wxpay.core.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wutong.wxpay.core.bean.result.WxError;
import com.wutong.wxpay.core.bean.token.WxAccessToken;

public class WxGsonBuilder {

  public static final GsonBuilder INSTANCE = new GsonBuilder();

  static {
    INSTANCE.disableHtmlEscaping();
    INSTANCE.registerTypeAdapter(WxAccessToken.class, new WxAccessTokenAdapter());
    INSTANCE.registerTypeAdapter(WxError.class, new WxErrorAdapter());
  }

  public static Gson create() {
    return INSTANCE.create();
  }

}
