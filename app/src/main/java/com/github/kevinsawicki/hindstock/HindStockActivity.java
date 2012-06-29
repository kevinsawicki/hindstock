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

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
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
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.github.kevinsawicki.hindstock.GainLossRequest.Quote;
import com.github.kevinsawicki.hindstock.R.color;
import com.github.kevinsawicki.hindstock.R.id;
import com.github.kevinsawicki.hindstock.R.layout;
import com.github.kevinsawicki.hindstock.R.string;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Main activity to compute the net gain/loss on a theoretical stock purchase of
 * either a quantity of shares or dollar amount investment.
 */
public class HindStockActivity extends Activity implements OnClickListener {

	private static final String TAG = "HindStock";

	private static final int ID_BUY_DATE = 0;

	private static final int ID_SELL_DATE = 1;

	private static final String EXTRA_QUOTE = "quote";

	private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

	private final NumberFormat decimalFormat = new DecimalFormat("0.00");

	private final DateFormat dateFormat = DateFormat.getDateInstance(SHORT);

	private Calendar buyDate = new GregorianCalendar();

	private Calendar sellDate = new GregorianCalendar();

	private Quote quote;

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

		setupDateArea();
		setupQuantityArea();
		setupDoneListeners();

		loadStocks();
	}

	private void setupDoneListeners() {
		OnKeyListener doneKeyListener = new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event != null && ACTION_DOWN == event.getAction()
						&& keyCode == KEYCODE_ENTER
						&& calcButton.getVisibility() == VISIBLE) {
					onClick(calcButton);
					return true;
				} else
					return false;
			}
		};
		symbolText.setOnKeyListener(doneKeyListener);
		shareText.setOnKeyListener(doneKeyListener);
		dollarText.setOnKeyListener(doneKeyListener);

		OnEditorActionListener doneEditorActionListener = new OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == IME_ACTION_DONE
						&& calcButton.getVisibility() == VISIBLE) {
					onClick(calcButton);
					return true;
				} else
					return false;
			}
		};
		symbolText.setOnEditorActionListener(doneEditorActionListener);
		shareText.setOnEditorActionListener(doneEditorActionListener);
		dollarText.setOnEditorActionListener(doneEditorActionListener);
	}

	private void setupDateArea() {
		OnClickListener sellDateListener = new OnClickListener() {

			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(ID_SELL_DATE);
			}
		};
		findViewById(id.tv_sell_date).setOnClickListener(sellDateListener);
		sellDateText.setOnClickListener(sellDateListener);

		OnClickListener buyDateListener = new OnClickListener() {

			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(ID_BUY_DATE);
			}
		};
		findViewById(id.tv_buy_date).setOnClickListener(buyDateListener);
		buyDateText.setOnClickListener(buyDateListener);
	}

	private void setupQuantityArea() {
		final RadioButton dollarsButton = (RadioButton) findViewById(id.rb_dollars);
		dollarsButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				dollarText.setEnabled(isChecked);
				dollarText.setFocusable(isChecked);
				dollarText.setFocusableInTouchMode(isChecked);
				if (isChecked)
					dollarText.requestFocus();
			}
		});

		final RadioButton sharesButton = (RadioButton) findViewById(id.rb_shares);
		sharesButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				shareText.setEnabled(isChecked);
				shareText.setFocusable(isChecked);
				shareText.setFocusableInTouchMode(isChecked);
				if (isChecked)
					shareText.requestFocus();
			}
		});

		OnClickListener sharesListener = new OnClickListener() {

			public void onClick(View v) {
				dollarsButton.setChecked(false);
				sharesButton.setChecked(true);
			}
		};
		findViewById(id.tv_shares).setOnClickListener(sharesListener);
		shareText.setOnClickListener(sharesListener);

		OnClickListener dollarsListener = new OnClickListener() {

			public void onClick(View v) {
				sharesButton.setChecked(false);
				dollarsButton.setChecked(true);
			}
		};
		findViewById(id.tv_dollars).setOnClickListener(dollarsListener);
		dollarText.setOnClickListener(dollarsListener);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (quote != null)
			outState.putSerializable(EXTRA_QUOTE, quote);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		String initialBuyDate = buyDateText.getText().toString();
		if (!TextUtils.isEmpty(initialBuyDate))
			try {
				buyDate.setTime(dateFormat.parse(initialBuyDate));
			} catch (ParseException e) {
				buyDate = new GregorianCalendar();
				buyDate.add(YEAR, -1);
				buyDateText.setText("");
			}
		else {
			buyDate = new GregorianCalendar();
			buyDate.add(YEAR, -1);
		}

		String initialSellDate = sellDateText.getText().toString();
		if (!TextUtils.isEmpty(initialSellDate))
			try {
				sellDate.setTime(dateFormat.parse(initialSellDate));
			} catch (ParseException e) {
				sellDate = new GregorianCalendar();
				sellDateText.setText("");
			}
		else
			sellDate = new GregorianCalendar();

		quote = (Quote) savedInstanceState.get(EXTRA_QUOTE);
		if (quote != null)
			displayQuote(quote);
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

		final int message;
		if (e instanceof InvalidBuyDateException)
			message = string.no_quote_for_buy_date;
		else if (e instanceof InvalidSellDateException)
			message = string.no_quote_for_sell_date;
		else
			message = string.requesting_quote_failed;

		runOnUiThread(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(), message, LENGTH_LONG)
						.show();
			}
		});
	}

	private int getColor(final int id) {
		return getResources().getColor(id);
	}

	private String getSymbol() {
		String symbol = symbolText.getText().toString().trim();
		return !TextUtils.isEmpty(symbol) ? symbol : "GOOG";
	}

	private float getShares() {
		String text = shareText.getText().toString().trim();
		if (TextUtils.isEmpty(text))
			return 100;

		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException nfe) {
			Toast.makeText(getApplicationContext(),
					string.error_parsing_share_amount, LENGTH_LONG).show();
			return -1;
		}
	}

	private float getDollars() {
		String text = dollarText.getText().toString().trim();
		if (TextUtils.isEmpty(text))
			return 1000;

		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException nfe) {
			Toast.makeText(getApplicationContext(),
					string.error_parsing_dollar_amount, LENGTH_LONG).show();
			return -1;
		}
	}

	private DatePickerDialog createSellDateDialog() {
		DatePickerDialog dialog = new DatePickerDialog(this,
				new OnDateSetListener() {

					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						Calendar updated = new GregorianCalendar(year,
								monthOfYear, dayOfMonth);
						Calendar today = new GregorianCalendar();
						if (updated.after(today)) {
							Toast.makeText(getApplicationContext(),
									string.invalid_sell_date, LENGTH_LONG)
									.show();
							updated = today;
							if (TextUtils.isEmpty(sellDateText.getText()))
								return;
						}

						sellDate.setTimeInMillis(updated.getTimeInMillis());
						sellDateText.setText(dateFormat.format(sellDate
								.getTime()));
					}
				}, sellDate.get(YEAR), sellDate.get(MONTH),
				sellDate.get(DAY_OF_MONTH));
		dialog.setTitle(string.set_sell_date);
		return dialog;
	}

	private DatePickerDialog createBuyDateDialog() {
		DatePickerDialog dialog = new DatePickerDialog(this,
				new OnDateSetListener() {

					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						Calendar updated = new GregorianCalendar(year,
								monthOfYear, dayOfMonth);
						if (updated.after(sellDate)) {
							Toast.makeText(getApplicationContext(),
									string.invalid_buy_date, LENGTH_LONG)
									.show();
							updated.setTimeInMillis(sellDate.getTimeInMillis());
							updated.add(DAY_OF_MONTH, -1);
						}

						buyDate.setTimeInMillis(updated.getTimeInMillis());
						buyDateText.setText(dateFormat.format(buyDate.getTime()));
					}
				}, buyDate.get(YEAR), buyDate.get(MONTH), buyDate
						.get(DAY_OF_MONTH));
		dialog.setTitle(string.set_buy_date);
		return dialog;
	}

	@SuppressWarnings("deprecation")
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

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(final int dialogId, final Bundle args) {
		switch (dialogId) {
		case ID_BUY_DATE:
			return createBuyDateDialog();
		case ID_SELL_DATE:
			return createSellDateDialog();
		default:
			return super.onCreateDialog(dialogId, args);
		}
	}

	private void hideKeyboard() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(getWindow().getDecorView()
						.getWindowToken(), 0);
	}

	private void displayQuote(final Quote quote) {
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

		netTextValue.append("  (").append(numberFormat.format(percentage))
				.append('%').append(')');
		buyPriceText.setText("$ " + decimalFormat.format(quote.getCost()));
		priceLabelsArea.setVisibility(VISIBLE);
		buyPriceText.setText("$ " + decimalFormat.format(quote.buyPrice));
		sellPriceText.setText("$ " + decimalFormat.format(quote.sellPrice));
		priceValuesArea.setVisibility(VISIBLE);
		netArea.setVisibility(VISIBLE);
		netText.setText(netTextValue);

		loadingArea.setVisibility(INVISIBLE);
		calcButton.setVisibility(VISIBLE);
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

			@Override
			protected void onSuccess(Quote quote) {
				HindStockActivity.this.quote = quote;
				displayQuote(quote);
			}

			@Override
			protected void onFailure(IOException cause) {
				loadingArea.setVisibility(INVISIBLE);
				calcButton.setVisibility(VISIBLE);
				showQuoteException(cause);
			}
		}.execute();
	}
}