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

import static java.util.Calendar.DAY_OF_YEAR;
import android.os.AsyncTask;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.github.kevinsawicki.stocks.StockQuoteRequest;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Request class to compute the gain/loss
 */
public abstract class GainLossRequest extends
		AsyncTask<StockQuoteRequest, Integer, float[]> {

	private final String symbol;

	private final float shares;

	private final float dollars;

	private final Calendar buyDate;

	private final Calendar sellDate;

	private IOException exception;

	/**
	 * Create request
	 * 
	 * @param symbol
	 * @param shares
	 * @param dollars
	 * @param buyDate
	 * @param sellDate
	 */
	public GainLossRequest(final String symbol, final float shares,
			final float dollars, final Calendar buyDate, final Calendar sellDate) {
		this.symbol = symbol;
		this.shares = shares;
		this.dollars = dollars;
		this.buyDate = buyDate;
		this.sellDate = sellDate;
	}

	/**
	 * @return exception
	 */
	public IOException getException() {
		return exception;
	}

	@Override
	protected float[] doInBackground(StockQuoteRequest... params) {
		exception = null;
		float[] prices = new float[params.length];
		for (int i = 0; i < params.length; i++)
			try {
				if (params[i].next())
					prices[i] = params[i].getClose();
			} catch (IOException e) {
				exception = e;
				return null;
			} catch (HttpRequestException e) {
				exception = e.getCause();
				return null;
			}
		return prices;
	}

	@Override
	protected void onPostExecute(final float[] result) {
		if (result == null)
			onFailure(exception);

		if (dollars > 0)
			onSuccess((result[1] * (dollars / result[0])) - dollars);
		else
			onSuccess(result[1] * shares - result[0] * shares);
	}

	/**
	 * Called after request completes and the net amount has been computed
	 * 
	 * @param netAmount
	 */
	protected abstract void onSuccess(float netAmount);

	/**
	 * Called when request fails providing the cause of the failure
	 * 
	 * @param cause
	 */
	protected abstract void onFailure(IOException cause);

	/**
	 * Execute request
	 */
	public void execute() {
		StockQuoteRequest buyRequest = new StockQuoteRequest();
		GregorianCalendar startBuyDate = new GregorianCalendar();
		startBuyDate.setTime(buyDate.getTime());
		startBuyDate.add(DAY_OF_YEAR, -7);
		buyRequest.setStartDate(startBuyDate).setEndDate(buyDate)
				.setSymbol(symbol);

		StockQuoteRequest sellRequest = new StockQuoteRequest();
		GregorianCalendar startSellDate = new GregorianCalendar();
		startSellDate.setTime(sellDate.getTime());
		startSellDate.add(DAY_OF_YEAR, -7);
		sellRequest.setStartDate(startSellDate).setEndDate(sellDate)
				.setSymbol(symbol);

		super.execute(buyRequest, sellRequest);
	}
}
