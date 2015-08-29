package com.orcchg.checkoutapp.items;

import android.util.Log;

import com.orcchg.checkoutapp.core.Database;
import com.orcchg.checkoutapp.core.Database.DatabaseException;
import com.orcchg.checkoutapp.core.DatabaseStoreable;
import com.orcchg.checkoutapp.core.ModifiedLabel;
import com.orcchg.checkoutapp.utils.Utility;

public class Entry implements DatabaseStoreable {
  private static final String TAG = "CheckOut_Entry";
  
  private ModifiedLabel mModifiedLabel = ModifiedLabel.NEW;
  
  private final long mID;
  private final String mTableName;
  private String mName;
  private int mLineIndex;
  private final long mStartDate;
  private long mLastCheckDate;
  private String mResourceURL;
  private boolean mIsArchived;
  private int mSupplementaryValue;
  
  /* Public API */
  // --------------------------------------------------------------------------
  private Entry(final Builder builder) {
    mID = builder.mID;
    mTableName= builder.mTableName;
    mName = builder.mName;
    mLineIndex = builder.mLineIndex;
    mStartDate = builder.mStartDate;
    mLastCheckDate = builder.mLastCheckDate;
    mResourceURL = builder.mResourceURL;
    mIsArchived = builder.mIsArchived;
    mSupplementaryValue = builder.mSupplementaryValue;
  }
  
  public static class Builder {
    private final long mID;
    private final String mTableName;
    private String mName = "";
    private int mLineIndex = 0;
    private final long mStartDate;
    private long mLastCheckDate = Utility.NEVER;
    private String mResourceURL = "";
    private boolean mIsArchived = false;
    private int mSupplementaryValue = 0;
    
    public Builder() {
      mID = Database.getInstance().getLastStoredEntryID();
      Database.getInstance().incrementLastStoredEntryID();
      mTableName = Database.getInstance().generateCheckOutsForEntryTable(mID);
      mStartDate = System.currentTimeMillis();
    }
    
    /// @note Only for private usage via Database or unit-testing!
    public Builder(long id, final String tableName, long startDate) {
      mID = id;
      mTableName = tableName;
      mStartDate = startDate;
    }
    
    public Builder setName(final String name) {
      mName = name;
      return this;
    }
    
    public Builder setLineIndex(int lineIndex) {
      mLineIndex = lineIndex;
      return this;
    }
    
    public Builder setLastCheckDate(long date) {
      mLastCheckDate = date;
      return this;
    }
    
    public Builder setResourceURL(final String url) {
      mResourceURL = url;
      return this;
    }
    
    public Builder setArchived(boolean isArchived) {
      mIsArchived = isArchived;
      return this;
    }
    
    public Builder setSupplementaryValue(int value) {
      mSupplementaryValue = value;
      return this;
    }
    
    public Entry build() {
      return new Entry(this);
    }
  }
  
  public long getID() { return mID; }
  public String getTableName() { return mTableName; }
  public String getName() { return mName; }
  public int getLineIndex() { return mLineIndex; }
  public long getStartDate() { return mStartDate; }
  public long getLastCheckDate() { return mLastCheckDate; }
  public String getResourceURL() { return mResourceURL; }
  public boolean isArchived() { return mIsArchived; }
  public int getSupplementaryValue() { return mSupplementaryValue; }
  
  public void setName(final String name) { mName = name; }
  public void setLineIndex(int index) { mLineIndex = index; }
  public void setLastCheckDate(long date) { mLastCheckDate = date; }
  public void setResourceURL(final String url) { mResourceURL = url; }
  public void setArchived(boolean isArchived) { mIsArchived = isArchived; }
  public void setSupplementaryValue(int value) { mSupplementaryValue = value; }
  public void incrementSupplementaryValue() { ++mSupplementaryValue; }
  public void decrementSupplementaryValue() { --mSupplementaryValue; }
  
  @Override
  public String toString() {
    return new StringBuilder("Entry: [")
        .append(mID).append(", ")
        .append(mTableName).append(", ")
        .append(mName).append(", ")
        .append(mLineIndex).append(", ")
        .append(mStartDate).append(", ")
        .append(mLastCheckDate).append(", ")
        .append(mResourceURL).append(", ")
        .append(mIsArchived).append(", ")
        .append(mSupplementaryValue).append("]")
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
    if (Database.getInstance().updateEntry(this)) {
      Log.v(TAG, "Updated: " + toString());
      return mID;
    }
    try {
      id = Database.getInstance().insertEntry(this);
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
    return Database.getInstance().deleteEntry(mID);
  }
}
