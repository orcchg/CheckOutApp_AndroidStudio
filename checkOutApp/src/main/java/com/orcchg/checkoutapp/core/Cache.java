package com.orcchg.checkoutapp.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orcchg.checkoutapp.items.CheckOut;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.utils.Utility;

public class Cache {
  private static final String TAG = "CheckOut_Cache";
  
  private static Cache INSTANCE;
  private boolean mPulledEntries = false;
  private boolean mPullCheckOuts = false;
  private boolean mDirtyBit = false;  // whether Database is out of date
  
  private List<Entry> mEntries;
  private Map<Long, Map<Month, CheckOut[]>> mCheckOutsInPreviousYear;
  private Map<Long, Map<Month, CheckOut[]>> mCheckOutsInYear;
  private Map<Long, Map<Month, CheckOut[]>> mCheckOutsInNextYear;
  private Map<Long, Map<Month, CheckOut[]>> mStorage;
  
  public static void init() {
    INSTANCE = new Cache();
  }
  
  public static Cache getInstance() {
    return INSTANCE;
  }
  
  @SuppressLint("UseSparseArrays")
  private Cache() {
    mCheckOutsInYear = new HashMap<Long, Map<Month, CheckOut[]>>();
//    mCheckOutsInPreviousYear = new HashMap<Long, Map<Month, CheckOut[]>>();
//    mCheckOutsInNextYear = new HashMap<Long, Map<Month, CheckOut[]>>();
    thisStorage();
  }
  
  /* Public API */
  // --------------------------------------------------------------------------
  public boolean isDirty() {
    return mDirtyBit;
  }
  
  @NonNull
  public List<Entry> getEntries() {
    return mEntries;
  }
  
  @NonNull
  public CheckOut[] getCheckOuts(long entryID, final Month month) {
    return getStorage().get(Long.valueOf(entryID)).get(month);
  }
  
  public int getTotalCheckOuts(long entryID, final Month month) {
    CheckOut[] checkouts = getCheckOuts(entryID, month);
    int total = 0;
    for (CheckOut checkout : checkouts) {
      if (checkout != null) {
        ++total;
      }
    }
    return total;
  }
  
  /**
   * Gets checkout for specified entry and date
   * @param entryID
   * @param date
   * @return Checkout instance, could be null
   */
  @Nullable
  public CheckOut getCheckOut(long entryID, final Utility.DMY date) {
    Log.v(TAG, "Get checkout for date: " + date.toString());
    CheckOut[] ref = getStorage().get(Long.valueOf(entryID)).get(date.getMonth());
    return ref[date.getDay() - 1];
  }
  
  /**
   * Gets checkout which is followed by specified date for certain entry
   * @param entryID
   * @param date
   * @return CheckOut entry which date is the most recent date before
   * the specified date, not inclusive. Null in case there is not such checkouts
   * in the Cache - probably, look for in another storage or date is to old
   */
  @Nullable
  public CheckOut getPreviousCheckOut(long entryID, final Utility.DMY date) {
    {  // try current month
      CheckOut[] checkouts = getStorage().get(Long.valueOf(entryID)).get(date.getMonth());
      for (int day = date.getDay() - 2; day >= 0; --day) {
        CheckOut checkout = checkouts[day];
        if (checkout != null && checkout.getModifiedLabel() != ModifiedLabel.DELETED) {
          return checkout;
        }
      }
    }
    // try current year
    Month previousMonth = date.getMonth().getPreviousNone();
    while (previousMonth != Month.NONE) {
      CheckOut[] checkouts = getStorage().get(Long.valueOf(entryID)).get(previousMonth);
      for (int day = Utility.DAYS_LIMIT - 1; day >= 0; --day) {
        CheckOut checkout = checkouts[day];
        if (checkout != null && checkout.getModifiedLabel() != ModifiedLabel.DELETED) {
          return checkout;
        }
      }
      previousMonth = previousMonth.getPreviousNone();
    }
    // not in this year
    return null;
  }
  
