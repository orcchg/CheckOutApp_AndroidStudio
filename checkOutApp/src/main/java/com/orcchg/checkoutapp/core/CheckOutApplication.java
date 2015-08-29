package com.orcchg.checkoutapp.core;

import android.app.Application;

public class CheckOutApplication extends Application {
  private static final String TAG = "CheckOut_Application";
  
  public CheckOutApplication() {
    super();
    ModifiedContentBuffer.init();
  }
}
