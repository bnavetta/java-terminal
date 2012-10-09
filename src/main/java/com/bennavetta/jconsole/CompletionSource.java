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