  public void markAllCheckOuts(long entryID, final ModifiedLabel label) {
    Map<Month, CheckOut[]> submap = getStorage().get(Long.valueOf(entryID));
    for (int m = 0; m < Utility.NUM_MONTHS; ++m) {
      CheckOut[] checkouts = submap.get(Utility.monthValues[m + 1]);
      for (CheckOut checkout : checkouts) {
        if (checkout != null) {
          checkout.setModifiedLabel(label);
        }
      }
    }
  }
  
  /* Switch storage */
  // --------------------------------------------------------------------------
  protected void prevStorage() {
    mStorage = mCheckOutsInPreviousYear;
  }
  
  protected void thisStorage() {
    mStorage = mCheckOutsInYear;
  }
  
  protected void nextStorage() {
    mStorage = mCheckOutsInNextYear;
  }
  
//  public void switchStorage() {
//    if (mStorage == mCheckOutsInYear) {
//      nextStorage();
//      return;
//    } else if (mStorage == mCheckOutsInPreviousYear) {
//      thisStorage();
//      return;
//    } else if (mStorage == mCheckOutsInNextYear) {
//      prevStorage();
//      return;
//    } else {
//      String message = "[BUG] Storage cannot be null!";
//      Log.e(TAG, message);
//      throw new RuntimeException(message);
//    }
//  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Cache: \n").append("  ");
    for (int i = 1; i <= Utility.NUM_MONTHS; ++i) {
      builder.append(Month.fromInt(i).toLetter()).append(" ");
    }
    builder.append('\n');
    for (Entry entry : mEntries) {
      builder.append(entry.getID()).append(' ');
      Map<Month, CheckOut[]> ref = getStorage().get(Long.valueOf(entry.getID()));
      for (int i = 1; i <= Utility.NUM_MONTHS; ++i) {
        CheckOut[] checkouts = ref.get(Month.fromInt(i));
        int total = 0;
        for (int j = 0; j < checkouts.length; ++j) {
          if (checkouts[j] != null) {
            ++total;
          }
        }
        if (total > 0) {
          builder.append(total).append(' ');
        } else {
          builder.append("  ");
        }
      }
      builder.append('\n');
    }
    return builder.toString();
  }
  
  /* New entities to be stored */
  // --------------------------------------------
  public void addEntry(final Entry entry) {
    mEntries.add(entry);
    getStorage().put(Long.valueOf(entry.getID()), createMonthlyMap());
  }
  
  public void addCheckOut(final CheckOut checkout) {
    CheckOut[] ref = getStorage().get(Long.valueOf(checkout.getEntryID())).get(checkout.getDate().getMonth());
    ref[checkout.getDate().getDay() - 1] = checkout;
  }
  
  /* Fetch from database */
  // --------------------------------------------------------------------------
  public int pullEntries() {
    if (mPulledEntries) {
      Log.d(TAG, "Entries have been already pulled from database");
      return 0;
    }
    mEntries = Database.getInstance().getAllEntries();
    Log.d(TAG, "Fecthed entries: " + mEntries.size());
    for (Entry entry : mEntries) {
      entry.setModifiedLabel(ModifiedLabel.OLD);
//      prevStorage();  getStorage().put(Long.valueOf(entry.getID()), createMonthlyMap());
//      nextStorage();  getStorage().put(Long.valueOf(entry.getID()), createMonthlyMap());
      thisStorage();  getStorage().put(Long.valueOf(entry.getID()), createMonthlyMap());
    }
    mPulledEntries = true;
    return mEntries.size();
  }
  
  public int pullCheckOutsForYear(int year) {
    if (year < Utility.YEAR_LIMIT) {
      Log.d(TAG, "Lower year boundary has been reached, nothing to be pulled");
      return 0;
    }
    if (mPullCheckOuts) {
      Log.d(TAG, "Checkouts have been already pulled from database");
      return 0;
    }
    if (mEntries.isEmpty()) {
      Log.i(TAG, "No entries have been cached, so no checkouts will be cached");
      return 0;
    }
    int totalCheckOuts = 0;
    for (Entry entry : mEntries) {
      totalCheckOuts += fetchCheckOutsForEntryInYear(entry.getID(), year);
    }
    mPullCheckOuts = true;
    return totalCheckOuts;
  }
  
