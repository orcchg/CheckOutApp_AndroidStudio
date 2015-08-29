package com.orcchg.checkoutapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.core.ModifiedLabel;
import com.orcchg.checkoutapp.dialogs.ChangeEntryDialog;
import com.orcchg.checkoutapp.dialogs.NamedItemDialog;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.Utility;

public class CheckOutPageCallerFragment extends Fragment {
  private static final String TAG = "CheckOut_CheckOutPageCallerFragment";
  
  public static CheckOutPageCallerFragment newInstance() {
    CheckOutPageCallerFragment fragment = new CheckOutPageCallerFragment();
    return fragment;
  }
  
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data != null) {
      CheckOutPage activity = (CheckOutPage) getActivity();
      switch (requestCode) {
        case NamedItemDialog.RESULT_CODE:
          int lineIndex = activity.getLeastEmptyLineIndexForEntry();
          
          String name = data.getStringExtra(NamedItemDialog.out_bundleKey_name);
          String resourceURL = data.getStringExtra(NamedItemDialog.out_bundleKey_resourceURL);
          Entry entry = new Entry.Builder()
              .setName(name)
              .setLineIndex(lineIndex)
              .setLastCheckDate(Utility.NEVER)
              .setResourceURL(resourceURL)
              .setArchived(false)
              .build();
          
          entry.setModifiedLabel(ModifiedLabel.NEW);
          Log.v(TAG, "Added new " + entry.toString());
          activity.addNewEntryToList(entry);
          break;
        // --------------------------------------
        case ChangeEntryDialog.RESULT_CODE:
          String changedName = data.getStringExtra(ChangeEntryDialog.out_bundleKey_name);
          boolean toDelete = data.getBooleanExtra(ChangeEntryDialog.out_bundleKey_toDelete, false);
          int entryIndex = data.getIntExtra(ChangeEntryDialog.out_bundleKey_entryIndex, -1);
          Entry ref = Cache.getInstance().getEntries().get(entryIndex);
          
          if (toDelete) {
            ref.setModifiedLabel(ModifiedLabel.DELETED);
            Cache.getInstance().markAllCheckOuts(ref.getID(), ModifiedLabel.DELETED);
            Cache.getInstance().markAsDirty();
            activity.deleteEntry(entryIndex);
            activity.refreshWidget();
          } else {
            ref.setName(changedName);
            ref.setModifiedLabel(ModifiedLabel.NEW);
            activity.refreshEntries();
            Cache.getInstance().markAsDirty();
          }
          break;
        // --------------------------------------
        default:
          Log.w(TAG, "Wrong request code has been received! Skipped");
          break;
      }
    } else {
      Log.w(TAG, "CheckOut page caller has received null data!");
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
