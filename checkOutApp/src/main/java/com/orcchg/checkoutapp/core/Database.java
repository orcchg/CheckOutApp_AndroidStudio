package com.orcchg.checkoutapp.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.orcchg.checkoutapp.items.CheckOut;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.Month;
import com.orcchg.checkoutapp.utils.Utility;

public class Database {
  private static final String TAG = "CheckOut_Database";
  
  private static Database INSTANCE;
  
  public static void init(final Context context) {
    INSTANCE = new Database(context);
  }
  
  public static Database getInstance() {
    return INSTANCE;
  }
  
  // Database handlers
  private static final String databaseName = "CheckOutAppDatabase.db";
  private SQLiteDatabase mDbHandler;
  
  // Entries table
  private static final String EntriesTableName = "EntriesTable";
  private static final String CREATE_ENTRIES_TABLE_STMT =
      "CREATE TABLE IF NOT EXISTS " + EntriesTableName + "(" +
      "'ID' INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 0, " +
      "'TableName' TEXT DEFAULT \"\", " +
      "'Name' TEXT DEFAULT \"\", " +
      "'LineIndex' INTEGER DEFAULT 0, " +
      "'StartDate' INTEGER DEFAULT 0, " +
      "'LastCheckDate' INTEGER DEFAULT 0, " +
      "'ResourceURL' TEXT DEFAULT \"\", " +
      "'IsArchived' INTEGER DEFAULT 0, " +
      "'SupplementaryValue' INTEGER DEFAULT 0);";

  static final int entryID_columnIndex = 0;
  static final int entryTableName_columnIndex = 1;
  static final int entryName_columnIndex = 2;
  static final int entryLineIndex_columnIndex = 3;
  static final int entryStartDate_columnIndex = 4;
  static final int entryLastCheckDate_columnIndex = 5;
  static final int entryResourceURL_columnIndex = 6;
  static final int entryIsArchived_columnIndex = 7;
  static final int entrySupplementaryValue_columnIndex = 8;
  
  private long lastStoredEntryID = 0;
  
  // CheckOut table
  private static final String CheckOutTableNamePrefix = "CheckOutTable_";
  static final int checkoutID_columnIndex = 0;
  static final int checkoutEntryID_columnIndex = 1;
  static final int checkoutDate_columnIndex = 2;
  
  private long lastStoredCheckOutID = 0;
  
  // Last stored checkout ID
  private static final String LastStoredCheckOutIDTableName = "LastStoredCheckOutIDTable";
  private static final String CREATE_LAST_STORED_CHECKOUT_ID_TABLE_STMT =
      "CREATE TABLE IF NOT EXISTS " + LastStoredCheckOutIDTableName + "(" +
      "'LastID' INTEGER DEFAULT 0);";
  
  private Database(final Context context) {
    open(context);
    mDbHandler.execSQL(CREATE_ENTRIES_TABLE_STMT);
    mDbHandler.execSQL(CREATE_LAST_STORED_CHECKOUT_ID_TABLE_STMT);
    prepareLastIDs();
  }
  
  /* Exceptions */
  // --------------------------------------------------------------------------
  @SuppressWarnings("serial")
  public static class NoPlaceholdersException extends Exception {
    public NoPlaceholdersException(String message) {
      super(message);
    }
  }
  
  @SuppressWarnings("serial")
  public static class DatabaseException extends Exception {
    public DatabaseException(String message) {
      super(message);
    }
  }
  
  /* Public API */
  // --------------------------------------------------------------------------
  public static String getDatabaseName() { return databaseName; }
  public static String getEntriesTableName() { return EntriesTableName; }
  
  public long getLastStoredEntryID() { return lastStoredEntryID; }
  public void incrementLastStoredEntryID() { ++lastStoredEntryID; }
  
  public long getLastStoredCheckOutID() { return lastStoredCheckOutID; }
  public void incrementLastStoredCheckOutID() { ++lastStoredCheckOutID; }
  
  public void open(final Context context) {
    Log.i(TAG, "Opening database...");
    mDbHandler = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
    Log.i(TAG, "Database has been opened");
  }
  public void close() {
    Log.i(TAG, "Closing database...");
    mDbHandler.close();
    Log.i(TAG, "Database has been closed");
  }
  
