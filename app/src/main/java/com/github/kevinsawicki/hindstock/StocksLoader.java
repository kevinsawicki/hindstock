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
import android.os.AsyncTask;

/**
 * Loader for list of all stocks
 */
public class StocksLoader extends AsyncTask<Void, Void, Cursor> {

  /**
   * Context
   */
  protected final Context context;

  /**
   * Create stock list loader
   *
   * @param context
   */
  public StocksLoader(final Context context) {
    this.context = context;
  }

  @Override
  protected Cursor doInBackground(Void... params) {
    return new StocksCache(context).getStocks();
  }
}
