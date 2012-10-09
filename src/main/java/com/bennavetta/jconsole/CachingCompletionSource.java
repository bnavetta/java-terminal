package com.bennavetta.jconsole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CachingCompletionSource implements CompletionSource
{
	private Map<String, List<String>> completionCache = new HashMap<String, List<String>>();
	
	public List<String> complete(String text)
	{
		if(completionCache.containsKey(text))
		{
			return completionCache.get(text);
		}
		else
		{
			List<String> results = doCompletion(text);
			completionCache.put(text, results);
			return results;
		}
	}
	
	protected abstract List<String> doCompletion(String input);
}
