package com.orcchg.checkoutapp.items;

import android.util.Log;

import com.orcchg.checkoutapp.core.Database;
import com.orcchg.checkoutapp.core.DatabaseStoreable;
import com.orcchg.checkoutapp.core.ModifiedLabel;
import com.orcchg.checkoutapp.core.Database.DatabaseException;
import com.orcchg.checkoutapp.utils.Utility;

public class CheckOut implements DatabaseStoreable {
  private static final String TAG = "CheckOut_CheckOut";
  
  private ModifiedLabel mModifiedLabel = ModifiedLabel.NEW;
  
  private final long mID;
  private final long mEntryID;
  private final Utility.DMY mDate;
  
  /* Public API */
  // --------------------------------------------------------------------------
  private CheckOut(final Builder builder) {
    mID = builder.mID;
    mEntryID = builder.mEntryID;
    mDate = builder.mDate;
  }
  
  public static class Builder {
    private final long mID;
    private final long mEntryID;
    private final Utility.DMY mDate;
    
    public Builder(long entryID, Utility.DMY date) {
      mID = Database.getInstance().getLastStoredCheckOutID();
      Database.getInstance().incrementLastStoredCheckOutID();
      mEntryID = entryID;
      mDate = date;
    }
    
    /// @note Only for private usage via Database or unit-testing!
    public Builder(long id, long entryID, Utility.DMY date) {
      mID = id;
      mEntryID = entryID;
      mDate = date;
    }
    
    public CheckOut build() {
      return new CheckOut(this);
    }
  }
  
  public long getID() { return mID; }
  public long getEntryID() { return mEntryID; }
  public Utility.DMY getDate() { return mDate; }
  
  @Override
  public String toString() {
    return new StringBuilder("CheckOut: [")
        .append(mID).append(", ")
        .append(mEntryID).append(", ")
        .append(mDate.toString()).append("]")
        .toString();
  }
  
  /* Database API */
  // --------------------------------------------------------------------------
  @Override
  public ModifiedLabel getModifiedLabel() { return mModifiedLabel; }
  @Override
  public void setModifiedLabel(final ModifiedLabel label) {
    mModifiedLabel = label;
  }
  
  @Override
  public long store() {
    long id = -1;
    if (Database.getInstance().updateCheckOut(this)) {
      Log.v(TAG, "Updated: " + toString());
      return mID;
    }
    try {
      id = Database.getInstance().insertCheckOut(this);
      Log.v(TAG, "Inserted: " + toString());
    } catch (DatabaseException e) {
      Log.e(TAG, e.getMessage());
      e.printStackTrace();
    }
    return id;
  }
  
  @Override
  public boolean delete() {
    Log.v(TAG, "Deleted: " + toString());
    return Database.getInstance().deleteCheckOut(mID, mEntryID);
  }
}
