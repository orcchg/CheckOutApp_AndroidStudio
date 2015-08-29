package com.orcchg.checkoutapp.core;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class DatabaseWriterThread {
  private static final String TAG = "CheckOut_DatabaseWriterThread";
  
  private Timer mTimer = new Timer();
  
  public DatabaseWriterThread() {
    mTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (Cache.getInstance().push()) {
          writeTask();
        }
      }
    }, 2_000l, 10_000l);
  }
  
  public void stop() {
    mTimer.cancel();
    Log.i(TAG, "Writer thread has stopped");
  }
  
  private void writeTask() {
    Log.i(TAG, "Writer thread has started its job");
    long total_items = ModifiedContentBuffer.getInstance().synchronizeBufferWithDatabase();
    Database.getInstance().updateLastStoredCheckOutID();
    Log.i(TAG, "Writer thread has finished its job: total[" + total_items + "]");
  }
}
