package com.orcchg.checkoutapp.widgets;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.orcchg.checkoutapp.CheckOutPage;
import com.orcchg.checkoutapp.OnEntriesIdsRetrievedListener;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.adapters.StrippedWideLineAdapter;
import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.items.StrippedWideLineBlock;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.utils.Utility;
import com.orcchg.checkoutapp.views.AbstractStrippedLineView.OnCellClickListener;
import com.orcchg.checkoutapp.views.ScrollDisabledListView;
import com.orcchg.checkoutapp.views.StrippedWideLineView;

public class MonthlyWidget extends AttendanceWidget {
  private static final String TAG = "CheckOut_MonthlyWidget";
  private static final String bundleKey_currentDateDay = "bundleKey_Today";
  private static final String bundleKey_daysInCurrentMonth = "bundleKey_daysInMonth";
  private static final String bundleKey_currentDateMonth = "bundleKey_Month";
  private static final String bundleKey_currentDateYear = "bundleKey_Year";
  private static final String bundleKey_presentDateDay = "bundleKey_presentDateDay";
  private static final String bundleKey_presentDateMonth = "bundleKey_presentDateMonth";
  private static final String bundleKey_presentDateYear = "bundleKey_presentDateYear";

  private StrippedWideLineBlock mHeaderWideBlock;
  private StrippedWideLineAdapter mMonthlyAttendanceAdapter;
  private StrippedWideLineView.OnCellClickListener mCellClickListener;

  private int SCROLL_MONTHLY_CELLS = 4;
  private int mDaysInMonthToDisplay;
  private Utility.DMY mCurrentDate, mPresentDate;
  
  public static MonthlyWidget newInstance(
      final Utility.DMY currentDate,
      final Utility.DMY presentDate,
      StrippedWideLineView.OnCellClickListener listener) {
    
    MonthlyWidget widget = new MonthlyWidget();
    widget.mCellClickListener = listener;
    Bundle args = new Bundle();
    args.putInt(bundleKey_currentDateDay, currentDate.getDay());
    args.putInt(bundleKey_daysInCurrentMonth, Utility.daysInMonth(currentDate.getMonth(), currentDate.getYear()));
    args.putInt(bundleKey_currentDateMonth, currentDate.getMonth().getValue());
    args.putInt(bundleKey_currentDateYear, currentDate.getYear());
    args.putInt(bundleKey_presentDateDay, presentDate.getDay());
    args.putInt(bundleKey_presentDateMonth, presentDate.getMonth().getValue());
    args.putInt(bundleKey_presentDateYear, presentDate.getYear());
    widget.setArguments(args);
    return widget;
  }
  
