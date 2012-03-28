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

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static java.text.DateFormat.SHORT;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.hindstock.R.color;
import com.github.kevinsawicki.hindstock.R.id;
import com.github.kevinsawicki.hindstock.R.layout;
import com.github.kevinsawicki.hindstock.R.string;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Main activity to compute the net gain/loss on a theoretical stock purchase of
 * either a quantity of shares or dollar figure investment.
 */
public class HindStockActivity extends Activity implements OnClickListener {

	private static final String TAG = "HindStock";

	private static final int ID_BUY_DATE = 0;

	private static final int ID_SELL_DATE = 1;

	private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

	private final NumberFormat decimalFormat = new DecimalFormat("0.00");

	private final DateFormat dateFormat = DateFormat.getDateInstance(SHORT);

	private final Calendar buyDate = new GregorianCalendar();

	private final Calendar sellDate = new GregorianCalendar();

	private AutoCompleteTextView symbolText;

	private EditText shareText;

	private EditText dollarText;

	private EditText buyDateText;

	private EditText sellDateText;

	private LinearLayout priceLabelsArea;

	private LinearLayout priceValuesArea;

	private TextView buyPriceText;

	private TextView sellPriceText;

	private LinearLayout loadingArea;

	private LinearLayout netArea;

	private TextView netLabel;

	private TextView netText;

