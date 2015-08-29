package com.orcchg.checkoutapp.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.util.Log;

public class ModifiedContentBuffer {
  private static final String TAG = "CheckOut_ModifiedContentBuffer";
  
  private static ModifiedContentBuffer INSTANCE;
  
  static void init() {
    INSTANCE = new ModifiedContentBuffer();
  }
  
  static ModifiedContentBuffer getInstance() {
    return INSTANCE;
  }
  
  private Queue<DatabaseStoreable> mBuffer;
  
  ModifiedContentBuffer() {
    mBuffer = new LinkedList<DatabaseStoreable>();
  }
  
  /* Public API */
  // --------------------------------------------------------------------------
  boolean put(final DatabaseStoreable item) { return mBuffer.offer(item); }
  boolean put(final List<? extends DatabaseStoreable> items) { return mBuffer.addAll(items); }
  
  long synchronizeBufferWithDatabase() {
    long counter = 0;
    long stored = 0;
    long deleted = 0;
    while (!mBuffer.isEmpty()) {
      DatabaseStoreable item = mBuffer.poll();
      if (item == null) {
        continue;  // skip null items
      }
      switch (item.getModifiedLabel()) {
        default:
        case OLD:
          // do nothing
          break;
        case NEW:
        case MODIFIED:
          item.store();
          ++stored;
          break;
        case DELETED:
          item.delete();
          ++deleted;
          break;
      }
      ++counter;
    }
    Log.i(TAG, "Synchronized buffer and Database: total[" + counter + "], stored[" + stored + "], deleted[" + deleted + "], old[" + (counter - stored - deleted) + "]");
    return counter;
  }
}
