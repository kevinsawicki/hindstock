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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.R.id;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.hindstock.R.layout;
import com.github.kevinsawicki.hindstock.R.string;

/**
 * Activity to choose a stock symbol from a filterable list
 */
public class SelectStockActivity extends SherlockActivity {

	private ListView list;

	private EditText search;

	private StockListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(layout.stock_list);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(string.title_select_stock);
		actionBar.setDisplayHomeAsUpEnabled(true);

		list = (ListView) findViewById(id.lv_symbols);
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Stock stock = (Stock) adapter.getItem(position);
				Intent result = new Intent();
				result.putExtra(EXTRA_STOCK.toString(), stock);
				setResult(RESULT_OK, result);
				finish();
			}
		});

		search = (EditText) findViewById(id.et_search);
		search.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s);
			}
		});
		loadStocks();
	}

	private void loadStocks() {
		new StockListLoader(this) {

			@Override
			protected void onPostExecute(Stock[] result) {
				adapter = new StockListAdapter(layout.stock,
						getLayoutInflater(), result);
				list.setAdapter(adapter);
				search.setEnabled(true);
			};
		}.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, HindStockActivity.class);
			intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
