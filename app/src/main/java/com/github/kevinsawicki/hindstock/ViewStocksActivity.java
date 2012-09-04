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

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.github.kevinsawicki.hindstock.IntentConstant.EXTRA_STOCK;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.hindstock.R.layout;
import com.github.kevinsawicki.wishlist.ViewFinder;

/**
 * Activity to view and select a stock
 */
public class ViewStocksActivity extends SherlockActivity implements
		OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(layout.stocks);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ViewFinder finder = new ViewFinder(this);
		ListView list = finder.find(android.R.id.list);
		final StockListAdapter adapter = new StockListAdapter(layout.stock,
				getLayoutInflater(), new Stock[0]);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		new StockListLoader(this) {

			@Override
			protected void onPostExecute(Stock[] result) {
				adapter.setItems(result);
			}
		}.execute();
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Stock stock = (Stock) parent.getItemAtPosition(position);
		Intent data = new Intent();
		data.putExtra(EXTRA_STOCK.name(), stock);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, PurchaseActivity.class);
			intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
