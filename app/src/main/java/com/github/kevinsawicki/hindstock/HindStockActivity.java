/*
 * Copyright (c) 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.github.kevinsawicki.hindstock;

import static android.view.View.GONE;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Main activity to compute the net gain/loss on a theoretical stock purchase of
 * either a quantity of shares or dollar figure investment.
 */
public class HindStockActivity extends Activity {

	private static final String TAG = "HindStock";

	private static final int ID_BUY_DATE = 0;

	private static final int ID_SELL_DATE = 1;

	private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

	private final DateFormat dateFormat = DateFormat.getDateInstance(SHORT);

	private final Calendar buyDate = new GregorianCalendar();

	private final Calendar sellDate = new GregorianCalendar();

	private EditText symbolText;

	private EditText shareText;

	private EditText dollarText;

	private EditText buyDateText;

	private EditText sellDateText;

	private LinearLayout loadingArea;

	private TextView netText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.main);

		symbolText = (EditText) findViewById(id.et_stock);
		shareText = (EditText) findViewById(id.et_shares);
		dollarText = (EditText) findViewById(id.et_dollars);
		buyDateText = (EditText) findViewById(id.et_buy_date);
		sellDateText = (EditText) findViewById(id.et_sell_date);
		loadingArea = (LinearLayout) findViewById(id.ll_loading);
		netText = (TextView) findViewById(id.tv_net);

		buyDate.add(YEAR, -1);

		Button calcButton = (Button) findViewById(id.b_calculate);
		calcButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(getWindow().getDecorView()
								.getWindowToken(), 0);

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

				loadingArea.setVisibility(VISIBLE);
				netText.setVisibility(GONE);
				new GainLossRequest(symbol, shares, dollars, buyDate, sellDate) {

					protected void onSuccess(float netAmount) {
						int dollars = Math.round(Math.abs(netAmount));
						StringBuilder netLabel = new StringBuilder();
						if (netAmount >= 0) {
							netText.setTextColor(getColor(color.gain));
							netLabel.append('+');
						} else {
							netText.setTextColor(getColor(color.loss));
							netLabel.append('-');
						}
						netLabel.append('$');
						netLabel.append(numberFormat.format(dollars));
						netLabel.append(" USD");
						netText.setText(netLabel);

						loadingArea.setVisibility(GONE);
						netText.setVisibility(VISIBLE);
					}

					protected void onFailure(IOException cause) {
						loadingArea.setVisibility(VISIBLE);
						netText.setVisibility(GONE);
						showQuoteException(cause);
					}
				}.execute();
			}
		});

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
	}

	private void showQuoteException(final IOException e) {
		Log.d(TAG, "Exception requesting quote", e);
		runOnUiThread(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(),
						"Requesting quote failed, please try again",
						LENGTH_LONG);
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
					"Error parsing share amount", LENGTH_LONG);
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
					"Error parsing dollar amount", LENGTH_LONG);
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
}