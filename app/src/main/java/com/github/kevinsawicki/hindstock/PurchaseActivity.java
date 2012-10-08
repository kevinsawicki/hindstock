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

import static com.github.kevinsawicki.hindstock.IntentConstant.EXTRA_QUOTE;
import static com.github.kevinsawicki.hindstock.IntentConstant.EXTRA_STOCK;
import static java.text.DateFormat.SHORT;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.hindstock.R.id;
import com.github.kevinsawicki.hindstock.R.layout;
import com.github.kevinsawicki.hindstock.R.menu;
import com.github.kevinsawicki.hindstock.R.string;
import com.github.kevinsawicki.wishlist.EditTextUtils;
import com.github.kevinsawicki.wishlist.EditTextUtils.BooleanRunnable;
import com.github.kevinsawicki.wishlist.Toaster;
import com.github.kevinsawicki.wishlist.ViewFinder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main activity to compute the net gain/loss on a theoretical stock purchase of
 * either a quantity of shares or dollar amount investment.
 */
public class PurchaseActivity extends SherlockActivity {

  private static final String TAG = "PurchaseActivity";

  private static final int REQUEST_BROWSE = 1;

  private static final int ID_BUY_DATE = 0;

  private static final int ID_SELL_DATE = 1;

  private final DateFormat dateFormat = DateFormat.getDateInstance(SHORT);

  private Calendar buyDate = new GregorianCalendar();

  private Calendar sellDate = new GregorianCalendar();

  private Calendar dialogDate = new GregorianCalendar();

  private Quote quote;

  private boolean calculating;

  private AutoCompleteTextView symbolText;

  private EditText amountText;

  private EditText buyDateText;

  private EditText sellDateText;

  private RadioButton dollarsButton;

  private MenuItem calculateItem;

  @Override
  public boolean onCreateOptionsMenu(Menu optionsMenu) {
    getSupportMenuInflater().inflate(menu.main, optionsMenu);
    calculateItem = optionsMenu.findItem(id.m_calculate);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case id.m_calculate:
      calculate();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(layout.main);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(string.app_name);
    actionBar.setSubtitle(string.main_subtitle);

    ViewFinder finder = new ViewFinder(this);
    symbolText = finder.find(id.actv_stock);
    amountText = finder.find(id.et_amount);
    buyDateText = finder.find(id.et_buy_date);
    sellDateText = finder.find(id.et_sell_date);
    dollarsButton = finder.find(id.rb_dollars);

    finder.onClick(id.tv_browse, new Runnable() {

      @Override
      public void run() {
        startActivityForResult(new Intent(getApplicationContext(),
            ViewStocksActivity.class), REQUEST_BROWSE);
      }
    });

    buyDate.add(YEAR, -1);

    setupDateArea(finder);
    setupDoneListeners();

    loadStocks();
  }

  private void loadStocks() {
    new StocksLoader(getApplicationContext()) {

      @Override
      protected void onPostExecute(Cursor cursor) {
        if (cursor == null)
          return;

        Context context = getApplicationContext();
        StockListAdapter adapter = new StockListAdapter(context, cursor);
        adapter.setFilterQueryProvider(new StocksFilter(context));
        symbolText.setAdapter(adapter);

      };
    }.execute();
  }

  private void setupDoneListeners() {
    EditTextUtils.onDone(amountText, new BooleanRunnable() {

      @Override
      public boolean run() {
        if (canCalculate()) {
          calculate();
          return true;
        } else
          return false;
      }
    });
  }

  private void setupDateArea(ViewFinder finder) {
    OnClickListener sellDateListener = new OnClickListener() {

      @SuppressWarnings("deprecation")
      public void onClick(View v) {
        if (!calculating)
          showDialog(ID_SELL_DATE);
      }
    };
    finder.onClick(id.tv_sell_date, sellDateListener);
    sellDateText.setOnClickListener(sellDateListener);

    OnClickListener buyDateListener = new OnClickListener() {

      @SuppressWarnings("deprecation")
      public void onClick(View v) {
        if (!calculating)
          showDialog(ID_BUY_DATE);
      }
    };
    finder.onClick(id.tv_buy_date, buyDateListener);
    buyDateText.setOnClickListener(buyDateListener);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    if (quote != null)
      outState.putSerializable(EXTRA_QUOTE.toString(), quote);
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

    quote = (Quote) savedInstanceState.get(EXTRA_QUOTE.toString());
    if (quote != null)
      displayQuote(quote);
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

    Toaster.showLong(this, message);
  }

