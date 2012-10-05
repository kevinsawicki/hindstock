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

import com.github.kevinsawicki.hindstock.GainLossRequest.Quote;

import android.content.Intent;

/**
 * Constants for use with {@link Intent}s
 */
public enum IntentConstant {

  /**
   * Handle to a {@link Quote}
   */
  EXTRA_QUOTE("quote"),

  /**
   * Handle to a {@link Stock}
   */
  EXTRA_STOCK("stock");

  private final String value;

  private IntentConstant(final String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
