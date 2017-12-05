package com.wutong.wxpay.core.util.qrcode;

import java.awt.Color;

public class MatrixToLogoImageConfig
{
  public static final Color DEFAULT_BORDERCOLOR = Color.RED;
  public static final int DEFAULT_BORDER = 2;
  public static final int DEFAULT_LOGOPART = 5;
  private final int border = 2;
  private final Color borderColor;
  private final int logoPart;
  
  public MatrixToLogoImageConfig()
  {
    this(DEFAULT_BORDERCOLOR, 5);
  }
  
  public MatrixToLogoImageConfig(Color borderColor, int logoPart)
  {
    this.borderColor = borderColor;
    this.logoPart = logoPart;
  }
  
  public Color getBorderColor()
  {
    return this.borderColor;
  }
  
  public int getBorder()
  {
    getClass();return 2;
  }
  
  public int getLogoPart()
  {
    return this.logoPart;
  }
}
