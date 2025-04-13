/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api;

/**
 * Exception thrown when there is an error parsing a directive.
 */
public class DirectiveParsingException extends Exception {
  private String directive;

  /**
   * Constructor.
   *
   * @param directive Name of the directive
   * @param message Message associated with the exception
   */
  public DirectiveParsingException(String directive, String message) {
    super(message);
    this.directive = directive;
  }

  /**
   * Constructor.
   *
   * @param directive Name of the directive
   * @param message Message associated with the exception
   * @param cause Cause of the exception
   */
  public DirectiveParsingException(String directive, String message, Throwable cause) {
    super(message, cause);
    this.directive = directive;
  }

  /**
   * Returns the name of the directive.
   *
   * @return Name of the directive
   */
  public String getDirective() {
    return directive;
  }

  @Override
  public String getMessage() {
    return String.format("Error in directive '%s': %s", directive, super.getMessage());
  }
}