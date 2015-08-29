package com.orcchg.checkoutapp.widgets;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ListBridge {
  private static final String TAG = "CheckOut_ListBridge";
  
  final private ListView mList;
  private View mClickSource, mTouchSource;
  private int mOffset = 0;
  private Handler mHandler = null;
  
  public ListBridge(final ListView list) {
    mList = list;
  }
  
  public Handler getHandler() {
    return mHandler;
  }
  
  /**
   * Scrolling outerlist involves scrolling of mList
   * @param outerList
   */
  public void bindFromOuterList(final ListView outerList) {
    outerList.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (mTouchSource == null) {
          mTouchSource = v;
        }
        
        if (v == mTouchSource) {
          mList.dispatchTouchEvent(event);
          switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
              v.performClick();
            case MotionEvent.ACTION_CANCEL:
              mClickSource = v;
              mTouchSource = null;
              break;
          }
        }
        return false;
      }});
    
    final OnItemClickListener outerItemClickListener = outerList.getOnItemClickListener();
    if (outerItemClickListener != null) {
      outerList.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          if (parent == mClickSource) {
            outerItemClickListener.onItemClick(parent, view, position, id);
          }
        }});
    }
    
    outerList.setOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {}
      
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view == mClickSource) {
          mList.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() + mOffset);
        }
      }
    });
    
    if (mHandler == null) {
      mHandler = new InternalHandler(this, outerList);
    }
  }
  
  /**
   * Scrolling mList involves scrolling of outerlist
   * @param outerList
   */
  public void bindToOuterList(final ListView outerList) {
    mList.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (mTouchSource == null) {
          mTouchSource = v;
        }
        
        if (v == mTouchSource) {
          outerList.dispatchTouchEvent(event);
          switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
              v.performClick();
            case MotionEvent.ACTION_CANCEL:
              mClickSource = v;
              mTouchSource = null;
              break;
          }
        }
        return false;
      }});
    
    final OnItemClickListener innerItemClickListener = mList.getOnItemClickListener();
    if (innerItemClickListener != null) {
      mList.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          if (parent == mClickSource) {
            innerItemClickListener.onItemClick(parent, view, position, id);
          }
        }});
    }
    
    mList.setOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {}
      
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view == mClickSource) {
          outerList.setSelectionFromTop(firstVisibleItem, view.getChildAt(0).getTop() - mOffset);
        }
      }
    });
    
    if (mHandler == null) {
      mHandler = new InternalHandler(this, outerList);
    }
  }
  
  /**
   * Duplex scrolling regardless the listview touched
   * @param outerList
   */
  public void bindWithOuterList(final ListView outerList) {
    bindFromOuterList(outerList);
    bindToOuterList(outerList);
  }
  
  /* Handler */
  /**
   * Imitation of callback the moment the entire layout have been rendered
   * in order to balance listviews, which could be slightly different in heights
   */
  private static class InternalHandler extends Handler {
    private final WeakReference<ListBridge> mWidget;
    private final WeakReference<ListView> mOuterList;
    
    private InternalHandler(final ListBridge widget, final ListView outerList) {
      mWidget = new WeakReference<ListBridge>(widget);
      mOuterList = new WeakReference<ListView>(outerList);
    }
    
    @Override
    public void handleMessage(Message msg) {
      ListBridge widget = mWidget.get();
      ListView outerList = mOuterList.get();
      if (widget == null || outerList == null) {
        return;
      }
      
      // Set outerList's x, y coordinates in loc[0], loc[1]
      int[] loc = new int[2];
      outerList.getLocationInWindow(loc);

      // Save outerList's y and get mList's coordinates
      int firstY = loc[1];
      widget.mList.getLocationInWindow(loc);

      widget.mOffset = firstY - loc[1];
      Log.d(TAG, "Balanced current listview with outer listview");
    }
  }
}
