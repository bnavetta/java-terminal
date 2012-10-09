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
