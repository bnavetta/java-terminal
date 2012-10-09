package com.bennavetta.jconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultCompletionSource extends CachingCompletionSource
{
	private List<String> terms;
	
	public DefaultCompletionSource(String... terms)
	{
		this(Arrays.asList(terms));
	}
	
	public DefaultCompletionSource(List<String> terms)
	{
		this.terms = terms;
	}
	
	@Override
	protected List<String> doCompletion(String input)
	{
		List<String> matches = new ArrayList<String>();
		for(String term : terms)
		{
			if(term.toLowerCase().startsWith(input.toLowerCase()))
			{
				matches.add(term);
			}
		}
		return matches;
	}

}
