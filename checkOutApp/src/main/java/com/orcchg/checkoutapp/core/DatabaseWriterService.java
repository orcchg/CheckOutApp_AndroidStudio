package com.orcchg.checkoutapp.core;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class DatabaseWriterService extends IntentService {
  private static final String TAG = "CheckOut_DatabaseWriterService";
  private static final String SERVICE_NAME = TAG;

  public DatabaseWriterService() {
    super(SERVICE_NAME);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service has started");
    long total_items = ModifiedContentBuffer.getInstance().synchronizeBufferWithDatabase();
    Database.getInstance().updateLastStoredCheckOutID();
    Database.getInstance().close();
    Log.i(TAG, "Service has finished: total[" + total_items + "]");
  }
}