  /* Get entities API */
  // --------------------------------------------------------------------------
  /* Entry */
  // --------------------------------------------
  public Entry getEntry(long id) { return fetchDataById(EntriesTableName, id, new EntryFetcher()); }
  public List<Entry> getAllEntries() { return fetchAllDataWithLimit(EntriesTableName, -1, new EntryFetcher()); }
  public long[] getAllEntriesIds() { return fetchAllIdsWithLimit(EntriesTableName, -1); }
  
  public String generateCheckOutsForEntryTable(long id) {
    String tableName = getCheckOutsForEntryTableName(id);
    String CREATE_CHECKOUT_TABLE_STMT =
        "CREATE TABLE IF NOT EXISTS " + tableName + "(" +
        "'ID' INTEGER PRIMARY KEY AUTOINCREMENT DEFAULT 0, " +
        "'EntryID' INTEGER DEFAULT 0, " +
        "'Date' INTEGER DEFAULT 0);";
    mDbHandler.execSQL(CREATE_CHECKOUT_TABLE_STMT);
    return tableName;
  }
  
  public String getCheckOutsForEntryTableName(long id) {
    return new StringBuilder(CheckOutTableNamePrefix).append(id).toString();
  }
  
  public String fetchCheckOutsForEntryTableName(long id) {
    String statement = "SELECT TableName FROM '" + EntriesTableName + ";";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    if (cursor.moveToNext()) {
      String tableName = cursor.getString(0);
      Log.d(TAG, "Entry with ID[" + id + "] has TableName: " + tableName);
      cursor.close();
      return tableName;
    } else {
      Log.w(TAG, "Entry with ID[" + id + "] has no TableName!");
      cursor.close();
      return null;
    }
  }
  
  public Cursor fetchEntriesRowids() { return fetchRowidsFromTable(EntriesTableName); }
  public Cursor fetchEntries() { return fetchDataFromTable(EntriesTableName); }
  public Cursor fetchEntryById(long rowid) { return fetchDataFromTableByRowid(EntriesTableName, rowid); }
  public Cursor fetchEntriesWithRowids(final long[] rowids) { return fetchDataFromTableWithRowids(EntriesTableName, rowids); }
  
  /* CheckOut */
  // --------------------------------------------------------------------------
  public List<CheckOut> getCheckOutsForEntryInYear(long entryID, int year) {
    String table = getCheckOutsForEntryTableName(entryID);
    String statement = new StringBuilder("SELECT * FROM '")
        .append(table)
        .append("' WHERE Date >= ? AND Date <= ? ORDER BY Date ASC;")
        .toString();
    
    String january = Long.toString(Utility.DMY.valueOf(1, Month.JANUARY, year).toLong());
    String december = Long.toString(Utility.DMY.valueOf(31, Month.DECEMBER, year).toLong());
    Log.d(TAG, "Fetching checkouts for entry with ID[" + entryID + "] in year " + year + " from " + january + " to " + december);
    
    List<CheckOut> list = new ArrayList<CheckOut>();
    Cursor cursor = mDbHandler.rawQuery(statement, new String[] {january, december});
    while (cursor.moveToNext()) {
      CheckOut checkout = fetchSingleCheckOutDataFromCursor(cursor);
      list.add(checkout);
    }
    cursor.close();
    return list;
  }
  
  /* Change content API */
  // --------------------------------------------------------------------------
  public long insertEntry(Entry entry) throws DatabaseException {
    ContentValues values = prepareEntry(entry);
    values.put("ID", entry.getID());
    return insert(EntriesTableName, values);
  }
  
  public boolean updateEntry(Entry entry) {
    return update(EntriesTableName, entry.getID(), prepareEntry(entry));
  }
  
  public long insertCheckOut(CheckOut checkout) throws DatabaseException {
    ContentValues values = prepareCheckOut(checkout);
    values.put("ID", checkout.getID());
    return insert(getCheckOutsForEntryTableName(checkout.getEntryID()), values);
  }
  
  public boolean updateCheckOut(CheckOut checkout) {
    return update(getCheckOutsForEntryTableName(checkout.getEntryID()), checkout.getID(), prepareCheckOut(checkout));
  }
  
