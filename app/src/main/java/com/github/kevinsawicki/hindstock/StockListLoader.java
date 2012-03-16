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
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for persistent stock list used for autocompletion of symbols
 */
public class StockListLoader extends AsyncTask<Void, Void, Stock[]> {

	private static final String TAG = "SLL";

	private final Context context;

	/**
	 * Create stock list loader
	 *
	 * @param context
	 */
	public StockListLoader(final Context context) {
		this.context = context;
	}

	protected Stock[] doInBackground(Void... params) {
		List<Stock> stocks = new ArrayList<Stock>();
		BufferedReader reader = null;
		final long start = System.currentTimeMillis();
		try {
			reader = new BufferedReader(new InputStreamReader(context
					.getAssets().open("stocks.txt")), 8192 * 2);
			String symbol;
			while ((symbol = reader.readLine()) != null) {
				if (symbol.length() == 0)
					continue;
				String name = reader.readLine();
				if (name == null || name.length() == 0)
					continue;
				stocks.add(new Stock(symbol, name));
			}
		} catch (IOException e) {
			Log.d(TAG, "Exception loading stock list", e);
			return new Stock[0];
		} finally {
			Log.d(TAG,
					"Stock loaded: " + stocks.size() + " Time: "
							+ (System.currentTimeMillis() - start));
			if (reader != null)
				try {
					reader.close();
				} catch (IOException ignored) {
					// Ignored
				}
		}
		return stocks.toArray(new Stock[stocks.size()]);
	}
}
