package com.johndarv.chatproj.chatserver;

import com.johndarv.chatproj.common.*;

public class Program {

	public static void main(String[] args) {
		IDisplay displayer = new ConsoleDisplay();
		Server server = new Server(displayer);
		server.start();
	}
}
