package com.orcchg.checkoutapp;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.core.ModifiedLabel;
import com.orcchg.checkoutapp.dialogs.AlertDialog;
import com.orcchg.checkoutapp.dialogs.NamedItemDialog;
import com.orcchg.checkoutapp.items.CheckOut;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.widgets.EntryWidget;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

public class EntryPage extends FragmentActivity {
  private static final String TAG = "CheckOut_EntryPage";
  private static final String CALLER_TAG = "EntryPage_caller";
  public static final String DIALOG_TAG = "EntryPage_dialog";
  private static final String ENTRY_WIDGET_TAG = "EntryPage_entryWidget";
  private static final String CALENDAR_TAG = "EntryPage_calendar";
  
  static final String in_bundleKey_month = "in_bundleKey_month";
  static final String in_bundleKey_year = "in_bundleKey_year";
  static final String in_bundleKey_entryIndex = "in_bundleKey_entryIndex";
  static final String out_bundleKey_entryIndex = in_bundleKey_entryIndex;
  
  private EntryPageCallerFragment mCallerFragment;
  private String mDeleteEntryMessage;
  private String mDeleteEntrySummary;
  private long mEntryID;
  private int mEntryIndex;
  private int mMonth;
  private int mYear;
  private String mEntryName;
  private String mEntryResourceURL;
  
  /* Lifecycle methods */
  // --------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "EntryPage onCreate");
    setContentView(R.layout.entry_page_layout);
    
    Intent intent = getIntent();
    mMonth = intent.getIntExtra(in_bundleKey_month, 0);
    mYear = intent.getIntExtra(in_bundleKey_year, 0);
    mEntryIndex = intent.getIntExtra(in_bundleKey_entryIndex, 0);
    
    mDeleteEntryMessage = getBaseContext().getResources().getString(R.string.util_delete_entry_title);
    mDeleteEntrySummary = getBaseContext().getResources().getString(R.string.util_delete_entry_summary);
    
    /* Entry widget */
    // ------------------------------------------
    Entry entry = Cache.getInstance().getEntries().get(mEntryIndex);
    mEntryID = entry.getID();
    mEntryName = entry.getName();
    mEntryResourceURL = entry.getResourceURL();
    final EntryWidget entryWidget = EntryWidget.newInstance(entry);
    
    /* Calendar widget */
    // ------------------------------------------
    Log.i(TAG, "Initializing Calendar with Month " + mMonth + " and Year " + mYear);
    final CaldroidFragment calendarWidget = CaldroidFragment.newInstance(CALENDAR_TAG, mMonth, mYear);
    Bundle args = new Bundle();
    args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
    calendarWidget.setArguments(args);
    calendarWidget.setCaldroidListener(new CaldroidListener() {
      @Override
      public void onSelectDate(Date date, View view) {
        // Disabled checkouts modification - read-only area!
//        Utility.DMY udate = Utility.DMY.valueOf(date);
//        Log.d(TAG, "Selected date in calendar: " + udate.toString());
//        CheckOut ref = Cache.getInstance().getCheckOut(mEntryID, udate);
//        boolean isFilled = calendarWidget.isSelectedDate(date);
//        
//        if (ref != null) {  // modify existing item
//          Log.d(TAG, "Modified checkout for entry with ID[" + mEntryID + "] at calendar date: " + udate.toString());
//          ModifiedLabel label = isFilled ? ModifiedLabel.DELETED : ModifiedLabel.MODIFIED;
//          ref.setModifiedLabel(label);
//        } else {  // spawn new item
//          Log.d(TAG, "New checkout will be spawned for entry with ID[" + mEntryID + "] at calendar date: " + udate.toString());
//          CheckOut checkout = new CheckOut.Builder(mEntryID, udate).build();
//          checkout.setModifiedLabel(ModifiedLabel.NEW);
//          Cache.getInstance().addCheckOut(checkout);
//        }
//        if (isFilled) {
//          calendarWidget.clearSelectedDate(date);
//        } else {
//          calendarWidget.setSelectedDate(date);
//        }
//        Cache.getInstance().markAsDirty();
//        calendarWidget.refreshView();
      }
      @Override
      public void onChangeMonth(int month, int year) {
        setSelectedDates(Month.fromInt(month), year);
      }
    });
    