	private Button calcButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.main);

		symbolText = (AutoCompleteTextView) findViewById(id.actv_stock);
		shareText = (EditText) findViewById(id.et_shares);
		dollarText = (EditText) findViewById(id.et_dollars);
		buyDateText = (EditText) findViewById(id.et_buy_date);
		sellDateText = (EditText) findViewById(id.et_sell_date);
		loadingArea = (LinearLayout) findViewById(id.ll_loading);
		priceLabelsArea = (LinearLayout) findViewById(id.ll_price_labels);
		priceValuesArea = (LinearLayout) findViewById(id.ll_price_values);
		buyPriceText = (TextView) findViewById(id.tv_buy_price);
		sellPriceText = (TextView) findViewById(id.tv_sell_price);
		netArea = (LinearLayout) findViewById(id.ll_net);
		netLabel = (TextView) findViewById(id.tv_net_label);
		netText = (TextView) findViewById(id.tv_net);
		calcButton = (Button) findViewById(id.b_calculate);

		buyDate.add(YEAR, -1);

		calcButton.setOnClickListener(this);

		sellDateText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showDialog(ID_SELL_DATE);
			}
		});
		sellDateText.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					showDialog(ID_SELL_DATE);
			}
		});

		buyDateText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showDialog(ID_BUY_DATE);
			}
		});
		buyDateText.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					showDialog(ID_BUY_DATE);
			}
		});

		((RadioButton) findViewById(id.rb_dollars))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						dollarText.setEnabled(isChecked);
					}
				});

		((RadioButton) findViewById(id.rb_shares))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						shareText.setEnabled(isChecked);
					}
				});

		loadStocks();
	}

	private void loadStocks() {
		new StockListLoader(this) {
			protected void onPostExecute(Stock[] result) {
				symbolText.setAdapter(new StockListAdapter(layout.stock,
						getLayoutInflater(), result));
			};
		}.execute();
	}

	private void showQuoteException(final IOException e) {
		Log.d(TAG, "Exception requesting quote", e);
		runOnUiThread(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(),
						string.requesting_quote_failed, LENGTH_LONG).show();
			}
		});
	}

	private int getColor(final int id) {
		return getResources().getColor(id);
	}

	private String getSymbol() {
		String symbol = symbolText.getText().toString().trim();
		return symbol.length() > 0 ? symbol : "GOOG";
	}

	private float getShares() {
		String text = shareText.getText().toString().trim();
		if (text.length() == 0)
			return 100;

		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException nfe) {
			Toast.makeText(getApplicationContext(),
					string.error_parsing_share_amount, LENGTH_LONG);
			return -1;
		}
	}

	private float getDollars() {
		String text = dollarText.getText().toString().trim();
		if (text.length() == 0)
			return 1000;

		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException nfe) {
			Toast.makeText(getApplicationContext(),
					string.error_parsing_dollar_amount, LENGTH_LONG);
			return -1;
		}
	}

	private DatePickerDialog createDateDialog(final Calendar date,
			final EditText field) {
		return new DatePickerDialog(this, new OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				date.set(YEAR, year);
				date.set(MONTH, monthOfYear);
				date.set(DAY_OF_MONTH, dayOfMonth);
				field.setText(dateFormat.format(date.getTime()));
			}
		}, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH));
	}

	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog,
			final Bundle args) {
		super.onPrepareDialog(id, dialog, args);

		switch (id) {
		case ID_BUY_DATE:
			((DatePickerDialog) dialog).updateDate(buyDate.get(YEAR),
					buyDate.get(MONTH), buyDate.get(DAY_OF_MONTH));
			break;
		case ID_SELL_DATE:
			((DatePickerDialog) dialog).updateDate(sellDate.get(YEAR),
					sellDate.get(MONTH), sellDate.get(DAY_OF_MONTH));
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(final int dialogId, final Bundle args) {
		switch (dialogId) {
		case ID_BUY_DATE:
			return createDateDialog(buyDate, buyDateText);
		case ID_SELL_DATE:
			return createDateDialog(sellDate, sellDateText);
		default:
			return super.onCreateDialog(dialogId, args);
		}
	}

	private void hideKeyboard() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(getWindow().getDecorView()
						.getWindowToken(), 0);
	}

	public void onClick(View v) {
		hideKeyboard();

		String symbol = getSymbol();
		float dollars;
		float shares;
		if (dollarText.isEnabled()) {
			dollars = getDollars();
			shares = -1;
		} else {
			dollars = -1;
			shares = getShares();
		}

		calcButton.setVisibility(INVISIBLE);
		loadingArea.setVisibility(VISIBLE);
		new GainLossRequest(symbol, shares, dollars, buyDate, sellDate) {

			protected void onSuccess(Quote quote) {
				float netAmount = quote.getNet();
				double dollars = Math.floor(Math.abs(netAmount) + 0.5F);
				int percentage = Math.round(quote.getRate());
				percentage = Math.abs(percentage);
				StringBuilder netTextValue = new StringBuilder();
				if (netAmount >= 0) {
					netLabel.setTextColor(getColor(color.gain));
					netLabel.setText(getString(string.profit_label));
					netText.setTextColor(getColor(color.gain));
				} else {
					netLabel.setTextColor(getColor(color.loss));
					netLabel.setText(getString(string.loss_label));
					netText.setTextColor(getColor(color.loss));
				}
				netTextValue.append('$').append(' ');

				if (dollars < 1000000F) {
					netTextValue.append(numberFormat.format(dollars));
				} else if (dollars < 1000000000F) {
					dollars = dollars / 1000000F;
					netTextValue.append(decimalFormat.format(dollars));
					netTextValue.append(' ').append('m');
				} else if (dollars < 1000000000000F) {
					dollars = dollars / 1000000000F;
					netTextValue.append(decimalFormat.format(dollars));
					netTextValue.append(' ').append('b');
				} else if (dollars < 1000000000000000F) {
					dollars = dollars / 1000000000000F;
					netTextValue.append(decimalFormat.format(dollars));
					netTextValue.append(' ').append('t');
				}

				netTextValue.append("  (")
						.append(numberFormat.format(percentage)).append('%')
						.append(')');
				buyPriceText.setText("$ "
						+ decimalFormat.format(quote.getCost()));
				priceLabelsArea.setVisibility(VISIBLE);
				buyPriceText.setText("$ "
						+ decimalFormat.format(quote.buyPrice));
				sellPriceText.setText("$ "
						+ decimalFormat.format(quote.sellPrice));
				priceValuesArea.setVisibility(VISIBLE);
				netArea.setVisibility(VISIBLE);
				netText.setText(netTextValue);

				loadingArea.setVisibility(INVISIBLE);
				calcButton.setVisibility(VISIBLE);
			}

			protected void onFailure(IOException cause) {
				loadingArea.setVisibility(INVISIBLE);
				calcButton.setVisibility(VISIBLE);
				showQuoteException(cause);
			}
		}.execute();
	}
}