  public boolean deleteEntry(long id) { return delete(EntriesTableName, id); }
  public boolean deleteCheckOut(long id, long entryID) { return delete(getCheckOutsForEntryTableName(entryID), id); }
  public boolean clearEntriesTable() { lastStoredEntryID = 0; return clearTable(EntriesTableName); }
  public boolean clearCheckTableForEntry(long entryID) { return clearTable(getCheckOutsForEntryTableName(entryID)); }
  public long totalEntries() { return totalRows(EntriesTableName); }
  public long totalCheckOutsForEntry(long entryID) { return totalRows(getCheckOutsForEntryTableName(entryID)); }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private void prepareLastIDs() {
    lastStoredEntryID = getLastID(EntriesTableName);
    lastStoredCheckOutID = initializeLastStoredCheckoutIDwithZeroIfTableIsEmpty();
    incrementLastStoredEntryID();
    incrementLastStoredCheckOutIDAndStore();
    Log.d(TAG, "Last stored IDs after preparation: Entry[" + lastStoredEntryID + "]");
  }
  
  private int getLastID(final String table_name) {
    String statement = "SELECT * FROM '" + table_name + "'";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    if (cursor.moveToLast()) {
      int id_column_index = cursor.getColumnIndexOrThrow("ID");
      int last_id = cursor.getInt(id_column_index);
      Log.d(TAG, "Read last ID: " + last_id + ", from Table: " + table_name);
      cursor.close();
      return last_id;
    } else {
      Log.d(TAG, "Table " + table_name + " in database " + databaseName + " is empty! Assigning 0 to last ID");
      cursor.close();
      return 0;
    }
  }
  
  private static final int LAST_STORED_CHECKOUT_ID_NOT_INITIALIZED = -1;
  
  private int _getLastStoredCheckOutID() {
    String statement = "SELECT LastID FROM '" + LastStoredCheckOutIDTableName + "';";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    if (cursor.moveToNext()) {
      int id = cursor.getInt(0);
      Log.v(TAG, "Got last stored checkout ID: " + id);
      cursor.close();
      return id;
    } else {
      Log.d(TAG, "Table " + LastStoredCheckOutIDTableName + " is empty. Default last ID is 0");
      cursor.close();
      return LAST_STORED_CHECKOUT_ID_NOT_INITIALIZED;
    }
  }
  
  private void incrementLastStoredCheckOutIDAndStore() {
    ++lastStoredCheckOutID;
    updateLastStoredCheckOutID();
  }
  
  void updateLastStoredCheckOutID() {
    String statement = "UPDATE OR REPLACE '" + LastStoredCheckOutIDTableName + "' SET LastID = '" + lastStoredCheckOutID + "';";
    mDbHandler.execSQL(statement);
  }
  
  private int initializeLastStoredCheckoutIDwithZeroIfTableIsEmpty() {
    int id = _getLastStoredCheckOutID();
    if (id == LAST_STORED_CHECKOUT_ID_NOT_INITIALIZED) {
      Log.d(TAG, "Last stored checkout ID initialization to zero");
      String statement = "INSERT OR REPLACE INTO '" + LastStoredCheckOutIDTableName + "' (LastID) VALUES (0);";
      mDbHandler.execSQL(statement);
      return 0;
    }
    return id;
  }
  
  /* Entry */
  // --------------------------------------------
  private Entry fetchSingleEntryDataFromCursor(Cursor cursor) {
    long id = cursor.getLong(entryID_columnIndex);
    String tableName = cursor.getString(entryTableName_columnIndex);
    String name = cursor.getString(entryName_columnIndex);
    int lineIndex = cursor.getInt(entryLineIndex_columnIndex);
    long startDate = cursor.getLong(entryStartDate_columnIndex);
    long lastCheckDate = cursor.getLong(entryLastCheckDate_columnIndex);
    String resourceURL = cursor.getString(entryResourceURL_columnIndex);
    boolean isArchived = cursor.getInt(entryIsArchived_columnIndex) == 0 ? false : true;
    int supplementaryValue = cursor.getInt(entrySupplementaryValue_columnIndex);
    
    Entry entry = new Entry.Builder(id, tableName, startDate)
        .setName(name).setLineIndex(lineIndex).setLastCheckDate(lastCheckDate)
        .setResourceURL(resourceURL).setArchived(isArchived)
        .setSupplementaryValue(supplementaryValue)
        .build();
    
    Log.v(TAG, "Read data: " + entry.toString());
    return entry;
  }
  
