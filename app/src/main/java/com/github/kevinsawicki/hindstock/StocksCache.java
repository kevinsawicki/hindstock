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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.github.kevinsawicki.wishlist.DatabaseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * Stocks cached in SQLite populated initially from a file
 */
public class StocksCache extends DatabaseHelper {

  private static final String TAG = "StocksLoader";

  private final Context context;

  /**
   * @param context
   */
  public StocksCache(final Context context) {
    super(context, "stocks.db", null, 6);

    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE stocks (_id INTEGER PRIMARY KEY AUTOINCREMENT, symbol TEXT, name TEXT, exchange TEXT);");
    db.execSQL("CREATE VIRTUAL TABLE search USING fts3(symbol TEXT PRIMARY KEY, name TEXT);");

    BufferedReader reader = null;
    long time = System.currentTimeMillis();
    int count = 0;
    ContentValues values = new ContentValues(3);
    db.beginTransaction();
    try {
      reader = new BufferedReader(new InputStreamReader(context.getAssets()
          .open("stocks.txt")), 8192 * 2);
      String symbol;
      while ((symbol = reader.readLine()) != null) {
        String name = reader.readLine();
        String exchange = reader.readLine();

        values.clear();
        values.put("symbol", symbol);
        values.put("name", name);
        db.insert("search", null, values);
        values.put("exchange", exchange);
        db.insert("stocks", null, values);
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
    db.execSQL("DROP TABLE IF EXISTS search");
    onCreate(db);
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
        "stocks.name", "stocks.exchange" }, null, null, null, null, null);
  }

  /**
   * Get stocks filtered to given query
   *
   * @param query
   * @return cursor
   */
  public Cursor getFilteredStocks(final String query) {
    SQLiteDatabase db = getReadable();
    if (db == null)
      return null;

    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables("stocks JOIN search ON (stocks.symbol = search.symbol)");
    return builder.query(db, new String[] { "stocks._id", "stocks.symbol",
        "stocks.name", "stocks.exchange" }, "search MATCH ?",
        new String[] { "name:*" + query + '*' }, null, null, null);
  }
}
