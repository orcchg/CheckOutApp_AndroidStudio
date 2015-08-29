package com.orcchg.checkoutapp.widgets;

import java.util.Arrays;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ListAdapter;

import com.orcchg.checkoutapp.CheckOutPage;
import com.orcchg.checkoutapp.OnEntriesIdsRetrievedListener;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.adapters.StrippedLineAdapter;
import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.core.ModifiedLabel;
import com.orcchg.checkoutapp.items.CheckOut;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.items.StrippedLineBlock;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.utils.Utility;
import com.orcchg.checkoutapp.views.ScrollDisabledListView;
import com.orcchg.checkoutapp.views.StrippedLineView;

public class DailyWidget extends AttendanceWidget {
  private static final String TAG = "CheckOut_DailyWidget";
  private static final String bundleKey_NumCols = "bundleKey_NumCols";
  private static final String bundleKey_currentDateDay = "bundleKey_Today";
  private static final String bundleKey_currentDateMonth = "bundleKey_Month";
  private static final String bundleKey_currentDateYear = "bundleKey_Year";
  private static final String bundleKey_presentDateDay = "bundleKey_presentDateDay";
  private static final String bundleKey_presentDateMonth = "bundleKey_presentDateMonth";
  private static final String bundleKey_presentDateYear = "bundleKey_presentDateYear";
  
  private StrippedLineBlock mHeaderBlock;
  private StrippedLineAdapter mDailyCheckOutsAdapter;
  private StrippedLineView.OnCellClickListener mCellClickListener;
  
  private int SCROLL_DAILY_CELLS = 8;
  private int mNumCols = 0;
  private int mDaysInMonth;
  private Utility.DMY mCurrentDate, mPresentDate;
  
