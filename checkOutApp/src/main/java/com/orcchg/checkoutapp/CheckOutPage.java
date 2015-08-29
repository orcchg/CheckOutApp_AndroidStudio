package com.orcchg.checkoutapp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.TextDrawable.IShapeBuilder;
import com.orcchg.checkoutapp.adapters.EntriesAdapter;
import com.orcchg.checkoutapp.adapters.SummariesAdapter;
import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.core.DatabaseWriterService;
import com.orcchg.checkoutapp.core.DatabaseWriterThread;
import com.orcchg.checkoutapp.dialogs.ChangeEntryDialog;
import com.orcchg.checkoutapp.dialogs.NamedItemDialog;
import com.orcchg.checkoutapp.dialogs.SettingsDialog;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.items.Summary;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.utils.Utility;
import com.orcchg.checkoutapp.views.ScrollDisabledListView;
import com.orcchg.checkoutapp.views.StrippedLineView;
import com.orcchg.checkoutapp.views.StrippedWideLineView;
import com.orcchg.checkoutapp.widgets.AttendanceWidget;
import com.orcchg.checkoutapp.widgets.DailyWidget;
import com.orcchg.checkoutapp.widgets.ListMultiBridge;
import com.orcchg.checkoutapp.widgets.MonthlyWidget;

public class CheckOutPage extends FragmentActivity implements OnEntriesIdsRetrievedListener {
  private static final String TAG = "CheckOut_CheckOutPage";
  private static final String CALLER_TAG = "CheckOutPage_caller";
  public static final String DIALOG_TAG = "CheckOutPage_dialog";
  private static final String DAILY_WIDGET_TAG = "CheckOutPage_dailyWidget";
  private static final String MONTHLY_WIDGET_TAG = "CheckOutPage_monthlyWidget";

  static final String in_bundleKey_weekday = "in_bundleKey_weekday";
  static final String in_bundleKey_day = "in_bundleKey_day";
  static final String in_bundleKey_month = "in_bundleKey_month";
  static final String in_bundleKey_year = "in_bundleKey_year";
  public static final String out_bundleKey_entryIndex = "out_bundleKey_entryIndex";
  public static final String out_bundleKey_xPositionWindow = "out_bundleKey_xPositionWindow";
  public static final String out_bundleKey_yPositionWindow = "out_bundleKey_yPositionWindow";
  static final String ACTION_ENTRY_CHANGED = "com.orcchg.entry.changed.broadcast";
  static final String ACTION_ENTRY_DELETED = "com.orcchg.entry.deleted.broadcast";
  
  private CheckOutPageCallerFragment mCallerFragment;
  
  private TextView mYearMonthTextView;
  private ListView mEntriesListView;
  private ListView mSummariesListView;
  private ImageButton mAddEntryImageButton;
  private ImageButton mPreviousImageButton, mNextImageButton;
  
  private DatabaseWriterThread mWriterThread;
  private Utility.DMY mCurrentDate, mPresentDate;
  private EntriesAdapter mEntriesAdapter;
  private SummariesAdapter mSummariesAdapter;
  private BroadcastReceiver mEntryChangedReceiver;
  private BroadcastReceiver mEntryDeletedReceiver;
  
  public static final int DAILY = 0;
  public static final int MONTHLY = 1;
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ DAILY, MONTHLY })
  private @interface CurrentWidget {}
  private @CurrentWidget int mCurrentWidget;
  
  /* Lifecycle methods */
  // --------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "CheckOutPage onCreate");
    setContentView(R.layout.checkout_page_layout);
    
    mEntryChangedReceiver = new EntryChangedReceiver(this);
    mEntryDeletedReceiver = new EntryDeletedReceiver(this);
    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mEntryChangedReceiver, new IntentFilter(ACTION_ENTRY_CHANGED));
    LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mEntryDeletedReceiver, new IntentFilter(ACTION_ENTRY_DELETED));
    
    /* Year-Month text header */
    // ------------------------------------------
    Intent intent = getIntent();
