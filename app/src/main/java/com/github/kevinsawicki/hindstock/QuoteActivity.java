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
import static com.github.kevinsawicki.hindstock.IntentConstant.EXTRA_QUOTE;
import android.content.Intent;
import android.os.Bundle;
import android.util.FloatMath;
import android.widget.TextView;

import com.actionbarsherlock.R.layout;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.github.kevinsawicki.hindstock.R.color;
import com.github.kevinsawicki.hindstock.R.id;
import com.github.kevinsawicki.hindstock.R.string;
import com.github.kevinsawicki.wishlist.ViewFinder;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Activity to display a stock quote
 */
public class QuoteActivity extends SherlockActivity {

  private final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

  private final NumberFormat decimalFormat = new DecimalFormat("0.00");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(layout.quote);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    Quote quote = (Quote) getIntent().getSerializableExtra(EXTRA_QUOTE.name());

    ViewFinder finder = new ViewFinder(this);
    TextView buyPriceText = finder.find(id.tv_buy_price);
    TextView sellPriceText = finder.find(id.tv_sell_price);
    TextView netLabel = finder.find(id.tv_net_label);
    TextView netText = finder.find(id.tv_net);

    float netAmount = quote.getNet();
    float dollars = FloatMath.floor(Math.abs(netAmount) + 0.5F);
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
    buyPriceText.setText("$ " + decimalFormat.format(quote.buyPrice));
    sellPriceText.setText("$ " + decimalFormat.format(quote.sellPrice));
    netText.setText(netTextValue);
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

  private int getColor(final int id) {
    return getResources().getColor(id);
  }
}
