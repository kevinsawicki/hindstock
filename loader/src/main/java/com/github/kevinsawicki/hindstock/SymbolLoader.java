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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;

/**
 * Loader of stock symbols
 */
public class SymbolLoader {

	private BufferedReader reader;

	private final File output;

	private final Map<String, String> symbols = new TreeMap<String, String>();

	private final Queue<File> files;

	/**
	 * Create loader to read symbols from given stream
	 *
	 * @param output
	 * @param directory
	 * @throws IOException
	 */
	public SymbolLoader(final File output, final File directory)
			throws IOException {
		File[] csvs = directory.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".csv");
			}
		});
		if (csvs == null || csvs.length == 0)
			throw new IllegalArgumentException("No .csv files in "
					+ directory.getAbsolutePath());

		files = new LinkedList<File>(Arrays.asList(csvs));
		this.output = output;
	}

	/**
	 * Writer the symbols and release the resources held by this loader
	 *
	 * @throws IOException
	 */
	public void finish() throws IOException {
		System.out.println("Symbols found: " + symbols.size());
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
		for (Entry<String, String> entry : symbols.entrySet())
			writer.append(entry.getKey()).append('\n').append(entry.getValue())
					.append('\n');
		if (reader != null)
			try {
				reader.close();
			} catch (IOException ignored) {
				// Ignored
			}
		writer.close();
	}

	private boolean advance() throws IOException {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException ignored) {
				// Ignored
			}
		if (files.isEmpty())
			return false;
		System.out.println("Processing: " + files.peek());
		reader = new BufferedReader(new FileReader(files.poll()), 8192);
		reader.readLine();
		return true;
	}

	private String normalizeName(final String name) {
		return name.trim().replace("&#39;", "'");
	}

	/**
	 * Read next stock line
	 *
	 * @return true if more lines, false otherwise
	 * @throws IOException
	 */
	public boolean next() throws IOException {
		if (reader == null && !advance())
			return false;

		String line = reader.readLine();
		while (line == null || line.length() == 0)
			if (advance())
				line = reader.readLine();
			else
				return false;

		final int length = line.length();
		int start = 1;
		int quote = line.indexOf('"', start);
		if (quote == -1)
			return advance();

		int column = 0;
		String symbol = null;
		String name = null;
		while (start < length) {
			switch (column++) {
			case 0:
				symbol = line.substring(start, quote);
				break;
			case 1:
				name = line.substring(start, quote);
				if (symbol.indexOf('^') == -1)
					symbols.put(symbol.toUpperCase(US), normalizeName(name));
				return true;
			}
			// Advance over closing quote, comma, and next open quote
			start = quote + 3;
			quote = line.indexOf('"', start);
			if (quote == -1)
				quote = length;
		}
		return advance();
	}

	/**
	 * Load all the CSV symbol files from a directory and output them to a
	 * single file.
	 * <p>
	 * First argument must be path to output file
	 * <p>
	 * Second argument must be directory containing one or more .csv files to
	 * import
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.err
					.println("First argument must be output file, second argument must be directory containing CSV files to import");
			return;
		}

		SymbolLoader loader = new SymbolLoader(new File(args[0]), new File(
				args[1]));
		while (loader.next())
			;
		loader.finish();
	}
}