//    int dayOfWeek = intent.getIntExtra(in_bundleKey_weekday, 1);
    int dayToDisplay = intent.getIntExtra(in_bundleKey_day, 1);
    int year_month = intent.getIntExtra(in_bundleKey_month, 1);
    Month monthToDisplay = Month.fromInt(year_month);
    int yearToDisplay = intent.getIntExtra(in_bundleKey_year, 2015);
    mCurrentDate = Utility.DMY.valueOf(dayToDisplay, monthToDisplay, yearToDisplay);
    mPresentDate = Utility.DMY.valueOf(mCurrentDate);
    mYearMonthTextView = (TextView) findViewById(R.id.checkout_page_yearmonth_textview);
    
    /* Summaries header and list */
    // ------------------------------------------
    ImageButton mSwitchImageButton = (ImageButton) findViewById(R.id.checkout_page_switch_imagebutton);
    mSwitchImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        switchWidget(mCurrentWidget == DAILY ? MONTHLY : DAILY);
      }
    });
    ImageButton mShareImageButton = (ImageButton) findViewById(R.id.checkout_page_share_imagebutton);
    final ImageButton mSettingsImageButton = (ImageButton) findViewById(R.id.checkout_page_settings_imagebutton);
    mSettingsImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int[] location = new int[2];
        mSettingsImageButton.getLocationInWindow(location);
        int visibleHeight = mSettingsImageButton.getHeight();
        Log.d(TAG, "Location for settings dialog: " + Arrays.toString(location));
        showSettingsDialogAtPosition(location[0], location[1] + visibleHeight);
      }
    });
    mSummariesAdapter = new SummariesAdapter(getBaseContext());
    mSummariesListView = (ScrollDisabledListView) findViewById(R.id.checkout_page_summaries_listview);
    mSummariesListView.setAdapter(mSummariesAdapter);
//    mSummariesListView.setOnItemClickListener(showEntryItemClickListener);
    
    /* Entries list */
    // ------------------------------------------
    mEntriesAdapter = new EntriesAdapter(getBaseContext());
    mEntriesListView = (ListView) findViewById(R.id.checkout_page_entries_listview);
    mEntriesListView.setAdapter(mEntriesAdapter);
    mEntriesListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showEntryPage(position);
//        showEntryDialog(position);
      }});
    
    /* Add new entry button */
    // ------------------------------------------
    IShapeBuilder drawable_config = TextDrawable.builder()
        .beginConfig()
          .textColor(Color.WHITE)
          .useFont(Typeface.DEFAULT)
          .fontSize(getResources().getInteger(R.integer.default_checkout_page_add_entry_imagebutton_text_drawable_size))
        .endConfig();
    
    final TextDrawable drawable = drawable_config.buildRound("+", getResources().getColor(R.color.floating_button_color));
