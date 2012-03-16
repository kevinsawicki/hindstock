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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.github.kevinsawicki.hindstock.R.id;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to display a list of stocks
 */
public class StockListAdapter extends ItemListAdapter<Stock> implements
		Filterable {

	private static class StockView extends ItemView<Stock> {

		private final TextView symbolText;
		private final TextView nameText;

		/**
		 * @param view
		 */
		public StockView(View view) {
			symbolText = (TextView) view.findViewById(id.tv_symbol);
			nameText = (TextView) view.findViewById(id.tv_name);
		}

		public void update(Stock item) {
			symbolText.setText(item.getSymbol());
			nameText.setText(item.getName());
		}
	}

	/**
	 * @param viewId
	 * @param inflater
	 * @param elements
	 */
	public StockListAdapter(int viewId, LayoutInflater inflater,
			Stock[] elements) {
		super(viewId, inflater, elements);
	}

	protected ItemView<Stock> createItemView(View view) {
		return new StockView(view);
	}

	public Filter getFilter() {
		return new Filter() {

			private boolean isPrefix(String prefix, int prefixLength,
					String value, int start, int end) {
				if (prefixLength > end - start)
					return false;
				for (int pIndex = 0; pIndex < prefixLength; pIndex++, start++)
					if (prefix.charAt(pIndex) != Character.toUpperCase(value
							.charAt(start)))
						return false;
				return true;
			}

			protected FilterResults performFiltering(CharSequence prefix) {
				FilterResults results = new FilterResults();
				final Stock[] initial = getInitialElements();

				if (prefix != null && prefix.length() > 0) {
					String upperPrefix = prefix.toString().toUpperCase(US);
					List<Stock> filteredSymbols = new ArrayList<Stock>();
					List<Stock> filteredNames = new ArrayList<Stock>();
					for (Stock stock : initial)
						if (stock.getSymbol().startsWith(upperPrefix))
							filteredSymbols.add(stock);
						else {
							String name = stock.getName();
							int nameLength = name.length();
							int prefixLength = upperPrefix.length();
							int index = 0;
							while (index < nameLength) {
								int space = name.indexOf(' ', index);
								if (space == -1)
									space = nameLength;
								if (isPrefix(upperPrefix, prefixLength, name,
										index, space))
									filteredNames.add(stock);
								index = space + 1;
							}
						}

					filteredSymbols.addAll(filteredNames);
					results.values = filteredSymbols.toArray();
					results.count = filteredSymbols.size();
				} else {
					results.values = initial;
					results.count = initial.length;
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				setItems((Object[]) results.values);
				if (results.count > 0)
					notifyDataSetChanged();
				else
					notifyDataSetInvalidated();
			}
		};
	}
}
