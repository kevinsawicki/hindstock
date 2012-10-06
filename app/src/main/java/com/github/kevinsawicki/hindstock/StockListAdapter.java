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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;

import com.github.kevinsawicki.hindstock.R.id;
import com.github.kevinsawicki.hindstock.R.layout;

/**
 * Adapter to display a list of stocks
 */
public class StockListAdapter extends SimpleCursorAdapter {

  /**
   * Create adapter for stocks
   *
   * @param context
   * @param cursor
   */
  public StockListAdapter(Context context, Cursor cursor) {
    super(context, layout.stock, cursor, new String[] { "symbol", "name" },
        new int[] { id.tv_symbol, id.tv_name }, 0);
  }

  /**
   * Get stock at position
   *
   * @param position
   * @return stock
   */
  public Stock getStock(int position) {
    Cursor cursor = (Cursor) getItem(position);
    return new Stock(cursor.getString(1), cursor.getString(2));
  }

  @Override
  public CharSequence convertToString(final Cursor cursor) {
    return cursor != null ? cursor.getString(1) : "";
  }
}