//    final TextDrawable drawable_pressed = drawable_config.buildRound("+", getResources().getColor(R.color.android_red_7));
    
    mAddEntryImageButton = (ImageButton) findViewById(R.id.checkout_page_add_entry_imagebutton);
    mAddEntryImageButton.setImageDrawable(drawable);
    mAddEntryImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showNewDialog(NEW_ENTRY, null);
      }
    });
    
    mPreviousImageButton = (ImageButton) findViewById(R.id.checkout_page_previous);
    mPreviousImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        clickPrevious();
      }
    });
    mNextImageButton = (ImageButton) findViewById(R.id.checkout_page_next);
    mNextImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        clickNext();
      }
    });
    
    /* Checkout widget */
    // ------------------------------------------
    mCallerFragment = CheckOutPageCallerFragment.newInstance();    
    FragmentManager fm = getSupportFragmentManager();
    fm.beginTransaction().remove(mCallerFragment).add(mCallerFragment, CALLER_TAG).commit();
    switchWidget(DAILY);
    
    /* Fetch from database */
    // ------------------------------------------
    fetchEntriesFromDatabase();
    fetchCheckOutsForYearFromDatabase(mCurrentDate.getYear());
  }  // onCreate()
  
  @Override
  protected void onStart() {
    super.onStart();
    mWriterThread = new DatabaseWriterThread();
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    Log.i(TAG, "CheckOutPage onStop");
    mWriterThread.stop();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.i(TAG, "CheckOutPage onDestroy");
    Cache.getInstance().push();
    startService(new Intent(getApplicationContext(), DatabaseWriterService.class));
    // service will close database automatically
    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mEntryChangedReceiver);
    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mEntryDeletedReceiver);
    mEntryChangedReceiver = null;
    mEntryDeletedReceiver = null;
  }
  
  /* Access methods */
  // --------------------------------------------------------------------------
  public ListView getEntriesList() {
    return mEntriesListView;
  }
  
  public ListView getSummariesList() {
    return mSummariesListView;
  }
  
  @Override
  public long[] requestForEntriesIds() {
    return mEntriesAdapter.getItemIds();
  }
  
  /* Internal methods */
  // --------------------------------------------------------------------------
  int getLeastEmptyLineIndexForEntry() {
    return mEntriesAdapter.getCount();
  }
  
  void addNewEntryToList(final Entry entry) {
    Cache.getInstance().addEntry(entry);
    Cache.getInstance().markAsDirty();
    mEntriesAdapter.add(entry);
    mEntriesListView.smoothScrollToPosition(mEntriesListView.getCount() - 1);
    Summary summary = new Summary(entry.getLastCheckDate());
    summary.setArchived(entry.isArchived());
    mSummariesAdapter.add(summary);
    mSummariesListView.smoothScrollToPosition(mSummariesListView.getCount() - 1);
    refreshEntries();
    
    FragmentManager fm = getSupportFragmentManager();
    switch (mCurrentWidget) {
      case DAILY:
        DailyWidget dailyWidget = (DailyWidget) fm.findFragmentByTag(DAILY_WIDGET_TAG);
        dailyWidget.addEmptyLine();
        break;
      case MONTHLY:
        MonthlyWidget monthlyWidget = (MonthlyWidget) fm.findFragmentByTag(MONTHLY_WIDGET_TAG);
        monthlyWidget.addEmptyLine();
        break;
    }
  }
  
  public void refreshEntries() {
    mEntriesAdapter.notifyDataSetChanged();
    mSummariesAdapter.notifyDataSetChanged();
  }
  
  void deleteEntry(int entryIndex) {
    mEntriesAdapter.remove(entryIndex);
    mSummariesAdapter.remove(entryIndex);
    refreshEntries();
    
    FragmentManager fm = getSupportFragmentManager();
    switch (mCurrentWidget) {
      case DAILY:
        DailyWidget dailyWidget = (DailyWidget) fm.findFragmentByTag(DAILY_WIDGET_TAG);
        dailyWidget.deleteLine(entryIndex);
        break;
      case MONTHLY:
        MonthlyWidget monthlyWidget = (MonthlyWidget) fm.findFragmentByTag(MONTHLY_WIDGET_TAG);
        monthlyWidget.deleteLine(entryIndex);
        break;
    }
  }
  
  void refreshWidget() {
    FragmentManager fm = getSupportFragmentManager();
    switch (mCurrentWidget) {
      case DAILY:
        DailyWidget dailyWidget = (DailyWidget) fm.findFragmentByTag(DAILY_WIDGET_TAG);
        dailyWidget.refresh();
        break;
      case MONTHLY:
        MonthlyWidget monthlyWidget = (MonthlyWidget) fm.findFragmentByTag(MONTHLY_WIDGET_TAG);
        monthlyWidget.refresh();
        break;
    }
  }
  
  public void updateSummary(int index, long date) {
    Summary ref = (Summary) mSummariesAdapter.getItem(index);
    ref.setDate(date);
    mSummariesAdapter.notifyDataSetChanged();
  }
  
  public void setSummaryArchived(int index, boolean isArchived) {
    Summary ref = (Summary) mSummariesAdapter.getItem(index);
    ref.setArchived(isArchived);
    mSummariesAdapter.notifyDataSetChanged();
  }
  
  /* Switch widgets */
  // --------------------------------------------------------------------------
  private void switchToYear(int year) {
    Cache.getInstance().needPullCheckoutsAgain();
    fetchCheckOutsForYearFromDatabase(year);  // actualize cache data  
    refreshWidget();
    Log.v(TAG, Cache.getInstance().toString());
  }
  
  private void switchWidget(final @CurrentWidget int type) {
    switchWidget(type, mPresentDate.getDay(), mPresentDate.getMonth(), mPresentDate.getYear());
  }
  
  public void switchWidget(final @CurrentWidget int type, int day, Month month, int year) {
    mCurrentWidget = type;
    mCurrentDate = Utility.DMY.valueOf(day, month, year);
    FragmentManager fm = getSupportFragmentManager();
    switch (type) {
      case DAILY:
        mYearMonthTextView.setText(month.toString(getBaseContext()) + "\n" + Integer.toString(year));
        Fragment dailyWidget = createDailyWidget(day, month, year);
        fm.beginTransaction().replace(R.id.checkout_page_container_framelayout, dailyWidget, DAILY_WIDGET_TAG).commit();
        break;
      case MONTHLY:
        mYearMonthTextView.setText(Integer.toString(year));
        Fragment monthlyWidget = createMonthlyWidget(day, month, year);
        fm.beginTransaction().replace(R.id.checkout_page_container_framelayout, monthlyWidget, MONTHLY_WIDGET_TAG).commit();
        break;
    }
  }
  
  /* Fetch from Database */
  // --------------------------------------------------------------------------
  private void fetchEntriesFromDatabase() {
    Cache.getInstance().pullEntries();
    List<Entry> entries = Cache.getInstance().getEntries();
    mEntriesAdapter.addAll(entries);
    for (Entry entry : entries) {
      Summary summary = new Summary(entry.getLastCheckDate());
      summary.setArchived(entry.isArchived());
      mSummariesAdapter.add(summary);
    }
    // don't scroll lists during initialization stage
  }
  
  private void fetchCheckOutsForYearFromDatabase(int year) {
    Cache.getInstance().pullCheckOutsForYear(year);
  }
  
