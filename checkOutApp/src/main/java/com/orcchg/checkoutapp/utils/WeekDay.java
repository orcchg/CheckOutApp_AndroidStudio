package com.orcchg.checkoutapp.utils;

public enum WeekDay {
  NONE(0), MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);
  
  final private int value;
  
  WeekDay(int value) {
    if (value < 0 || value > 7) {
      throw new IllegalArgumentException("Weekday value must be between 0 and 7!");
    }
    this.value = value;
  }
  
  public int getValue() { return value; }
  
  public static WeekDay fromInt(int value) {
    if (value < 0 || value > 7) {
      throw new IllegalArgumentException("Weekday value must be between 0 and 7!");
    }
    switch (value) {
      case 0:
        return NONE;
      case 1:
        return MONDAY;
      case 2:
        return TUESDAY;
      case 3:
        return WEDNESDAY;
      case 4:
        return THURSDAY;
      case 5:
        return FRIDAY;
      case 6:
        return SATURDAY;
      case 7:
        return SUNDAY;
      default:
        return null;
    }
  }
  
  @Override
  public String toString() {
    switch (value) {
      case 0:
        return "NONE";
      case 1:
        return "MONDAY";
      case 2:
        return "TUESDAY";
      case 3:
        return "WEDNESDAY";
      case 4:
        return "THURSDAY";
      case 5:
        return "FRIDAY";
      case 6:
        return "SATURDAY";
      case 7:
        return "SUNDAY";
      default:
        return "";
    }
  }
}
