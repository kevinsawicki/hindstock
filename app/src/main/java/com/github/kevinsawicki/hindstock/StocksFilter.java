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
import android.text.TextUtils;
import android.widget.FilterQueryProvider;

/**
 * Filter query provider over all stocks
 */
public class StocksFilter implements FilterQueryProvider {

  private final StocksCache cache;

  /**
   * Create filter query provider
   *
   * @param context
   */
  public StocksFilter(final Context context) {
    cache = new StocksCache(context);
  }

  @Override
  public Cursor runQuery(final CharSequence constraint) {
    if (!TextUtils.isEmpty(constraint))
      return cache.getFilteredStocks(constraint.toString());
    else
      return cache.getStocks();
  }
}
