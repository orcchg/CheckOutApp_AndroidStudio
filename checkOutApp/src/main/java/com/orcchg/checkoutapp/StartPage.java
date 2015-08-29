package com.orcchg.checkoutapp;

import org.joda.time.LocalDate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.core.Database;
import com.orcchg.checkoutapp.utils.DrawUtility;

public class StartPage extends FragmentActivity {
  private static final String TAG = "CheckOut_StartPage";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    System.setProperty("org.joda.time.DateTimeZone.Provider", "com.orcchg.checkoutapp.utils.FastDateTimeZoneProvider");
    Cache.init();
    Database.init(getBaseContext());
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    DisplayMetrics outMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
    Log.i(TAG, "Display density: " + outMetrics.density + ", w = " + DrawUtility.pixelToDip(this, outMetrics.widthPixels) + ", h = " + DrawUtility.pixelToDip(this, outMetrics.heightPixels));
    Log.i(TAG, "Display pixels: w = " + outMetrics.widthPixels + ", h = " + outMetrics.heightPixels);
    
    LocalDate ymd = new LocalDate();
    Log.d(TAG, "Local time: " + ymd.toString());
    int dayOfWeek = ymd.getDayOfWeek();
    int dayOfMonth = ymd.getDayOfMonth();
    int monthOfYear = ymd.getMonthOfYear();
    int year = ymd.getYear();
    Log.d(TAG, "Day of week: " + dayOfWeek + ", Day of month: " + dayOfMonth + ", Month of year: " + monthOfYear + ", Year: " + year);

    Intent intent = new Intent(this, CheckOutPage.class);
    intent.putExtra(CheckOutPage.in_bundleKey_weekday, dayOfWeek);
    intent.putExtra(CheckOutPage.in_bundleKey_day, dayOfMonth);
    intent.putExtra(CheckOutPage.in_bundleKey_month, monthOfYear);
//    intent.putExtra(CheckOutPage.in_bundleKey_daysInMonth, ymd.dayOfMonth().getMaximumValue());
    intent.putExtra(CheckOutPage.in_bundleKey_year, year);
    startActivity(intent);
    finish();
  }
}