  private ContentValues prepareEntry(Entry entry) {  // without ID
    ContentValues values = new ContentValues();
    values.put("TableName", entry.getTableName());
    values.put("Name", entry.getName());
    values.put("LineIndex", entry.getLineIndex());
    values.put("StartDate", entry.getStartDate());
    values.put("LastCheckDate", entry.getLastCheckDate());
    values.put("ResourceURL", entry.getResourceURL());
    values.put("IsArchived", entry.isArchived());
    values.put("SupplementaryValue", entry.getSupplementaryValue());
    return values;
  }
  
  /* CheckOut */
  // --------------------------------------------
  private CheckOut fetchSingleCheckOutDataFromCursor(Cursor cursor) {
    long id = cursor.getLong(checkoutID_columnIndex);
    long entryID = cursor.getLong(checkoutEntryID_columnIndex);
    long date = cursor.getLong(checkoutDate_columnIndex);
    
    CheckOut checkout = new CheckOut.Builder(id, entryID, Utility.DMY.parse(date)).build();
    Log.v(TAG, "Read data: " + checkout.toString());
    return checkout;
  }
  
  private ContentValues prepareCheckOut(CheckOut checkout) {  // without ID
    ContentValues values = new ContentValues();
    values.put("EntryID", checkout.getEntryID());
    values.put("Date", checkout.getDate().toLong());
    return values;
  }
  
  /* Internal Database methods */
  // --------------------------------------------
  private long insert(final String table_name, ContentValues values) throws DatabaseException {
    Log.d(TAG, "Insert: table name[" + table_name + "], values[" + values + "]");
    long rowid = -1;
    try {
      rowid = mDbHandler.insertOrThrow(table_name, null, values);
    } catch (SQLException e) {
      String message = "Failed to insert into table " + table_name + ", error: " + e.getMessage();
      Log.e(TAG, message);
      throw new DatabaseException(message);
    }
    return rowid;
  }
  
  private boolean update(final String table_name, final long id, ContentValues values) {
    Log.d(TAG, "Update: table name[" + table_name + "], row id[" + id + "], values[" + values + "]");
    int affected_number = mDbHandler.update(table_name, values, "ID = '" + id + "'", null);
    if (affected_number == 0) {
      Log.d(TAG, "Nothing to be updated.");
      return false;
    } else if (affected_number > 1) {
      String message = "More than one rows have been affected with update. This is invalid behavior.";
      Log.e(TAG, message);
      throw new RuntimeException(message);
    }
    return true;
  }
  
  private boolean delete(final String table_name, final long id) {
    Log.d(TAG, "Delete: table name[" + table_name + "], row id[" + id + "]");
    int affected_number = mDbHandler.delete(table_name, "ID = '" + id + "'", null);
    if (affected_number == 0) {
      Log.d(TAG, "No rows were deleted.");
      return false;
    }
    return true;
  }
  
  private boolean clearTable(final String table_name) {
    Log.d(TAG, "Clear: table name[" + table_name + "]");
    int affected_number = mDbHandler.delete(table_name, "1", null);
    if (affected_number == 0) {
      Log.d(TAG, "No rows were deleted.");
      return false;
    }
    return true;
  }
  
  private long totalRows(final String table_name) {
    Log.d(TAG, "Total rows: table name[" + table_name + "]");
    String statement = "SELECT COUNT(*) FROM '" + table_name + "'";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    if (cursor.moveToFirst()) {
      long rows_total = cursor.getLong(0);
      Log.d(TAG, "Number of rows in table " + table_name + " is " + rows_total);
      return rows_total;
    } else {
      String message = "Table " + table_name + " in database " + databaseName + " is empty!";
      Log.e(TAG, message);
      return 0L;
    }
  }
  
  private String makePlaceholders(int len) throws NoPlaceholdersException {
    if (len < 1) {
      throw new NoPlaceholdersException("No placeholders");
    } else {
      StringBuilder sb = new StringBuilder(len * 2 - 1);
      sb.append("?");
      for (int i = 1; i < len; i++) {
        sb.append(",?");
      }
      return sb.toString();
    }
  }
  