//  private void fetchCheckOutsForYearInAdvanceFromDatabase(int year) {
//    Cache.getInstance().pullCheckOutsForYearInAdvance(year);
//  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  public void bindListsViaBridge() {
    AttendanceWidget widget = null;
    FragmentManager fm = getSupportFragmentManager();
    switch (mCurrentWidget) {
      case DAILY:
        widget = (DailyWidget) fm.findFragmentByTag(DAILY_WIDGET_TAG);
        break;
      case MONTHLY:
        widget = (MonthlyWidget) fm.findFragmentByTag(MONTHLY_WIDGET_TAG);
        break;
    }
    ListMultiBridge bridge = new ListMultiBridge(mEntriesListView);
    bridge.bindToOuterList(mSummariesListView, widget.getList());
//    mEntriesListView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 50, MotionEvent.ACTION_DOWN, 0, 0, 0));
  }
  
  private DailyWidget createDailyWidget(int day, Month month, int year) {
    StrippedLineView.OnCellClickListener cellClickListener = new StrippedLineView.OnCellClickListener() {
      @Override
      public void onCellClick(int cellNumber, int lineID) {
        FragmentManager fm = getSupportFragmentManager();
        DailyWidget dailyWidget = (DailyWidget) fm.findFragmentByTag(DAILY_WIDGET_TAG);
        dailyWidget.offerCheckOut(lineID, cellNumber);
      }
      @Override
      public void onCellDoubleClick(int cellNumber, int lineID) {}
      @Override
      public void onCellLongClick(int cellNumber, int lineID) {}
    };
    DailyWidget widget = DailyWidget.newInstance(Utility.DMY.valueOf(day, month, year), mPresentDate, cellClickListener);
    return widget;
  }
  
  private MonthlyWidget createMonthlyWidget(int day, Month month, int year) {
    StrippedWideLineView.OnCellClickListener wideCellClickListener = new StrippedWideLineView.OnCellClickListener() {
      @Override
      public void onCellClick(int cellNumber, int lineID) {}
      @Override
      public void onCellDoubleClick(int cellNumber, int lineID) {}
      @Override
      public void onCellLongClick(int cellNumber, int lineID) {}
    };
    MonthlyWidget widget = MonthlyWidget.newInstance(Utility.DMY.valueOf(day, month, year), mPresentDate, wideCellClickListener);
    return widget;
  }
  
  /* Show dialogs */
  // --------------------------------------------------------------------------
  private static final int NEW_ENTRY = 0;
  private static final int CHANGE_ENTRY = 1;
  private static final int SETTINGS = 2;
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ NEW_ENTRY, CHANGE_ENTRY, SETTINGS })
  public @interface DialogType {}
  
  private void showNewDialog(@DialogType int type, Bundle bundle) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    Fragment prev = fm.findFragmentByTag(DIALOG_TAG);
    if (prev != null) {
      ft.remove(prev).commit();
    }
    
    DialogFragment dialog = null;
    int requestCode = 0;
    switch (type) {
      case NEW_ENTRY:
        dialog = NamedItemDialog.newInstance();
        ((NamedItemDialog) dialog).setCallerPage(NamedItemDialog.CHECKOUT_PAGE);
        requestCode = NamedItemDialog.RESULT_CODE;
        break;
      case CHANGE_ENTRY:
        dialog = ChangeEntryDialog.newInstance();
        requestCode = ChangeEntryDialog.RESULT_CODE;
        break;
      case SETTINGS:
        dialog = SettingsDialog.newInstance();
        break;
    }
    if (bundle != null) { dialog.setArguments(bundle); }
    dialog.setTargetFragment(mCallerFragment, requestCode);
    dialog.show(fm, DIALOG_TAG);
  }
  
  private void showEntryPage(int entryIndex) {
    Intent intent = new Intent(getApplicationContext(), EntryPage.class);
    intent.putExtra(EntryPage.in_bundleKey_month, mCurrentDate.getMonth().getValue());
    intent.putExtra(EntryPage.in_bundleKey_year, mCurrentDate.getYear());
    intent.putExtra(EntryPage.in_bundleKey_entryIndex, entryIndex);
    startActivity(intent);
  }
  
  @SuppressWarnings("unused")
  private void showEntryDialog(int entryIndex) {
    Bundle bundle = new Bundle();
    bundle.putInt(out_bundleKey_entryIndex, entryIndex);
    showNewDialog(CHANGE_ENTRY, bundle);
  }
  
  private void showSettingsDialogAtPosition(int x, int y) {
    Bundle bundle = new Bundle();
    bundle.putInt(out_bundleKey_xPositionWindow, x);
    bundle.putInt(out_bundleKey_yPositionWindow, y);
    showNewDialog(SETTINGS, bundle);
  }
  
  /* Navigation items */
  // --------------------------------------------------------------------------
  private void clickPrevious() {
    if (!mNextImageButton.isEnabled()) {
      mNextImageButton.setEnabled(true);
    }
    switch (mCurrentWidget) {
      case DAILY:
        Month month = mCurrentDate.getMonth().getPrevious();
        if (month.getValue() == mCurrentDate.getMonth().getValue()) {
          // not changing month means the border has been reached
          mPreviousImageButton.setEnabled(false);
          return;  // no switch
        } else if (!mPreviousImageButton.isEnabled()) {
          mPreviousImageButton.setEnabled(true);
        }
        int day = Utility.daysInMonth(month, mCurrentDate.getYear());
        if (month.getValue() == mPresentDate.getMonth().getValue()) {
          day = mPresentDate.getDay();
        }
        switchWidget(mCurrentWidget, day, month, mCurrentDate.getYear());
        mCurrentDate = Utility.DMY.valueOf(mCurrentDate.getDay(), month, mCurrentDate.getYear());
        break;
      case MONTHLY:
        Month monthPrevious = Month.DECEMBER;
        int year = mCurrentDate.getYear() - 1;
        if (year <= Utility.YEAR_LIMIT) {
          mPreviousImageButton.setEnabled(false);
        } else if (!mPreviousImageButton.isEnabled()) {
          mPreviousImageButton.setEnabled(true);
        }
        if (year == mPresentDate.getYear()) {
          monthPrevious = mPresentDate.getMonth();
        }
        switchToYear(year);
        switchWidget(mCurrentWidget, mCurrentDate.getDay(), monthPrevious, year);
        mCurrentDate = Utility.DMY.valueOf(mCurrentDate.getDay(), mCurrentDate.getMonth(), year);
        break;
    }
  }
  
  private void clickNext() {
    if (!mPreviousImageButton.isEnabled()) {
      mPreviousImageButton.setEnabled(true);
    }
    switch (mCurrentWidget) {
      case DAILY:
        Month month = mCurrentDate.getMonth().getNext();
        if (month.getValue() == mCurrentDate.getMonth().getValue()) {
          // not changing month means the border has been reached
          mNextImageButton.setEnabled(false);
          return;  // no switch
        } else if (!mNextImageButton.isEnabled()) {
          mNextImageButton.setEnabled(true);
        }
        int day = 1;
        if (month.getValue() == mPresentDate.getMonth().getValue()) {
          day = mPresentDate.getDay();
        }
        switchWidget(mCurrentWidget, day, month, mCurrentDate.getYear());
        mCurrentDate = Utility.DMY.valueOf(mCurrentDate.getDay(), month, mCurrentDate.getYear());
        break;
      case MONTHLY:
        Month monthNext = Month.JANUARY;
        int year = mCurrentDate.getYear() + 1;
        if (year == mPresentDate.getYear()) {
          monthNext = mPresentDate.getMonth();
        }
        switchToYear(year);
        switchWidget(mCurrentWidget, mCurrentDate.getDay(), monthNext, year);
        mCurrentDate = Utility.DMY.valueOf(mCurrentDate.getDay(), mCurrentDate.getMonth(), year);
        break;
    }
  }
  
  private int getListHeight() {
    float displayDensity = getResources().getDisplayMetrics().density;
    return (int) (getResources().getDimension(R.dimen.checkout_page_entries_list_item_height) / displayDensity);
  }
}
