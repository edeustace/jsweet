/*
 * Copyright 2007 Google Inc.
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

import type { ClickEvent } from "@experiment/user-ts";
import {
  EntryPoint,
  ClickHandler,
  Window,
  Button,
  RootPanel,
} from "@experiment/user-ts";

/**
 * HelloWorld application.
 */
export class Hello implements EntryPoint {
  public onModuleLoad(): void {
    let b = new Button(
      "Click me",
      new (class implements ClickHandler {
        public onClick(event: ClickEvent): void {
          Window.alert("Hello, AJAX");
        }
      })(),
    );

    RootPanel.get().add(b);
  }
}