  /* Fetch private API */
  // --------------------------------------------------------------------------
  private enum IDtype { NONE, ROWID, ID, TABINDEX }
  
  private interface Fetcher<T> {
    public T fetchDataFromCursor(Cursor cursor);
  }
  
  private class EntryFetcher implements Fetcher<Entry> {
    @Override
    public Entry fetchDataFromCursor(Cursor cursor) {
      return fetchSingleEntryDataFromCursor(cursor);
    }
  }
  
  private class CheckOutFetcher implements Fetcher<CheckOut> {
    @Override
    public CheckOut fetchDataFromCursor(Cursor cursor) {
      return fetchSingleCheckOutDataFromCursor(cursor);
    }
  }
  
  private <T, F extends Fetcher<T>> T fetchDataById(
      final String table_name,
      long id,
      F f) {
    List<T> list = fetchDataCompoundStatement(table_name, IDtype.ID, new long[]{id}, null, null, null, f);
    if (list.isEmpty()) {
      return null;  // no such person with specified id
    }
    return list.get(0);
  }
  
  private long[] fetchAllIdsWithLimit(final String table_name, long limit) {
    if (limit < 0) {
      Log.d(TAG, "Limit is negative - all entries will be selected");
    }
    
    String statement = "SELECT ID FROM '" + table_name + "' LIMIT '" + limit + "';";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    long[] ids = new long[cursor.getCount()];
    int index = 0;
    while (cursor.moveToNext()) {
      ids[index++] = cursor.getLong(0);
    }
    cursor.close();
    return ids;
  }
  
  private <T, F extends Fetcher<T>> List<T> fetchAllDataWithLimit(final String table_name, long limit, F f) {
    if (limit < 0) {
      Log.d(TAG, "Limit is negative - all entries will be selected");
    }
    
    String statement = "SELECT * FROM '" + table_name + "' LIMIT '" + limit + "';";
    Cursor cursor = mDbHandler.rawQuery(statement, null);
    List<T> list =  fetchAllDataFromCursor(cursor, f);
    cursor.close();
    return list;
  }
  
  private <T, F extends Fetcher<T>> List<T> fetchAllDataFromCursor(Cursor cursor, F f) {
    List<T> list = new ArrayList<T>(cursor.getCount());
    while (cursor.moveToNext()) {
      T entry = f.fetchDataFromCursor(cursor);
      list.add(entry);
    }
    return list;
  }
  
  private <T, F extends Fetcher<T>> List<T> fetchDataCompoundStatement(
      final String table_name,
      final IDtype idType,
      final long[] ids,
      final int[] days,
      final EnumSet<Month> months,
      final int[] years,
      F f) {
    
    try {
      StringBuilder statement = new StringBuilder("SELECT * FROM '").append(table_name).append("' ");
      CompoundStatement result = compoundWhereSuffix(statement, idType, ids, days, months, years);
      
      Cursor cursor = mDbHandler.rawQuery(result.statement, result.args);
      return fetchAllDataFromCursor(cursor, f);
      
    } catch (NoPlaceholdersException e) {
      Log.d(TAG, "No placeholders provided, resulting list is empty");
      return new ArrayList<T>();
    }
  }
  
  private Cursor fetchRowidsCompoundStatement(
      final String table_name,
      final IDtype idType,
      final long[] ids,
      final int[] days,
      final EnumSet<Month> months,
      final int[] years) {
    
    try {
      StringBuilder statement = new StringBuilder("SELECT rowid FROM '").append(table_name).append("' ");
      CompoundStatement result = compoundWhereSuffix(statement, idType, ids, days, months, years);
      
      return mDbHandler.rawQuery(result.statement, result.args);
      
    } catch (NoPlaceholdersException e) {
      Log.d(TAG, "No placeholders provided, resulting cursor is null");
      return null;
    }
  }
  
  private static class CompoundStatement {
    final String statement;
    final String[] args;
    
