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

/**
 * Stock model class
 */
public class Stock {

	/**
	 * Name of stock
	 */
	public final String name;

	/**
	 * Stock symbol
	 */
	public final String symbol;

	/**
	 * Create stock
	 *
	 * @param symbol
	 * @param name
	 */
	public Stock(final String symbol, final String name) {
		this.symbol = symbol;
		this.name = name;
	}

	public String toString() {
		return symbol;
	}

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}
}
