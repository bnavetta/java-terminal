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
