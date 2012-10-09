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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

public class DemoConsole
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Console Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		
		Console console = new Console(Color.BLACK, Color.GREEN, 
				new Font(Font.MONOSPACED, Font.BOLD, 14), "$ ");
		console.setPreferredSize(new Dimension(800, 600));
		
		console.setCompletionSource(new DefaultCompletionSource("help", "list", "die", "dinosaurs"));
		console.setProcessor(new InputProcessor() {
			public void process(String text, Console console)
			{
				System.out.println("You typed: '" + text + "'");
			}
		});
		frame.add(console);
		
		frame.pack();
		frame.setVisible(true);
	}

}
