package com.orcchg.checkoutapp.utils;

import android.content.Context;

import com.orcchg.checkoutapp.R;

public enum Month {
  NONE(0), JANUARY(1), FEBRUARY(2), MARCH(3), APRIL(4), MAY(5), JUNE(6), JULY(7), AUGUST(8),
  SEPTEMBER(9), OCTOBER(10), NOVEMBER(11), DECEMBER(12);
  
  final private int value;
  
  Month(int value) {
    if (value < 0 || value > 12) {
      throw new IllegalArgumentException("Month value must be between 0 and 12!");
    }
    this.value = value;
  }
  
  public int getValue() { return value; }
  public Month getPrevious() {
    if (value <= 1) { return JANUARY; }
    return Month.fromInt(value - 1);
  }
  public Month getPreviousNone() {
    if (value <= 0) { return NONE; }
    return Month.fromInt(value - 1);
  }
  public Month getNext() {
    if (value >= 12) { return DECEMBER; }
    return Month.fromInt(value + 1);
  }
  public Month getNextNone() {
    if (value > 12) { return NONE; }
    return Month.fromInt(value + 1);
  }
  
  public static Month fromInt(int value) {
    if (value < 0 || value > 12) {
      throw new IllegalArgumentException("Month value must be between 0 and 12!");
    }
    switch (value) {
      case 0:
        return NONE;
      case 1:
        return JANUARY;
      case 2:
        return FEBRUARY;
      case 3:
        return MARCH;
      case 4:
        return APRIL;
      case 5:
        return MAY;
      case 6:
        return JUNE;
      case 7:
        return JULY;
      case 8:
        return AUGUST;
      case 9:
        return SEPTEMBER;
      case 10:
        return OCTOBER;
      case 11:
        return NOVEMBER;
      case 12:
        return DECEMBER;
      default:
        return null;
    }
  }
  
  public char toLetter() {
    switch (value) {
      case 0:
        return '-';
      case 1:
        return 'J';
      case 2:
        return 'F';
      case 3:
        return 'M';
      case 4:
        return 'A';
      case 5:
        return 'M';
      case 6:
        return 'J';
      case 7:
        return 'J';
      case 8:
        return 'A';
      case 9:
        return 'S';
      case 10:
        return 'O';
      case 11:
        return 'N';
      case 12:
        return 'D';
      default:
        return ' ';
    }
  }
  
  @Override
  public String toString() {
    switch (value) {
      case 0:
        return "NONE";
      case 1:
        return "JANUARY";
      case 2:
        return "FEBRUARY";
      case 3:
        return "MARCH";
      case 4:
        return "APRIL";
      case 5:
        return "MAY";
      case 6:
        return "JUNE";
      case 7:
        return "JULY";
      case 8:
        return "AUGUST";
      case 9:
        return "SEPTEMBER";
      case 10:
        return "OCTOBER";
      case 11:
        return "NOVEMBER";
      case 12:
        return "DECEMBER";
      default:
        return "";
    }
  }
  
  public String serialize() {
    return Integer.toString(value);
  }
  
  public String toString(Context context) {
    if (value < 1) {
      return "";
    }
    return context.getResources().getStringArray(R.array.util_months)[value - 1];
  }
}
