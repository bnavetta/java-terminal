/**
 * Copyright (C) 2012 Ben Navetta <ben.navetta@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bennavetta.jconsole;

import java.util.List;

public interface CompletionSource
{
	/**
	 * Generate a list of possible completions for an input segment
	 * @param text the text to complete
	 * @return a list of completions. If the list is {@code null} or empty, then it is assumed that
	 * there is no possible completion (a bell will be sounded). If there is one item, then that item
	 * will be inserted. Otherwise, all completions will be shown.
	 */
	public List<String> complete(String text);
}