  public static DailyWidget newInstance(
      final Utility.DMY currentDate,
      final Utility.DMY presentDate,
      StrippedLineView.OnCellClickListener listener) {
    
    int days_in_month = Utility.daysInMonth(currentDate.getMonth(), currentDate.getYear());
    if (currentDate.getDay() < 0 || currentDate.getDay() > days_in_month) {
      throw new IllegalArgumentException("Day value [" + currentDate.getDay() + "] is out of bounds within month: " + currentDate.getMonth().toString());
    }
    
    DailyWidget widget = new DailyWidget();
    widget.mCellClickListener = listener;
    Bundle args = new Bundle();
    args.putInt(bundleKey_NumCols, days_in_month);
    args.putInt(bundleKey_currentDateDay, currentDate.getDay());
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
    SCROLL_DAILY_CELLS = getResources().getInteger(R.integer.scroll_daily_cells);
    Bundle args = getArguments();
    mNumCols = args.getInt(bundleKey_NumCols);
    mHeaderBlock = new StrippedLineBlock(mNumCols);
    for (int i = 0; i < mNumCols; ++i) {
      mHeaderBlock.block[i] = i + 1;  // day counter in a month
    }
    int dayInMonth = args.getInt(bundleKey_currentDateDay);
    Month currentMonth = Month.fromInt(args.getInt(bundleKey_currentDateMonth));
    int currentYear = args.getInt(bundleKey_currentDateYear);
    mCurrentDate = new Utility.DMY(dayInMonth, currentMonth, currentYear);
    int presentDay = args.getInt(bundleKey_presentDateDay);
    Month presentMonth = Month.fromInt(args.getInt(bundleKey_presentDateMonth));
    int presentYear = args.getInt(bundleKey_presentDateYear);
    mPresentDate = new Utility.DMY(presentDay, presentMonth, presentYear);
    mDaysInMonth = Utility.daysInMonth(currentMonth, currentYear);
    
    mDailyCheckOutsAdapter = new StrippedLineAdapter(getActivity().getBaseContext(), mCellClickListener);
    mDailyCheckOutsAdapter.setCellWidth(R.dimen.daily_widget_stripped_line_cell_width);
    mDailyCheckOutsAdapter.setHeight(R.dimen.daily_widget_stripped_line_cell_height);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of DailyWidget");
    View rootView = inflater.inflate(R.layout.daily_widget_layout, container, false);
    
    final StrippedLineView mHeaderLineView = (StrippedLineView) rootView.findViewById(R.id.daily_widget_header_in_horizontal_scroll);
    mHeaderLineView.setBlock(mHeaderBlock);
    if (Utility.DMY.compare(mCurrentDate, mPresentDate) == Utility.DMY.DMYCompare.EQUAL) {
      mHeaderLineView.setCellEnabled(mCurrentDate.getDay() - 1, true);
    }
    mHeaderLineView.setTypeface(Typeface.BOLD);

    final HorizontalScrollView scrollView = (HorizontalScrollView) rootView.findViewById(R.id.daily_widget_horizontal_scroll_view);
    scrollView.post(new Runnable() {
      @Override
      public void run() {
        int scroll_position = (mCurrentDate.getDay() - SCROLL_DAILY_CELLS) * mHeaderLineView.getMeasuredCellWidth();
        scrollView.scrollTo(scroll_position, 0);
      }
    });
    
    mAttendanceList = (ScrollDisabledListView) rootView.findViewById(R.id.daily_widget_list_in_horizontal_scroll);
    mAttendanceList.setAdapter(mDailyCheckOutsAdapter);
    initBridgeDelayed();
    return rootView;
  }
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    bindListsViaBridge();
    long[] entriesIDs = mListener.requestForEntriesIds();
    // entries are guaranteed being fetched from Database before this call
    fillDailyCheckOuts(entriesIDs);
  }
  
  /* Miscellaneous methods */
  // --------------------------------------------------------------------------
  public void addEmptyLine() {
    addEmptyLine(false);
  }
  
  public void offerCheckOut(int lineID, int cellIndex) {
    CheckOutPage activity = (CheckOutPage) getActivity();
    ListAdapter entriesAdapter = activity.getEntriesList().getAdapter();
    long entryID = entriesAdapter.getItemId(lineID);

    Utility.DMY date = Utility.DMY.valueOf(cellIndex + 1, mCurrentDate.getMonth(), mCurrentDate.getYear());
    CheckOut ref = Cache.getInstance().getCheckOut(entryID, date);
    boolean isFilled = isFilled(lineID, cellIndex);
    
    Entry entryRef = (Entry) entriesAdapter.getItem(lineID);
    if (!isFilled && entryRef.getLastCheckDate() < date.toLong()) {
      entryRef.setLastCheckDate(date.toLong());
      entryRef.setModifiedLabel(ModifiedLabel.MODIFIED);
      // hope that clicking at Today's cell will not involve wrong setting
      // of summary to be labeled as Future, because we set date for entry
      // just before we update summary
      activity.updateSummary(lineID, date.toLong());
    } else if (isFilled && date.toLong() == entryRef.getLastCheckDate()) {
      // find previous checkout with the most recent date
      CheckOut previousCheckout = Cache.getInstance().getPreviousCheckOut(entryID, date);
      if (previousCheckout != null) {  // got previous checkout in this month or year
        long previousDate = previousCheckout.getDate().toLong();
        entryRef.setLastCheckDate(previousDate);
        entryRef.setModifiedLabel(ModifiedLabel.MODIFIED);
        activity.updateSummary(lineID, previousDate);
      } else {  // previous checkout for date doesn't exist in current year or storage
        // TODO: unknown summary
        entryRef.setLastCheckDate(Utility.NEVER);
        entryRef.setModifiedLabel(ModifiedLabel.MODIFIED);
        activity.updateSummary(lineID, Utility.NEVER);
      }
    } else if (isFilled && date.toLong() > entryRef.getLastCheckDate()) {
      // impossible to uncheck checkout which date is larger than last checkout date of entry
      String message = "[BUG]: it must be impossible to uncheck already checked checkout which date is larger than the date of the last checkout of entry";
      Log.e(TAG, message + "\ntouch date: " + date.toLong() + ", last checkout date: " + entryRef.getLastCheckDate());
      throw new RuntimeException(message);
    }  // else - isFilled and date.toLong() < last checkout date - do nothing
    
    if (ref != null) {  // modify existing item
      Log.d(TAG, "Modified checkout for entry with ID[" + entryID + "] at cell: " + cellIndex);
      ModifiedLabel label = isFilled ? ModifiedLabel.DELETED : ModifiedLabel.MODIFIED;
      ref.setModifiedLabel(label);
      if (isFilled) {
        entryRef.decrementSupplementaryValue();
      } else {
        entryRef.incrementSupplementaryValue();
      }
      entryRef.setModifiedLabel(ModifiedLabel.MODIFIED);
    } else {  // spawn new item
      Log.d(TAG, "New checkout will be spawned for entry with ID[" + entryID + "] at cell: " + cellIndex);
      CheckOut checkout = new CheckOut.Builder(entryID, date).build();
      checkout.setModifiedLabel(ModifiedLabel.NEW);
      Cache.getInstance().addCheckOut(checkout);
      entryRef.incrementSupplementaryValue();
      entryRef.setModifiedLabel(ModifiedLabel.MODIFIED);
    }
    fillCell(lineID, cellIndex, !isFilled);
    mDailyCheckOutsAdapter.notifyDataSetChanged();
    activity.refreshEntries();
    Cache.getInstance().markAsDirty();
  }
  
  public void refresh() {
    // XXX: Not working
//    int length = mDailyCheckOutsAdapter.getCount();
//    for (int i = 0; i < length; ++i) {
//      long entryID = mDailyCheckOutsAdapter.getItemId(i);
//      CheckOut[] checkouts = Cache.getInstance().getCheckOuts(entryID, mCurrentDate.getMonth());
//      for (int d = 0; d < mDaysInMonth; ++d) {
//        fillCell(i, d, checkouts[d] != null ? true : false);
//      }
//    }
    mDailyCheckOutsAdapter.notifyDataSetChanged();
  }
  
  public void deleteLine(int lineID) {
    mDailyCheckOutsAdapter.remove(lineID);
    mDailyCheckOutsAdapter.notifyDataSetChanged();
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  public void addEmptyLine(boolean isInitializing) {
    StrippedLineBlock line = new StrippedLineBlock(mNumCols);
    addLineToList(line, isInitializing);
  }
  
  private void addLineToList(final StrippedLineBlock line, boolean isInitializing) {
    if (mCurrentDate.getYear() < mPresentDate.getYear() ||
        (mCurrentDate.getYear() == mPresentDate.getYear() &&
        mCurrentDate.getMonth().getValue() < mPresentDate.getMonth().getValue())) {
      Arrays.fill(line.filled_1, 0, line.filled_1.length, true);
    } else if (mCurrentDate.getYear() > mPresentDate.getYear() ||
        (mCurrentDate.getYear() == mPresentDate.getYear() &&
        mCurrentDate.getMonth().getValue() > mPresentDate.getMonth().getValue())) {
      // no fill
    } else {  // equal years and months
      Arrays.fill(line.filled_1, 0, mCurrentDate.getDay() - 1, true);
    }
    mDailyCheckOutsAdapter.add(line);
    if (!isInitializing) {
      mDailyCheckOutsAdapter.notifyDataSetChanged();
      mAttendanceList.smoothScrollToPosition(mAttendanceList.getCount() - 1);
    }
  }
  
  private void fillCell(int lineID, int cellIndex, boolean willBeFilled) {
    StrippedLineBlock blockRef = (StrippedLineBlock) mDailyCheckOutsAdapter.getItem(lineID);
    blockRef.block[cellIndex] = willBeFilled ? -1 : 0;
    blockRef.filled_2[cellIndex] = willBeFilled;
  }
  
  private boolean isFilled(int lineID, int cellIndex) {
    StrippedLineBlock blockRef = (StrippedLineBlock) mDailyCheckOutsAdapter.getItem(lineID);
    return blockRef.filled_2[cellIndex];
  }
  
  private void fillDailyCheckOuts(long[] entriesIDs) {
    for (int i = 0; i < entriesIDs.length; ++i) {
      addEmptyLine(true);  // initializing stage
      CheckOut[] checkouts = Cache.getInstance().getCheckOuts(entriesIDs[i], mCurrentDate.getMonth());
      for (int d = 0; d < mDaysInMonth; ++d) {
        fillCell(i, d, checkouts[d] != null ? true : false);
      }
    }
    mDailyCheckOutsAdapter.notifyDataSetChanged();
  }
  
  private void bindListsViaBridge() {
    CheckOutPage activity = (CheckOutPage) getActivity();
    activity.bindListsViaBridge();
  }
}
