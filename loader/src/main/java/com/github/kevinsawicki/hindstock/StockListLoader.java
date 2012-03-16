package com.github.kevinsawicki.hindstock;

import com.github.kevinsawicki.stocks.StockQuoteRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 
 */
public class StockListLoader {

	private final StockQuoteRequest request;

	private final BufferedReader reader;

	private final GregorianCalendar startDate;

	private final GregorianCalendar endDate;

	private final PrintWriter writer;

	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	private int year;

	public StockListLoader(final File output, final InputStream stream)
			throws IOException {
		reader = new BufferedReader(new InputStreamReader(stream), 8192);
		reader.readLine();
		writer = new PrintWriter(output);
		request = new StockQuoteRequest();
		startDate = new GregorianCalendar();
		year = startDate.get(Calendar.YEAR);
		endDate = new GregorianCalendar();
	}

	private void resetDates(int year) {
		startDate.set(Calendar.YEAR, year);
		startDate.set(Calendar.DAY_OF_YEAR, 1);
		endDate.set(Calendar.YEAR, year + 1);
		startDate.set(Calendar.DAY_OF_YEAR, 1);
		startDate.add(Calendar.DAY_OF_YEAR, -1);
	}

	/**
	 * Release the resources held by this generator
	 */
	public void release() {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException ignored) {
				// Ignored
			}
		writer.close();
	}

	public boolean next() throws IOException {
		final String line = reader.readLine();
		if (line == null || line.length() == 0) {
			release();
			return false;
		}

		final int length = line.length();
		int start = 1;
		int quote = line.indexOf('"', start);
		int column = 0;
		String symbol = null;
		String name = null;
		while (start < length) {
			if (start + 1 != quote)
				switch (column++) {
				case 0:
					symbol = line.substring(start, quote);
					break;
				case 1:
					name = line.substring(start, quote);
					request.release().setSymbol(symbol);

					System.out.println("Fetching start sale of " + symbol
							+ " - " + name);
					Date saleStartDate = null;
					float price = 0;
					int currentYear = year;
					while (currentYear > 0) {
						resetDates(currentYear);
						request.setEndDate(endDate.getTime()).setStartDate(
								startDate.getTime());
						if (!request.next())
							break;
						saleStartDate = request.getDate();
						price = request.getOpen();
						while (request.next()) {
							saleStartDate = request.getDate();
							price = request.getOpen();
						}
						currentYear--;
					}
					if (saleStartDate != null) {
						writer.write('"' + symbol + "\",\"" + name + "\",\""
								+ '"' + format.format(saleStartDate) + "\",\""
								+ price + "\"\n");
					} else
						saleStartDate = null;
					return true;
				}
			start = quote + 1;
			quote = line.indexOf('"', start);
			if (quote == -1)
				quote = length;
		}
		return false;
	}

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		File file = new File(
				"/home/kevin/workspaces/github/StockListGenerator/src/main/resources/nasdaq-with-date.csv");
		if (file.exists())
			file.delete();
		StockListLoader gen = new StockListLoader(file,
				StockListLoader.class.getResourceAsStream("/nasdaq.csv"));
		try {
			while (gen.next())
				;
		} finally {
			gen.release();
			System.out.println("Took "
					+ ((System.currentTimeMillis() - start) / 1000)
					+ " seconds");

		}
	}
}
