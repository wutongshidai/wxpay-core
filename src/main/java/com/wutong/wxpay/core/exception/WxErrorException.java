package com.wutong.wxpay.core.exception;

import com.wutong.wxpay.core.bean.result.WxError;

public class WxErrorException extends Exception {

  private static final long serialVersionUID = -6357149550353160810L;

  private WxError error;

  public WxErrorException(WxError error) {
    super(error.toString());
    this.error = error;
  }

  public WxErrorException(WxError error, Throwable cause) {
    super(error.toString(), cause);
    this.error = error;
  }

  public WxError getError() {
    return this.error;
  }


}