  /* Communicate with Activity */
  // --------------------------------------------------------------------------
  private OnEntriesIdsRetrievedListener mListener = null;
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnEntriesIdsRetrievedListener) activity;
    } catch (ClassCastException e) {
      String message = activity.toString() + " must implement OnPersonIdsRetrievedListener inteface!";
      Log.e(TAG, message);
      throw new ClassCastException(message);
    }
  };
  
  /* Lifecycle methods */
  // --------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SCROLL_MONTHLY_CELLS = getResources().getInteger(R.integer.scroll_monthly_cells);
    mHeaderWideBlock = new StrippedWideLineBlock(Utility.NUM_MONTHS);
    String[] month_strings = getResources().getStringArray(R.array.util_months);
    System.arraycopy(month_strings, 0, mHeaderWideBlock.block, 0, Utility.NUM_MONTHS);
    
    Bundle args = getArguments();
    int dayInMonth = args.getInt(bundleKey_currentDateDay);
    mDaysInMonthToDisplay = args.getInt(bundleKey_daysInCurrentMonth);
    Month currentMonth = Month.fromInt(args.getInt(bundleKey_currentDateMonth));
    int currentYear = args.getInt(bundleKey_currentDateYear);
    mCurrentDate = new Utility.DMY(dayInMonth, currentMonth, currentYear);
    int presentDay = args.getInt(bundleKey_presentDateDay);
    Month presentMonth = Month.fromInt(args.getInt(bundleKey_presentDateMonth));
    int presentYear = args.getInt(bundleKey_presentDateYear);
    mPresentDate = new Utility.DMY(presentDay, presentMonth, presentYear);
    
    mMonthlyAttendanceAdapter = new StrippedWideLineAdapter(getActivity().getBaseContext(), mCellClickListener);
    mMonthlyAttendanceAdapter.setCellWidth(R.dimen.monthly_widget_stripped_wide_line_cell_width);
    mMonthlyAttendanceAdapter.setHeight(R.dimen.monthly_widget_stripped_wide_line_cell_height);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of MonthlyWidget");
    View rootView = inflater.inflate(R.layout.monthly_widget_layout, container, false);
    
    final StrippedWideLineView mHeaderWideLineView = (StrippedWideLineView) rootView.findViewById(R.id.monthly_widget_header_in_horizontal_scroll);
    mHeaderWideLineView.setBlock(mHeaderWideBlock);
    if (Utility.DMY.compare(mCurrentDate, mPresentDate) == Utility.DMY.DMYCompare.EQUAL) {
      mHeaderWideLineView.setCellEnabled(mCurrentDate.getMonth().getValue() - 1, true);
    }  
    mHeaderWideLineView.setOnCellClickListener(new OnCellClickListener() {
    @Override
    public void onCellLongClick(int cellNumber, int lineID) {}
    
    @Override
    public void onCellDoubleClick(int cellNumber, int lineID) {
      switchToDailyAtMonth(cellNumber);
    }
    
    @Override
    public void onCellClick(int cellNumber, int lineID) {
      switchToDailyAtMonth(cellNumber);
    }
  });
    
    final HorizontalScrollView scrollView = (HorizontalScrollView) rootView.findViewById(R.id.monthly_widget_horizontal_scroll_view);
    scrollView.post(new Runnable() {
      @Override
      public void run() {
        int scroll_position = (mCurrentDate.getMonth().getValue() - SCROLL_MONTHLY_CELLS) * mHeaderWideLineView.getMeasuredCellWidth();
        scrollView.scrollTo(scroll_position, 0);
      }
    });
    
    mAttendanceList = (ScrollDisabledListView) rootView.findViewById(R.id.monthly_widget_list_in_horizontal_scroll);
    mAttendanceList.setAdapter(mMonthlyAttendanceAdapter);
    initBridgeDelayed();
    return rootView;
  }
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    bindListsViaBridge();
    long[] entriesIDs = mListener.requestForEntriesIds();
    // entries are guaranteed being fetched from Database before this call
    fillMonthlyCheckOuts(entriesIDs);
  }
  
  /* Miscellaneous methods */
  // --------------------------------------------------------------------------
  public void addEmptyLine() {
    addEmptyLine(false);
  }
  
  public void refresh() {
    // XXX: Not working
//    int length = mMonthlyAttendanceAdapter.getCount();
//    for (int i = 0; i < length; ++i) {
//      long entryID = mMonthlyAttendanceAdapter.getItemId(i);
//      CheckOut[] checkouts = Cache.getInstance().getCheckOuts(entryID, mCurrentDate.getMonth());
//      for (int d = 0; d < mDaysInMonth; ++d) {
//        fillCell(i, d, checkouts[d] != null ? true : false);
//      }
//    }
    mMonthlyAttendanceAdapter.notifyDataSetChanged();
  }
  
  public void deleteLine(int lineID) {
    mMonthlyAttendanceAdapter.remove(lineID);
    mMonthlyAttendanceAdapter.notifyDataSetChanged();
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  public void addEmptyLine(boolean isInitializing) {
    StrippedWideLineBlock line = new StrippedWideLineBlock(Utility.NUM_MONTHS);
    addLineToList(line, isInitializing);
  }
  
  private void addLineToList(final StrippedWideLineBlock line, boolean isInitializing) {
    if (mCurrentDate.getYear() < mPresentDate.getYear()) {
      for (int i = 0; i < Utility.NUM_MONTHS; ++i) {
        line.filled_percent_1[i] = 100;
      }
    } else if (mCurrentDate.getYear() > mPresentDate.getYear()) {
      // no fill
    } else {  // equal years
      for (int i = 0; i < mCurrentDate.getMonth().getValue(); ++i) {
        line.filled_percent_1[i] = 100;
      }
      line.filled_percent_1[mCurrentDate.getMonth().getValue() - 1] = mCurrentDate.getDay() * 100 / mDaysInMonthToDisplay;
    }
    mMonthlyAttendanceAdapter.add(line);
    if (!isInitializing) {
      mMonthlyAttendanceAdapter.notifyDataSetChanged();
      mAttendanceList.smoothScrollToPosition(mAttendanceList.getCount() - 1);
    }
  }
  
  private void fillCell(int lineID, int cellIndex, int count, int percent) {
    StrippedWideLineBlock blockRef = (StrippedWideLineBlock) mMonthlyAttendanceAdapter.getItem(lineID);
    blockRef.block[cellIndex] = Integer.toString(count);
    blockRef.filled_percent_2[cellIndex] = percent;
  }
  
  private void fillMonthlyCheckOuts(long[] entriesIDs) {
    for (int i = 0; i < entriesIDs.length; ++i) {
      addEmptyLine(true);  // initializing stage
      for (int m = 0; m < Utility.NUM_MONTHS; ++m) {
        Month month = Utility.monthValues[m + 1];
        int totalCheckouts = Cache.getInstance().getTotalCheckOuts(entriesIDs[i], month);
        int percent = (int) (totalCheckouts * 100.0f / Utility.daysInMonth(month, mCurrentDate.getYear()));
        fillCell(i, m, totalCheckouts, percent);
      }
    }
    mMonthlyAttendanceAdapter.notifyDataSetChanged();
  }
  
  private void switchToDailyAtMonth(int cellNumber) {
    Month month = Month.fromInt(cellNumber + 1);
    CheckOutPage activity = (CheckOutPage) getActivity();
    int day = 1;
    if (month.getValue() == mCurrentDate.getMonth().getValue()) {
      day = mCurrentDate.getDay();
    }
    activity.switchWidget(CheckOutPage.DAILY, day, month, mCurrentDate.getYear());
  }
  
  private void bindListsViaBridge() {
    CheckOutPage activity = (CheckOutPage) getActivity();
    activity.bindListsViaBridge();
  }
}