//  public int pullCheckOutsForYearInAdvance(int year) {
//    int totalCheckOuts = 0;
//    mPullCheckOuts = false;
//    prevStorage();  totalCheckOuts += pullCheckOutsForYear(year - 2);
//    mPullCheckOuts = false;
//    nextStorage();  totalCheckOuts += pullCheckOutsForYear(year - 1);
//    mPullCheckOuts = false;
//    thisStorage();  totalCheckOuts += pullCheckOutsForYear(year);
//    return totalCheckOuts;
//  }
  
  /* Push to database */
  // --------------------------------------------
  public boolean push() {
    if (mDirtyBit) {
      Log.i(TAG, "Pushing " + mEntries.size() + " entries and supplementary checkouts...");
      for (Entry entry : mEntries) {  // old items will be ignored
        ModifiedContentBuffer.getInstance().put(entry);
        Map<Month, CheckOut[]> submap = getStorage().get(Long.valueOf(entry.getID()));
        for (int m = 1; m <= Utility.NUM_MONTHS; ++m) {
          CheckOut[] checkouts = submap.get(Utility.monthValues[m]);
          ModifiedContentBuffer.getInstance().put(Arrays.asList(checkouts));
        }
      }
      mDirtyBit = false;
      // writer thread / service will start doing its job
      return true;
    } else {
      Log.i(TAG, "Database is up to date");
    }
    return false;  // up to date
  }
  
  public void markAsDirty() {
    mDirtyBit = true;
  }
  
  public void needPullCheckoutsAgain() {
    mDirtyBit = true;
    push();  // store all unsaved changes
    clearCheckOuts();
    mPullCheckOuts = false;
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  @NonNull
  private Map<Long, Map<Month, CheckOut[]>> getStorage() {
    return mStorage;
  }
  
  @NonNull
  private Map<Month, CheckOut[]> createMonthlyMap() {
    Map<Month, CheckOut[]> map = new HashMap<Month, CheckOut[]>();
    for (int m = 1; m <= Utility.NUM_MONTHS; ++m) {
      CheckOut[] value = new CheckOut[Utility.DAYS_LIMIT];
      map.put(Utility.monthValues[m], value);
    }
    return map;
  }
  
  private int fetchCheckOutsForEntryInYear(long entryID, int year) {
    List<CheckOut> rawCheckOuts = Database.getInstance().getCheckOutsForEntryInYear(entryID, year);
    for (CheckOut checkout : rawCheckOuts) {
      checkout.setModifiedLabel(ModifiedLabel.OLD);
      CheckOut[] ref = getStorage().get(Long.valueOf(entryID)).get(checkout.getDate().getMonth());
      ref[checkout.getDate().getDay() - 1] = checkout;
    }
    return rawCheckOuts.size();
  }
  
  @SuppressWarnings("unused")
  private List<Integer> alreadyCached(final List<DatabaseStoreable> list) {
    List<Integer> indices = new ArrayList<Integer>(list.size());
    for (int i = 0; i < list.size(); ++i) {
      DatabaseStoreable item = list.get(i);
      if (item.getModifiedLabel() != ModifiedLabel.OLD) {
        indices.add(Integer.valueOf(i));
      }
    }
    return indices;
  }
  
  private void clearCheckOuts() {
    for (Entry entry : mEntries) {
      Map<Month, CheckOut[]> ref = getStorage().get(Long.valueOf(entry.getID()));
      for (int m = 1; m < Utility.NUM_MONTHS; ++m) {
        CheckOut[] checkouts = ref.get(Utility.monthValues[m]);
        Arrays.fill(checkouts, null);
      }
    }
  }
}
