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

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.junit.Test;


public class ConsoleTest {

	@Test
	public void test() throws InterruptedException
	{
		//TODO: This is a GUI test, so I'm not really sure how to assert things. I'll get to that at some point.
		Console console = new Console(Color.BLACK, Color.GREEN, new Font(Font.MONOSPACED, Font.PLAIN, 12), "> ");
		//...
		assertEquals(Color.GREEN, console.getCaretColor());
		assertNotNull(console.getConsoleDocument());
	}

	private static Object lock = new Object();
	private static JFrame frame = new JFrame("Console");
	
	public static void main(String[] args) throws Exception
	{
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Console console = new Console(Color.BLACK, Color.GREEN, new Font(Font.MONOSPACED, Font.BOLD, 14), "> ");
		frame.add(console);
		
		console.setProcessor(new InputProcessor(){
			public void process(String text, Console console)
			{
				System.out.println(text);
			}
		});
		console.setCompletionSource(new DefaultCompletionSource("ls", "cc", "git", "grep", "bash"));
		console.setPreferredSize(new Dimension(800, 600));
		frame.pack();
		frame.setVisible(true);
		
		Thread t = new Thread() {
			public void run()
			{
				synchronized(lock)
				{
					while(frame.isVisible())
					{
						try
						{
							lock.wait();
						}
						catch(InterruptedException e)
						{
							//ignore
						}
					}
					System.out.println("Exiting...");
					System.exit(0);
				}
			}
		};
		
		t.start();
		
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent evt)
			{
				synchronized(lock)
				{
					frame.setVisible(false);
					lock.notify();
				}
			}
		});
		
		t.join();
	}
}
