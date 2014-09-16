package com.johndarv.chatproj.chatserver;

import com.johndarv.chatproj.common.*;

public class ConsoleDisplay implements IDisplay {
	@Override
	public void display(String str) {
		System.out.println(str);
	}
}