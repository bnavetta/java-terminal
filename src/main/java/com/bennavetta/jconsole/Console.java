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

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import java.awt.Component;

public class Console extends JTextPane implements KeyListener, MouseWheelListener, ComponentListener, MouseListener
{	
	private static final long serialVersionUID = -5260432287332359321L;
	
	private ConsoleDocument doc;		// Holder of all text on the window
	
	private int maxLinesPerScreen = 17; 	// This is default based on default window size.
	
	private String prompt;			// Structured as such: [connection][subprompt][prompt]
	private String subPrompt = "";		// Subprompt: one character at the end of the prompt
    	public String connection = "Home>";	// Computer name, IP, or Domain name (for use with networking)
    	private String path = "C:\\";		// Current Directory
	
	private final Font f;			// Keeps track of the current font for line counting.
	
    	private boolean wasInFocus = true;	// Used to focus the screen when typing/scrolling.
	
	private ArrayList<String> prompts = new ArrayList<String>();                // List of previously run commands
   	private ArrayList<String> DOCUMENT_HARDCOPY = new ArrayList<String>();      // List of all lines since last cls.
    	private String currentCommand = "";                                         // The current command being written, constantly being updated
    	private int currentPosition = 0;                                            // The line, as referenced in "DOCUMENT_HARDCOPY" that is at the top of the window
    	private int currentCommandnum = 0;                                          // The current command number, as referenced in "prompts," that the user is 
										    //  accessing, based on arrow keys.
	
	private InputProcessor processor = new NoOpInputProcessor();		    // Processor of input, as name implies.
	
	private CompletionSource completionSource = new NoOpCompletionSource();
	
	private MutableAttributeSet defaultStyle;

    /**
     * Sets the maximum number of lines that can fit on a window of the specified height.
     * Note that this is the height of the text area, not the entire window. (do not send
     * the raw window height, subtract the borders.)
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  heightInPixels   The height of the window, in pixels
     */
    public void setScreenHeight(int heightInPixels) { //in pixels
        FontMetrics fm = this.getFontMetrics(f);
        int height = fm.getMaxDescent(); //haha, getMaxDecent works too, but was quickly depricated!
        height += fm.getMaxAscent();
        maxLinesPerScreen = heightInPixels / height;
    }

    /**
     * Performs the smallest necessary amount of scrolling to move the "true cursor," or
     * current line being edited, onto the user's screen. Should be called anytime user
     * attempts to append the document, so they can see what they're doing.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     */
    public void focus() {
        //1. Is it already in focus?
        int MinimumFocusablePosition = DOCUMENT_HARDCOPY.size() - maxLinesPerScreen;
        int MaximumFocusablePosition = DOCUMENT_HARDCOPY.size() - 1;
        
        if ((MinimumFocusablePosition <= currentPosition)&&(currentPosition <= MaximumFocusablePosition))
            return;
        
        //2. Otherwise, set current position to a focusable location
        int scrollDistance = 0;
        if (MinimumFocusablePosition > currentPosition) {
            scrollDistance = MinimumFocusablePosition - currentPosition;
        } else if (MaximumFocusablePosition < currentPosition) {
            scrollDistance = MaximumFocusablePosition - currentPosition;
        }
        scroll(scrollDistance);
        wasInFocus = true;
    }
    
    /**
     * Class used internally, no need to understand it.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  lineNumber the line number to be checked
     * @returns boolean value of the comparison -> Is it in focus?
     */
    private boolean isInFocus(int lineNumber) {
        boolean toReturn = wasInFocus;
        wasInFocus = lineNumber < DOCUMENT_HARDCOPY.size();
        return toReturn;
    }
    
