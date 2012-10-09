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

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;

public class ConsoleDocument extends DefaultStyledDocument implements CaretListener
{
	private Caret caret;
	
	private static final long serialVersionUID = -1270788544217141905L;

	private int limit;

	public void write(String text, MutableAttributeSet attrs)
	{
		try
		{
			insertString(getLength(), text, attrs);
			limit = getLength();
			caret.setDot(limit);
		}
		catch(BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	public void writeUser(String text, MutableAttributeSet attrs)
	{
		try
		{
			insertString(getLength(), text, attrs);
			caret.setDot(getLength());
		}
		catch(BadLocationException e)
		{
			e.printStackTrace();
		}
	}
	
	public String getUserInput()
	{
		try
		{
			return getText(limit, getLength() - limit);
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void remove(int offs, int len) throws BadLocationException
	{
		if(offs < limit)
		{
			return;
		}
		super.remove(offs, len);
	}

	public void setCaret(Caret caret)
	{
		this.caret = caret;
	}
	
	public void caretUpdate(CaretEvent e)
	{
		if(e.getDot() < limit)
		{
			caret.setDot(limit);
		}
	}
	
}
