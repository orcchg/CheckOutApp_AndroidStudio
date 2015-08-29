package com.orcchg.checkoutapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.orcchg.checkoutapp.dialogs.AlertDialog;
import com.orcchg.checkoutapp.dialogs.NamedItemDialog;

public class EntryPageCallerFragment extends Fragment {
  private static final String TAG = "CheckOut_EntryPageCallerFragment";
  
  public static EntryPageCallerFragment newInstance() {
    EntryPageCallerFragment fragment = new EntryPageCallerFragment();
    return fragment;
  }
  
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data != null) {
      EntryPage activity = (EntryPage) getActivity();
      switch (requestCode) {
        case AlertDialog.RESULT_CODE:
          boolean result = data.getBooleanExtra(AlertDialog.out_bundleKey_result, false);
          if (result) {
            activity.deleteEntry();
          }
          break;
        case NamedItemDialog.RESULT_CODE:
          String name = data.getStringExtra(NamedItemDialog.out_bundleKey_name);
          String resourceURL = data.getStringExtra(NamedItemDialog.out_bundleKey_resourceURL);
          activity.modifyEntry(name, resourceURL);
          break;
        default:
          Log.w(TAG, "Wrong request code has been received! Skipped");
          break;
      }
    } else {
      Log.w(TAG, "Entry page caller has received null data!");
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