    mCallerFragment = EntryPageCallerFragment.newInstance();
    FragmentManager fm = getSupportFragmentManager();
    fm.beginTransaction().remove(mCallerFragment).add(mCallerFragment, CALLER_TAG).commit();
    fm.beginTransaction().replace(R.id.entry_widget_container, entryWidget, ENTRY_WIDGET_TAG).commit();
    fm.beginTransaction().replace(R.id.calendar_container, calendarWidget, CALENDAR_TAG).commit();
  }  // onCreate()
  
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Log.i(TAG, "EntryPage onBackPressed");
    closeWithResult();
  }
  
  /* Internal methods */
  // --------------------------------------------------------------------------
  public void showEntryUpdateDialog() {
    NamedItemDialog dialog = NamedItemDialog.newInstance(mEntryName, mEntryResourceURL);
    dialog.setCallerPage(NamedItemDialog.ENTRY_PAGE);
    dialog.setTargetFragment(mCallerFragment, NamedItemDialog.RESULT_CODE);
    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
  }
  
  public void showAlertDialog() {
    AlertDialog dialog = AlertDialog.newInstance(mDeleteEntryMessage, mDeleteEntrySummary);
    dialog.setTargetFragment(mCallerFragment, AlertDialog.RESULT_CODE);
    dialog.show(getSupportFragmentManager(), DIALOG_TAG);
  }
  
  void modifyEntry(final String name, final String resourceURL) {
    EntryWidget fragment = (EntryWidget) getSupportFragmentManager().findFragmentByTag(ENTRY_WIDGET_TAG);
    fragment.modifyEntry(name, resourceURL);
  }
  
  void deleteEntry() {
    Entry ref = Cache.getInstance().getEntries().get(mEntryIndex);
    ref.setModifiedLabel(ModifiedLabel.DELETED);
    Cache.getInstance().markAllCheckOuts(ref.getID(), ModifiedLabel.DELETED);
    Cache.getInstance().markAsDirty();
    broadcastEntryDeleted();
    finish();  // close entry page
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private void broadcastEntryChanged() {
    Intent intent = new Intent();
    intent.setAction(CheckOutPage.ACTION_ENTRY_CHANGED);
    intent.putExtra(out_bundleKey_entryIndex, mEntryIndex);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
  }
  
  private void broadcastEntryDeleted() {
    Intent intent = new Intent();
    intent.setAction(CheckOutPage.ACTION_ENTRY_DELETED);
    intent.putExtra(out_bundleKey_entryIndex, mEntryIndex);
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
  }
  
  private void setSelectedDates(final Month month, int year) {
    // TODO: handle year changes
    CheckOut[] checkouts = Cache.getInstance().getCheckOuts(mEntryID, month);
    FragmentManager fm = getSupportFragmentManager();
    CaldroidFragment calendar = (CaldroidFragment) fm.findFragmentByTag(CALENDAR_TAG);
    for (int i = 0; i < checkouts.length; ++i) {
      if (checkouts[i] != null) {
        Date date = checkouts[i].getDate().toDate();
        Log.v(TAG, "Selected date: " + checkouts[i].getDate() + ", after conversion: " + date.toString());
        calendar.setSelectedDate(date);
      }
    }
    calendar.refreshView();
  }
  
  private void closeWithResult() {
    EntryWidget entryWidget = (EntryWidget) getSupportFragmentManager().findFragmentByTag(ENTRY_WIDGET_TAG);
    Bundle bundle = entryWidget.requestResult();
    boolean isModified = bundle.getBoolean(EntryWidget.out_bundleKey_isModified, false);
    if (isModified) {
      String name = bundle.getString(EntryWidget.out_bundleKey_name);
      String resourceURL = bundle.getString(EntryWidget.out_bundleKey_resourceURL);
      boolean isArchived = bundle.getBoolean(EntryWidget.out_bundleKey_isArchived);
      Entry ref = Cache.getInstance().getEntries().get(mEntryIndex);
      ref.setName(name);
      ref.setResourceURL(resourceURL);
      ref.setArchived(isArchived);
      ref.setModifiedLabel(ModifiedLabel.MODIFIED);
      Cache.getInstance().markAsDirty();
      broadcastEntryChanged();
    }
    finish();
  }
}