    /**
     * Scrolls the document by a specified number of lines, in a direction specified by 
     * positive/negative integer value. 
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  distance the number of lines to scroll
     */
    public void scroll(int distance) {
        currentPosition+=distance;
        if (isInFocus(currentPosition))
            currentCommand = doc.getUserInput();
        if (currentPosition < 0) currentPosition = 0;
        while (DOCUMENT_HARDCOPY.contains(""))
            DOCUMENT_HARDCOPY.remove("");
        if (DOCUMENT_HARDCOPY.size() < 1 || DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1).endsWith("\n")) {
            DOCUMENT_HARDCOPY.add(currentCommand);
        } else {
            DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,currentCommand);
        }

        doc = new ConsoleDocument();
        doc.setConsole(this);
        setDocument(doc);
        doc.setCaret(getCaret());
        
        doc.setFocusAfterAppend(false);
        for (int i = 0; i + currentPosition < DOCUMENT_HARDCOPY.size(); i++) {
            if (DOCUMENT_HARDCOPY.get(currentPosition + i).endsWith("\n"))
                this.write(DOCUMENT_HARDCOPY.get(currentPosition + i));
            else {
                this.write(prompt);
                doc.writeUser(DOCUMENT_HARDCOPY.get(currentPosition + i),defaultStyle);
            }
                
        }
        doc.setFocusAfterAppend(true);
    }
    
    
    
	public CompletionSource getCompletionSource() {
		return completionSource;
	}

	public void setCompletionSource(CompletionSource completionSource) {
		this.completionSource = completionSource;
	}

	
    /**
     * Sets the one-character subprompt of the console's prompting system, then updates the
     * entire prompt.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  prompt to become the new prompt
     */
    public void setPrompt(String prompt) {
        this.subPrompt = prompt;
        this.prompt = this.connection + this.path + this.subPrompt;
    }
    
    /**
     * Sets the directory filepath of the console's prompting system, then updates the
     * entire prompt.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @param  path to become the new path
     */
    public void setPath(String path) {
        this.path = path;
        this.prompt = this.connection + this.path + this.subPrompt;
    }
    
    /**
     * Returns the current path.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     * @returns the console's current filepath.
     */
    public String getPath() {
        return path;
    }
    
	public InputProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(InputProcessor processor) {
		this.processor = processor;
	}

	 public Console() { f = null; } //for debugging only

	public Console(Color background, Color text, Font font, String prompt)
	{
	    super();
        doc = new ConsoleDocument();
        doc.setConsole(this);
        setDocument(doc);
        
        
        DOCUMENT_HARDCOPY.add("");
        
        setBackground(background);
        
        setCaretColor(text);
        addCaretListener(doc);
        doc.setCaret(getCaret());
        
        f = font;
        MutableAttributeSet attrs = getInputAttributes();
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
        StyleConstants.setForeground(attrs, text);
        getStyledDocument().setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
        defaultStyle = attrs;
        
        
        this.prompt = this.connection + this.path + prompt;
        doc.write(this.prompt, defaultStyle);
        
        addKeyListener(this); //catch tabs, enters, and up/down arrows for autocomplete and input processing
        addMouseWheelListener(this);
        addMouseListener(this);
	}
	
    /**
     * "Clears" the terminal window...
     *          ...by replacing it with a new one - this was the easiest way to do it.
     *
     * @author Joey Patel
     * @author pateljo@northvilleschools.net (valid until 06/18)
     */
    public void cls() {
        doc = new ConsoleDocument();
        doc.setConsole(this);
        setDocument(doc);
        doc.setCaret(getCaret());
        DOCUMENT_HARDCOPY = new ArrayList<String>();
        DOCUMENT_HARDCOPY.add("");
        currentPosition = 0;
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
			//don't append autocomplete tabs to the document
			e.consume();
		}
	}

	public void keyPressed(KeyEvent e)
	{
		// Is the cursor in a valid position?
        if (!doc.isCursorValid())
            doc.makeCursorValid();
            
        // Is the screen focused on the proper line?
        focus();
            
        //TAB -> AUTOCOMPLETE
        if(e.getKeyCode() == KeyEvent.VK_TAB)
        {
            e.consume();
            String input = doc.getUserInput().trim();
            
            List<String> completions = completionSource.complete(input);
            if(completions == null || completions.isEmpty())
            {
                //no completions
                Toolkit.getDefaultToolkit().beep();
            }
            else if(completions.size() == 1) //only one match - print it
            {
                String toInsert = completions.get(0);
                toInsert = toInsert.substring(input.length());
                doc.writeUser(toInsert, defaultStyle);
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
                help.append("\n" + prompt);
                doc.write(help.toString(), defaultStyle);
                doc.writeUser(input, defaultStyle);
            }
        }
        
        //UP ARROW -> FILL IN A PREV COMMAND
        if (e.getKeyCode() == KeyEvent.VK_UP)
        {
            e.consume(); //Don't actually go up a row
            
            //Get current input
            String currentInput = doc.getUserInput().trim();

            //If there's no previous commands, beep and return
            if (currentCommandnum <= 0) {
                currentCommandnum = 0; //It should never be less than zero, but you never know...
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            //remove the current input from console and, if it's null, initialize it to an empty string.
            if (currentInput != null && currentInput != "") { //not sure which one it returns, but it doesn't really matter
                this.remove(doc.getLimit(),currentInput.length());
            } else {
                currentInput = "";                            //In case it's null... this may be unnecessary
            }
            
            //If it's something the user just typed, save it for later, just in case.
            if (currentCommandnum >= prompts.size()) {
                currentCommandnum = prompts.size();
                currentCommand = currentInput;      //save the current command, for down arrow use.
            }
            
            //move on to actually processing the command, now that all extraneous cases are taken care of.
            
            //based on previous checks, currentCommandnum should be in the range of 1 to prompts.size() before change.
            //after change, it should be in the range of 0 to (prompts.size() - 1), valid for indexing prompts.
            currentCommandnum--; //update command number. (lower num = older command)
            
            //Index prompts and write the replacement.
            String replacementCommand = prompts.get(currentCommandnum);
            doc.writeUser(replacementCommand,defaultStyle);
            
            //Similar to tab, don't trigger processing because the user might not agree with the autocomplete
        }
        
        //DOWN ARROW -> FILL IN A NEWER COMMAND
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            e.consume(); //pretty sure you can't go down, but if you can... don't.
            
            //If you've exhausted the list and replaced the line with the current command, beep and return
            if (currentCommandnum >= prompts.size()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            
            currentCommandnum++;
            
            //Now, regardless of where you are in the list of commands, you're going to need to replace text.
            String currentInput = doc.getUserInput().trim();
            if (currentInput != null && currentInput != "")  //not sure which one it returns, but it doesn't really matter
                this.remove(doc.getLimit(),currentInput.length());
            
            
            //If you've exhausted the list but not yet replaced the line with the current command...
            if (currentCommandnum == prompts.size()) {
                doc.writeUser(currentCommand,defaultStyle);
                return;
            }
            
            //If, for some reason, the list is not in range (lower bound), make it in range.
            if (currentCommandnum < 0) {
                currentCommandnum = 0;
            }
            
            //finally, write in the new command.
            doc.writeUser(prompts.get(currentCommandnum),defaultStyle);
        }
	}

	public void keyReleased(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,prompt + doc.getUserInput());
            if (!DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1).endsWith("\n"))
                DOCUMENT_HARDCOPY.set(DOCUMENT_HARDCOPY.size()-1,DOCUMENT_HARDCOPY.get(DOCUMENT_HARDCOPY.size()-1) + "\n");
            DOCUMENT_HARDCOPY.add("");
            String line = doc.getUserInput().trim();
            String[] args = parseLine(line);
            prompts.add(line);
            currentCommandnum = prompts.size();
            processor.process(args, this);
            doc.write(prompt, defaultStyle);
        }
	}
	
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.scroll(e.getWheelRotation() * 3);
    }
    
    public void componentHidden(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentResized(ComponentEvent evt) {
        this.setScreenHeight((int)(((JFrame)evt.getSource()).getContentPane().getSize().getHeight()));
    }
    
    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
    }
    public void mouseClicked(MouseEvent e) {}
    
    String mostRecentSelectedText = "";
    
    public void mouseReleased(MouseEvent e) {
        if (this.getSelectedText() != null) // See if they selected something 
            mostRecentSelectedText = this.getSelectedText();
        else
            mostRecentSelectedText = "";
        if (e.isPopupTrigger())
            doPop(e);
    }
    
    private void doPop(MouseEvent e){
        PopUp menu = new PopUp();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private class PopUp extends JPopupMenu {
        JMenuItem copyButton;
        public PopUp(){
            copyButton = new JMenuItem(new AbstractAction("copy") {
                public void actionPerformed(ActionEvent e) {
                    if(!mostRecentSelectedText.equals("")) {
                        StringSelection selection = new StringSelection(mostRecentSelectedText);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, selection);
                    }
                }
            });
            add(copyButton);
        }
    }

    private static String[] parseLine(String line)
    {
        List<String> args = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        char[] chars = line.toCharArray();
        boolean inQuotes = false;
        for (char c :chars)
        {
            if (c == '"')
            {
                if (current.length() > 0)
                {
                    args.add(current.toString());
                    current.setLength(0);
                }
                inQuotes = !inQuotes;
            }
            else if (inQuotes)
            {
                current.append(c);
            }
            else if (c == ' ')
            {
                if (current.length() > 0)
                {
                    args.add(current.toString());
                    current.setLength(0);
                }
            }
            else
            {
                current.append(c);
            }
        }

        args.add(current.toString().trim());

        return args.toArray(new String[0]);
    }
    
	private static class NoOpInputProcessor implements InputProcessor
	{
		public void process(String[] text, Console console) {}
	}
	
	private static class NoOpCompletionSource implements CompletionSource
	{
		public List<String> complete(String input)
		{
			return null;
		}
	}
}
