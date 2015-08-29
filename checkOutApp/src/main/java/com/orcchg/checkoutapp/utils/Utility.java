package com.orcchg.checkoutapp.utils;

import hirondelle.date4j.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;

import android.support.annotation.IntDef;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.common.base.Joiner;

public class Utility {
  private static final String TAG = "CheckOut_Utility";
  private static final String dateFormatStr = "dd.MM.yyyy";
  private static final String datetimeFormatStr = "dd.MM.yyyy HH:mm:ss";
  public static final DateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.US);
  public static final DateFormat datetimeFormat = new SimpleDateFormat(datetimeFormatStr, Locale.US);
  
  public final static Month[] monthValues = Month.values();
  public static final int NUM_MONTHS = 12;
  public static final int DAYS_LIMIT = 31;
  public static final int YEAR_LIMIT = 1970;
  
  public static final String delims = "([:space:]|[:punct:])+";
  public static final String delimForPackedBundleValue = "%";
  
  public static char infinity = '∞';
  public static int infinityValue = 2_000_000_000;
  
  public static String getCurrentDateTimePosix() {
    Date date = new Date();
    return Long.toString(date.getTime());
  }
  
  public static String getCurrentDate() {
    return dateFormat.format(new Date());
  }
  
  public static String getCurrentDateTime() {
    return datetimeFormat.format(new Date());
  }
  
  public static String parseMillis(long millis) {
    if (millis == 0) {
      return "";
    }
    return dateFormat.format(new Date(millis));
  }
  
  public static boolean equalf(float lhs, float rhs, float epsilon) {
    return Math.abs(lhs - rhs) < epsilon;
  }
  
  public static int daysInMonth(Month month, int year) {
    if (year < 0) {
      String message = "Year value must not be negative!";
      Log.e(TAG, message);
      throw new IllegalArgumentException(message);
    }
    
    boolean isBissextile = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0) ? true : false;
    switch (month) {
      case JANUARY:
      case MARCH:
      case MAY:
      case JULY:
      case AUGUST:
      case OCTOBER:
      case DECEMBER:
        return 31;
      case APRIL:
      case JUNE:
      case SEPTEMBER:
      case NOVEMBER:
        return 30;
      case FEBRUARY:
        if (isBissextile) {
          return 29;
        } else {
          return 28;
        }
      default:
        String message = "Invalid enum value for Month: " + month.toString();
        Log.e(TAG, message);
        throw new IllegalArgumentException(message);
    }
  }
  
  public static WeekDay getWeekDay(int dayInMonth, Month month, int year) {
    DateTime dt = new DateTime(year, month.getValue(), dayInMonth, 6, 0, 0, 0);
    int day = dt.getWeekDay();
    switch (day) {
      case 1:  return WeekDay.SUNDAY;
      case 2:  return WeekDay.MONDAY;
      case 3:  return WeekDay.TUESDAY;
      case 4:  return WeekDay.WEDNESDAY;
      case 5:  return WeekDay.THURSDAY;
      case 6:  return WeekDay.FRIDAY;
      case 7:  return WeekDay.SATURDAY;
      default:
        String message = "Logic error during calculating week day! Date4j has an error!";
        Log.e(TAG, message);
        throw new RuntimeException(message);
    }
  }
  
  /* Serialize API */
  // --------------------------------------------------------------------------
  public static String dateCodeToString(int code) {
    if (code <= 0) {
      String message = "Value must be bigger than 0";
      Log.e(TAG, message);
      throw new IllegalArgumentException(message);
    }
    String zero = "0";
    if (code <= 9) {
      return zero + Integer.toString(code);
    }
    return Integer.toString(code);
  }
  
  public static class DMY implements Comparable<DMY> {
    private final int day;
    private final Month month;
    private final int year;

    public int getDay() { return day; }
    public Month getMonth() { return month; }
    public int getYear() { return year; }
    
    public DMY(int day, int month, int year) {
      this.day = day;
      this.month = Month.fromInt(month);
      this.year = year;
    }
    
    public DMY(int day, final Month month, int year) {
      this.day = day;
      this.month = month;
      this.year = year;
    }
    
    public static DMY valueOf(int day, int month, int year) {
      return new DMY(day, month, year);
    }
    
    public static DMY valueOf(int day, final Month month, int year) {
      return new DMY(day, month, year);
    }
    
    public static DMY valueOf(final Date date) {
      return DMY.parse(date.getTime());
    }
    
    public static DMY valueOf(final DMY dmy) {
      return new DMY(dmy.day, dmy.month, dmy.year);
    }
    
    public static DMY parse(long milliseconds) {
      TimeZone tz = TimeZone.getDefault();
      DateTime dt = DateTime.forInstant(milliseconds, tz);
      return new DMY(dt.getDay(), dt.getMonth(), dt.getYear());
    }
    
    @Override
    public String toString() {
      return new StringBuilder("DMY[")
          .append(day).append(", ")
          .append(month.toString()).append(", ")
          .append(year).append("]")
          .toString();
    }
    
    public long toLong() {
      TimeZone tz = TimeZone.getDefault();
      DateTime dt = new DateTime(year, month.getValue(), day, 6, 0, 0, 0);
      return dt.getMilliseconds(tz);
    }
    
    @SuppressWarnings("deprecation")
    public Date toDate() {
      return new Date(year - 1900, month.getValue() - 1, day);
    }
    
    @Override
    public int compareTo(DMY another) {
      if (year < another.year) {
        return -1;
      } else if (year > another.year) {
        return 1;
      } else {  // year == another.year
        int month_compare = month.compareTo(another.month);
        if (month_compare < 0) {
          return -2;
        } else if (month_compare > 0) {
          return 2;
        } else {  // month == another.month
          if (day < another.day) {
            return -3;
          } else if (day > another.day) {
            return 3;
          } else {
            return 0;
          }
        }
      }
    }
    
    public static enum DMYCompare {
      UNKNOWN, EQUAL, DAY_LESS, DAY_GREATER, MONTH_LESS, MONTH_GREATER, YEAR_LESS, YEAR_GREATER;
    }
    
    public static DMYCompare compare(DMY lhs, DMY rhs) {
      Log.v(TAG, "Compare dates: " + lhs.toString() + " and " + rhs.toString());
      int compare_result = lhs.compareTo(rhs);
      switch (compare_result) {
        case -1: return DMYCompare.YEAR_LESS;
        case 1:  return DMYCompare.YEAR_GREATER;
        case -2: return DMYCompare.MONTH_LESS;
        case 2:  return DMYCompare.MONTH_GREATER;
        case -3: return DMYCompare.DAY_LESS;
        case 3:  return DMYCompare.DAY_GREATER;
        case 0:  return DMYCompare.EQUAL;
        default: return DMYCompare.UNKNOWN;
      }
    }
    
    public static int compareDates(final String first_date, final String second_date) throws ParseException {
      Date first = Utility.dateFormat.parse(first_date);
      Date second = Utility.dateFormat.parse(second_date);
      return first.compareTo(second);
    }
    
    public static DMY parseDate(final String date) {
      if (date.isEmpty()) {
        return null;
      }
      String[] tokens = date.split(delims);
      DMY dmy = new DMY(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
      return dmy;
    }
  }  // class DMY
  
  public static String[] serializeMonths(final EnumSet<Month> months) {
    String[] serialized = new String[months.size()];
    int i = 0;
    for (Month month : months) {
      serialized[i] = month.serialize();
      ++i;
    }
    return serialized;
  }
  
  public static String[] serializeInts(int[] ints) {
    String[] serialized = new String[ints.length];
    for (int i = 0; i < ints.length; ++i) {
      serialized[i] = Integer.toString(ints[i]);
    }
    return serialized;
  }
  
  public static String[] serializeLongs(long[] longs) {
    String[] serialized = new String[longs.length];
    for (int i = 0; i < longs.length; ++i) {
      serialized[i] = Long.toString(longs[i]);
    }
    return serialized;
  }
  
  public static <T> String listToString(final List<T> list) {
    return Joiner.on("|").join(list);
  }
  
  /* Constants */
  // --------------------------------------------------------------------------
  public static final long NEVER = 0l;
  public static final long DAY_MS      = 86_400_000l;
  public static final long DAY_2_MS    = 172_800_000l;
  public static final long DAY_3_MS    = 259_200_000l;
  public static final long DAY_4_MS    = 345_600_000l;
  public static final long DAY_5_MS    = 432_000_000l;
  public static final long DAY_6_MS    = 518_400_000l;
  public static final long WEEK_MS     = 604_800_000l;
  public static final long WEEK_2_MS   = 1_209_600_000l;
  public static final long WEEK_3_MS   = 1_814_400_000l;
  public static final long MONTH_MS    = 2_419_200_000l;
  public static final long MONTH_2_MS  = 4_838_400_000l;
  public static final long MONTH_3_MS  = 7_257_600_000l;
  public static final long MONTH_4_MS  = 9_676_800_000l;
  public static final long MONTH_5_MS  = 12_096_000_000l;
  public static final long MONTH_6_MS  = 14_515_200_000l;
  public static final long MONTH_7_MS  = 16_934_400_000l;
  public static final long MONTH_8_MS  = 19_353_600_000l;
  public static final long MONTH_9_MS  = 21_772_800_000l;
  public static final long MONTH_10_MS = 24_192_000_000l;
  public static final long MONTH_11_MS = 26_611_200_000l;
  public static final long YEAR_MS     = 29_030_400_000l;
  public static final long LONG_MS     = 43_545_600_000l;
  
  /* URL API */
  // --------------------------------------------------------------------------
  public static final int RESOURCE_URL_UNKNOWN = -1;
  public static final int RESOURCE_URL_IMAGE = 0;
  public static final int RESOURCE_URL_FILE = 1;
  public static final int RESOURCE_URL_MAP = 2;
  public static final int RESOURCE_URL_VIDEO = 3;
  public static final int RESOURCE_URL_WEB = 4;
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
    RESOURCE_URL_UNKNOWN,
    RESOURCE_URL_IMAGE,
    RESOURCE_URL_FILE,
    RESOURCE_URL_MAP,
    RESOURCE_URL_VIDEO,
    RESOURCE_URL_WEB
  })
  public @interface ResourceURLType {}
  
  @ResourceURLType
  public static int resolveResourceURLType(final String url) {
    if (url.isEmpty()) { return RESOURCE_URL_UNKNOWN; }
    if (url.startsWith("http://")) { return RESOURCE_URL_WEB; }
    if (url.startsWith("geo:")) { return RESOURCE_URL_MAP; }
    String mimeType = getMimeType(url);
    Log.d(TAG, "Resource [" + url + "] has MIME type: " + mimeType);
    if (mimeType != null && mimeType.startsWith("image")) { return RESOURCE_URL_IMAGE; }
    if (mimeType != null && mimeType.startsWith("video")) { return RESOURCE_URL_VIDEO; }
    return RESOURCE_URL_FILE;
  }
  
  public static String getMimeType(String url) {
    String type = null;
//    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    String extension = FilenameUtils.getExtension(url);
    if (extension != null) {
      type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    Log.d(TAG, "Extension: " + extension + ", type: " + type);
    return type;
  }
}
