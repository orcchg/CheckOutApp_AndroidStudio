package com.orcchg.checkoutapp;

import java.lang.ref.WeakReference;

import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.items.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EntryChangedReceiver extends BroadcastReceiver {
  private static final String TAG = "CheckOut_EntryChangedReceiver";
  private WeakReference<CheckOutPage> ref;
  
  public EntryChangedReceiver(final CheckOutPage activity) {
    ref = new WeakReference<CheckOutPage>(activity);
  }
  
  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent != null) {
      CheckOutPage activity = ref.get();
      if (activity != null) {
        Log.d(TAG, "Received broadcast");
        int entryIndex = intent.getIntExtra(EntryPage.out_bundleKey_entryIndex, -1);
        Entry ref = Cache.getInstance().getEntries().get(entryIndex);
        activity.setSummaryArchived(entryIndex, ref.isArchived());
        activity.refreshEntries();
        activity.refreshWidget();
      } else {
        Log.e(TAG, "Stalled reference on CheckOut page");
      }
    } else {
      Log.w(TAG, "Broadcast receiver has got null data!");
    }
  }
}
