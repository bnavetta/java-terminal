package com.bennavetta.jconsole;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

public class Console extends JTextPane implements KeyListener
{	
	private static final long serialVersionUID = -5260432287332359321L;
	
	private ConsoleDocument doc;
	
	private String prompt;
	
	private StringBuilder text = new StringBuilder();
	
	private InputProcessor processor = new NoOpInputProcessor();
	
	private CompletionSource completionSource = new NoOpCompletionSource();
	
	private MutableAttributeSet defaultStyle;

	public CompletionSource getCompletionSource() {
		return completionSource;
	}

	public void setCompletionSource(CompletionSource completionSource) {
		this.completionSource = completionSource;
	}

	
	public InputProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(InputProcessor processor) {
		this.processor = processor;
	}

	public Console(Color background, Color text, Font font, String prompt)
	{
		super();
		doc = new ConsoleDocument();
		setDocument(doc);
		
		setBackground(background);
		
		setCaretColor(text);
		addCaretListener(doc);
		doc.setCaret(getCaret());
		
		MutableAttributeSet attrs = getInputAttributes();
		StyleConstants.setFontFamily(attrs, font.getFamily());
		StyleConstants.setFontSize(attrs, font.getSize());
		StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
        StyleConstants.setForeground(attrs, text);
        getStyledDocument().setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
        defaultStyle = attrs;
        
        this.prompt = prompt;
		doc.write(prompt, defaultStyle);
        
		addKeyListener(this); //not the best way, but it works for now
	}
	
	public void write(String text)
	{
		doc.write(text, defaultStyle);
	}
	
	public void remove(int offset, int length)
	{
		try
		{
			getStyledDocument().remove(offset, length);
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ConsoleDocument getConsoleDocument()
	{
		return this.doc;
	}

	public void keyTyped(KeyEvent e)
	{
		if(e.getKeyChar() == '\t')
		{
			//don't append autocomplete tabs to the text buffer
			e.consume();
			return;
		}
		text.append(e.getKeyChar());
	}

	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_TAB)
		{
			e.consume();
			List<String> completions = completionSource.complete(text.toString().trim());
			if(completions == null || completions.isEmpty())
			{
				//no completions
				Toolkit.getDefaultToolkit().beep();
			}
			else if(completions.size() == 1) //only one match - print it
			{
				String toInsert = completions.get(0);
				toInsert = toInsert.substring(text.length());
				doc.writeUser(toInsert, defaultStyle);
				text.append(toInsert);
					//don't trigger processing because the user might not agree with the autocomplete
			}
			else
			{
				StringBuilder help = new StringBuilder();
				help.append('\n');
				for(String str : completions)
				{
					help.append(' ');
					help.append(str);
				}
				help.append("\n" + prompt + text.toString());
				doc.write(help.toString(), defaultStyle);
			}
		}
	}

	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			processor.process(text.toString().trim(), this); //trim to remove newlines
			text = new StringBuilder();
			doc.write(prompt, defaultStyle);
		}
	}
	
	private static class NoOpInputProcessor implements InputProcessor
	{
		public void process(String text, Console console) {}
	}
	
	private static class NoOpCompletionSource implements CompletionSource
	{
		public List<String> complete(String input)
		{
			return null;
		}
	}
}