    CompoundStatement(String statement, String[] args) {
      this.statement = statement;
      this.args = args;
    }
    
    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("Compound statement: ");
      builder.append(statement).append("  Args: ").append(Arrays.toString(args));
      return builder.toString();
    }
  }
  
  private CompoundStatement compoundWhereSuffix(
      StringBuilder statement,
      final IDtype idType,
      final long[] ids,
      final int[] days,
      final EnumSet<Month> months,
      final int[] years) throws NoPlaceholdersException {
    
    boolean whereAppended = false;
    boolean hasIDs = true;
    boolean hasDays = true;
    boolean hasMonths = true;
    
    String idTypeColumn = "";
    switch (idType) {
      case ROWID:
        idTypeColumn = "rowid";
        break;
      case ID:
        idTypeColumn = "ID";
        break;
      case TABINDEX:
        idTypeColumn = "TabIndex";
        break;
      default:
        break;
    }
    
    String[] args = ArrayUtils.EMPTY_STRING_ARRAY;
    
    if (idType == IDtype.NONE || ids == null || ids.length == 0) {
      // skip person ids
      hasIDs = false;
    } else if (ids.length == 1) {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      statement.append(idTypeColumn).append(" = '").append(ids[0]).append("' ");
    } else {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      statement.append(idTypeColumn).append(" IN (").append(makePlaceholders(ids.length)).append(") ");
      args = ArrayUtils.addAll(args, Utility.serializeLongs(ids));
      Log.v(TAG, "Args [ID]: " + Arrays.toString(args));
    }
    
    if (days == null || days.length == 0) {
      // skip days
      hasDays = false;
    } else if (days.length == 1) {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      if (hasIDs) { statement.append("AND "); }
      statement.append("DayInMonth = '").append(days[0]).append("' ");
    } else {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      if (hasIDs) { statement.append("AND "); }
      statement.append("DayInMonth IN (").append(makePlaceholders(days.length)).append(") ");
      args = ArrayUtils.addAll(args, Utility.serializeInts(days));
      Log.v(TAG, "Args + [Day]: " + Arrays.toString(args));
    }
    
    if (months == null || months.isEmpty()) {
      // skip months
      hasMonths = false;
    } else {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      if (hasIDs || hasDays) { statement.append("AND "); }
      months.remove(Month.NONE);
      statement.append("Month IN (").append(makePlaceholders(months.size())).append(") ");
      args = ArrayUtils.addAll(args, Utility.serializeMonths(months));
      Log.v(TAG, "Args + [Month]: " + Arrays.toString(args));
    }
    
    if (years == null || years.length == 0) {
      // skip years
    } else if (years.length == 1) {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      if (hasIDs || hasDays || hasMonths) { statement.append("AND "); }
      statement.append("Year = '").append(years[0]).append("' ");
    } else {
      if (!whereAppended) { whereAppended = true; statement.append("WHERE "); }
      if (hasIDs || hasDays || hasMonths) { statement.append("AND "); }
      statement.append("Year IN (").append(makePlaceholders(years.length)).append(") ");
      args = ArrayUtils.addAll(args, Utility.serializeInts(years));
      Log.v(TAG, "Args + [Year]: " + Arrays.toString(args));
    }
    
    String resultStatement = statement.append(";").toString();
    CompoundStatement compoundStatement = new CompoundStatement(resultStatement, args);
    Log.d(TAG, compoundStatement.toString());
    return compoundStatement;
  }
  
  private Cursor fetchRowidsFromTable(String table_name) {
    String statement = "SELECT rowid FROM '" + table_name + "';";
    return mDbHandler.rawQuery(statement, null);
  }
  
  private Cursor fetchDataFromTable(String table_name) {
    String statement = "SELECT * FROM '" + table_name + "';";
    return mDbHandler.rawQuery(statement, null);
  }
  
  private Cursor fetchDataFromTableByRowid(String table_name, long rowid) {
    String statement = "SELECT * FROM '" + table_name + "' WHERE rowid = ?;";
    return mDbHandler.rawQuery(statement, new String[]{Long.toString(rowid)});
  }
  
  private Cursor fetchDataFromTableWithRowids(String table_name, final long[] rowids) {
    String statement = "";
    try {
      statement = "SELECT * FROM '" + table_name + "' WHERE rowid IN(" + makePlaceholders(rowids.length) + ");";
    } catch (NoPlaceholdersException e) {
      throw new RuntimeException(e);
    }
    return mDbHandler.rawQuery(statement, Utility.serializeLongs(rowids));
  }
}
