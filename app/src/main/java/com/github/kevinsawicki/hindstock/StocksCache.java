/*
 * Copyright 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kevinsawicki.hindstock;

import static java.util.Locale.US;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * Stocks cached in SQLite populated initially from a file
 */
public class StocksCache extends SQLiteOpenHelper {

  private static final String TAG = "StocksLoader";

  private final Context context;

  /**
   * @param context
   */
  public StocksCache(final Context context) {
    super(context, "stocks.db", null, 2);

    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE VIRTUAL TABLE stocks USING fts3(_id INTEGER PRIMARY KEY AUTOINCREMENT, symbol TEXT, name TEXT);");

    BufferedReader reader = null;
    long time = System.currentTimeMillis();
    int count = 0;
    ContentValues values = new ContentValues(2);
    db.beginTransaction();
    try {
      reader = new BufferedReader(new InputStreamReader(context.getAssets()
          .open("stocks.txt")), 8192 * 2);
      String symbol;
      while ((symbol = reader.readLine()) != null) {
        if (symbol.length() == 0)
          continue;
        String name = reader.readLine();
        if (TextUtils.isEmpty(name))
          continue;

        values.put("symbol", symbol);
        values.put("name", name);
        db.replace("stocks", null, values);
        count++;
      }
      db.setTransactionSuccessful();
    } catch (IOException e) {
      Log.d(TAG, "Loading stocks failed", e);
    } finally {
      db.endTransaction();
      time = System.currentTimeMillis() - time;
      Log.d(TAG,
          MessageFormat.format("Stock loaded: {0} Time: {1}ms", count, time));
      if (reader != null)
        try {
          reader.close();
        } catch (IOException ignored) {
          // Ignored
        }
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS stocks");
    onCreate(db);
  }

  /**
   * Get readable database
   *
   * @return readable database or null if it failed to create/open
   */
  protected SQLiteDatabase getReadable() {
    try {
      return getReadableDatabase();
    } catch (SQLiteException e1) {
      // Make second attempt
      try {
        return getReadableDatabase();
      } catch (SQLiteException e2) {
        return null;
      }
    }
  }

  /**
   * Get cursor over all stocks
   *
   * @return cursor
   */
  public Cursor getStocks() {
    SQLiteDatabase db = getReadable();
    if (db == null)
      return null;
    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables("stocks");
    return builder.query(db, new String[] { "stocks._id", "stocks.symbol",
        "stocks.name" }, null, null, null, null, null);
  }

  /**
   * Get stocks filtered to given query
   *
   * @param query
   * @return cursor
   */
  public Cursor getFilteredStocks(String query) {
    SQLiteDatabase db = getReadable();
    if (db == null)
      return null;

    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables("stocks");
    return builder.query(db, new String[] { "stocks._id", "stocks.symbol",
        "stocks.name" }, "stocks MATCH ?",
        new String[] { "symbol:" + query.toUpperCase(US) + '*' + "OR name:*"
            + query + '*' }, null, null, null);
  }
}
