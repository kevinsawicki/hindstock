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

import java.io.Serializable;

/**
 * Quote containing price and share amount
 */
public class Quote implements Serializable {

  private static final long serialVersionUID = -5079891859032286157L;

  /**
   * Share price bought at
   */
  public final float buyPrice;

  /**
   * Share price sold at
   */
  public final float sellPrice;

  /**
   * Number of shares
   */
  public final float shares;

  /**
   * Create stock quote
   *
   * @param buyPrice
   * @param sellPrice
   * @param shares
   */
  public Quote(final float buyPrice, final float sellPrice, final float shares) {
    this.buyPrice = buyPrice;
    this.sellPrice = sellPrice;
    this.shares = shares;
  }

  /**
   * Get amount paid for shares
   *
   * @return purchase cost
   */
  public float getCost() {
    return buyPrice * shares;
  }

  /**
   * Get net process
   *
   * @return net amount
   */
  public float getNet() {
    return (sellPrice - buyPrice) * shares;
  }

  /**
   * Get return rate
   *
   * @return rate
   */
  public float getRate() {
    return (getNet() / getCost()) * 100F;
  }
}