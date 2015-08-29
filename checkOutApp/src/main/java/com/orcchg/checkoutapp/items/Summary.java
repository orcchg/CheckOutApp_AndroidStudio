package com.orcchg.checkoutapp.items;

import android.content.Context;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.utils.Utility;

public class Summary {
  private long mDate;
  private boolean mIsArchived;
  
  public Summary(long date) {
    setDate(date);
  }
  
  public String getSummary(final Context context) { return resolveDate(context); }
  public long getDate() { return mDate; }
  public String getFormattedDate() {
    if (mDate <= Utility.NEVER) {
      return "";
    }
    return Utility.dateFormat.format(mDate);
  }
  public boolean isArchived() { return mIsArchived; }
  
  public void setDate(long date) { mDate = date; }
  public void setArchived(boolean isArchived) { mIsArchived = isArchived; }
  
  private String resolveDate(final Context context) {
    if (mDate <= Utility.NEVER) {
      return context.getResources().getString(R.string.summary_date_never);
    }
    
    long today = System.currentTimeMillis();
    long difference = today - mDate;
    if (difference < 0) {
      return context.getResources().getString(R.string.summary_date_future);
    }
    difference = Math.abs(difference);
    
    if (difference < Utility.DAY_MS) {
      return context.getResources().getString(R.string.summary_date_today);
    } else if (difference < Utility.DAY_2_MS) {
      return context.getResources().getString(R.string.summary_date_yesterday);
    } else if (difference < Utility.DAY_3_MS) {
      return context.getResources().getString(R.string.summary_date_two_days_ago);
    } else if (difference < Utility.DAY_4_MS) {
      return context.getResources().getString(R.string.summary_date_three_days_ago);
    } else if (difference < Utility.DAY_5_MS) {
      return context.getResources().getString(R.string.summary_date_four_days_ago);
    } else if (difference < Utility.DAY_6_MS) {
      return context.getResources().getString(R.string.summary_date_five_days_ago);
    } else if (difference < Utility.WEEK_MS) {
      return context.getResources().getString(R.string.summary_date_six_days_ago);
    } else if (difference < Utility.WEEK_2_MS) {
      return context.getResources().getString(R.string.summary_date_week_ago);
    } else if (difference < Utility.WEEK_3_MS) {
      return context.getResources().getString(R.string.summary_date_two_weeks_ago);
    } else if (difference < Utility.MONTH_MS) {
      return context.getResources().getString(R.string.summary_date_three_weeks_ago);
    } else if (difference < Utility.MONTH_2_MS) {
      return context.getResources().getString(R.string.summary_date_month_ago);
    } else if (difference < Utility.MONTH_3_MS) {
      return context.getResources().getString(R.string.summary_date_two_months_ago);
    } else if (difference < Utility.MONTH_4_MS) {
      return context.getResources().getString(R.string.summary_date_three_months_ago);
    } else if (difference < Utility.MONTH_5_MS) {
      return context.getResources().getString(R.string.summary_date_four_months_ago);
    } else if (difference < Utility.MONTH_6_MS) {
      return context.getResources().getString(R.string.summary_date_five_months_ago);
    } else if (difference < Utility.MONTH_7_MS) {
      return context.getResources().getString(R.string.summary_date_six_months_ago);
    } else if (difference < Utility.MONTH_8_MS) {
      return context.getResources().getString(R.string.summary_date_seven_months_ago);
    } else if (difference < Utility.MONTH_9_MS) {
      return context.getResources().getString(R.string.summary_date_eight_months_ago);
    } else if (difference < Utility.MONTH_10_MS) {
      return context.getResources().getString(R.string.summary_date_nine_months_ago);
    } else if (difference < Utility.MONTH_11_MS) {
      return context.getResources().getString(R.string.summary_date_ten_months_ago);
    } else if (difference < Utility.YEAR_MS) {
      return context.getResources().getString(R.string.summary_date_eleven_months_ago);
    } else if (difference < Utility.LONG_MS) {
      return context.getResources().getString(R.string.summary_date_year_ago);
    } else {
      return context.getResources().getString(R.string.summary_date_long_ago);
    }
  }
}
