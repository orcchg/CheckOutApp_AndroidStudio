package com.orcchg.checkoutapp.widgets;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orcchg.checkoutapp.CheckOutPage;

public class AttendanceWidget extends Fragment {
  private static final String TAG = "CheckOut_AttendanceWidget";
  
  protected ListView mAttendanceList;
  private ListMultiBridge mBridge;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }
  
  @Override
  public void onResume() {
    super.onResume();
    /**
     * Assumption that a half second delay is long enough
     * to render the layout and start the timer in onResume()
     */
    if (mBridge.getHandler() != null) {
      mBridge.getHandler().sendEmptyMessageDelayed(0, 500);
    }
  }
  
  public ListView getList() {
    return mAttendanceList;
  }
  
  protected void initBridgeDelayed() {
    if (mAttendanceList == null) {
      String message = "Attendance listview has not been initialized!";
      Log.e(TAG, message);
      throw new IllegalStateException(message);
    }
    mBridge = new ListMultiBridge(mAttendanceList);
  }

  protected void bindFromOuterList(final ListView... outerList) {
    mBridge.bindFromOuterList(outerList);
  }
  
  protected void bindToOuterList(final ListView... outerList) {
    mBridge.bindToOuterList(outerList);
  }
  
  protected void bindWithOuterList(final ListView... outerList) {
    mBridge.bindWithOuterList(outerList);
  }
}