  private String getSymbol() {
    String symbol = symbolText.getText().toString().trim();
    return !TextUtils.isEmpty(symbol) ? symbol : symbolText.getHint()
        .toString();
  }

  private float getAmount() {
    String text = amountText.getText().toString().trim();
    if (TextUtils.isEmpty(text))
      text = amountText.getHint().toString();

    try {
      return Float.parseFloat(text);
    } catch (NumberFormatException nfe) {
      Toaster.showLong(this, string.error_parsing_amount);
      return -1;
    }
  }

  private DatePickerDialog createDateDialog(Calendar date, int title) {
    DatePickerDialog dialog = new DatePickerDialog(this,
        new OnDateSetListener() {

          @Override
          public void onDateSet(DatePicker view, int year, int monthOfYear,
              int dayOfMonth) {
            dialogDate.set(YEAR, year);
            dialogDate.set(MONTH, monthOfYear);
            dialogDate.set(DAY_OF_MONTH, dayOfMonth);
          }
        }, date.get(YEAR), date.get(MONTH), date.get(DAY_OF_MONTH));
    dialog.setTitle(title);
    dialog.setCancelable(true);
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void onPrepareDialog(final int id, final Dialog dialog,
      final Bundle args) {
    super.onPrepareDialog(id, dialog, args);

    switch (id) {
    case ID_BUY_DATE:
      final AtomicBoolean buyCancelled = new AtomicBoolean(false);
      dialog.setOnCancelListener(new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
          buyCancelled.set(true);
        }
      });
      dialog.setOnDismissListener(new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
          if (buyCancelled.get())
            return;

          if (dialogDate.after(sellDate)) {
            Toaster.showLong(PurchaseActivity.this, string.invalid_buy_date);
            dialogDate.setTimeInMillis(sellDate.getTimeInMillis());
            dialogDate.add(DAY_OF_MONTH, -1);
          }

          buyDate.setTimeInMillis(dialogDate.getTimeInMillis());
          buyDateText.setText(dateFormat.format(buyDate.getTime()));
        }
      });
      ((DatePickerDialog) dialog).updateDate(buyDate.get(YEAR),
          buyDate.get(MONTH), buyDate.get(DAY_OF_MONTH));
      break;
    case ID_SELL_DATE:
      final AtomicBoolean sellCancelled = new AtomicBoolean(false);
      dialog.setOnCancelListener(new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
          sellCancelled.set(true);
        }
      });
      dialog.setOnDismissListener(new OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
          if (sellCancelled.get())
            return;

          sellDate.setTimeInMillis(dialogDate.getTimeInMillis());
          sellDateText.setText(dateFormat.format(sellDate.getTime()));
        }
      });
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
      return createDateDialog(buyDate, string.set_buy_date);
    case ID_SELL_DATE:
      return createDateDialog(sellDate, string.set_sell_date);
    default:
      return super.onCreateDialog(dialogId, args);
    }
  }

  private void hideKeyboard() {
    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
        .hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
  }

  private void displayQuote(final Quote quote) {
    Intent intent = new Intent(getApplicationContext(), QuoteActivity.class);
    intent.putExtra(EXTRA_QUOTE.name(), quote);
    startActivity(intent);

    showCalculating(false);
  }

  private void showCalculating(boolean calculating) {
    this.calculating = calculating;

    if (calculateItem != null)
      calculateItem.setEnabled(!calculating);

    symbolText.setEnabled(!calculating);
    amountText.setEnabled(!calculating);
  }

  private boolean canCalculate() {
    return calculateItem != null && calculateItem.isEnabled();
  }

  private void calculate() {
    hideKeyboard();

    String symbol = getSymbol();
    float dollars;
    float shares;
    if (dollarsButton.isChecked()) {
      dollars = getAmount();
      shares = -1;
    } else {
      dollars = -1;
      shares = getAmount();
    }

    showCalculating(true);
    new GainLossRequest(symbol, shares, dollars, buyDate, sellDate) {

      @Override
      protected void onSuccess(Quote quote) {
        PurchaseActivity.this.quote = quote;
        displayQuote(quote);
      }

      @Override
      protected void onFailure(IOException cause) {
        showCalculating(false);
        showQuoteException(cause);
      }
    }.execute();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_BROWSE && resultCode == RESULT_OK
        && data != null) {
      Stock stock = (Stock) data.getSerializableExtra(EXTRA_STOCK.name());
      if (stock != null) {
        symbolText.setText(stock.symbol);
        symbolText.setSelection(stock.symbol.length());
        symbolText.dismissDropDown();
        amountText.requestFocus();
      }
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }
}