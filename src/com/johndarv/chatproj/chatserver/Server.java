package com.johndarv.chatproj.chatserver;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.johndarv.chatproj.common.*;

public class Server {

	private int port;
	private SimpleDateFormat dateFormat;
	private boolean keepGoing;
	private ArrayList<ClientThread> clientThreads;
	private ServerSocket serverSocket;
	private IDisplay displayer;
	
	public Server(IDisplay program) {
		this(program, 56789);
	}
	
	public Server(IDisplay displayer, int port) {
		this.displayer = displayer;
		this.port = port;
		this.dateFormat = new SimpleDateFormat("HH:mm:ss");
		this.clientThreads = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException ex) {
			this.keepGoing = false;
			displayer.display(String.format("Threw IO Exception: %s", ex.getMessage()));
		}
		
		while (this.keepGoing)
		{
			listenForClients();
		}
		
		shutDown();
	}

	public void stop() {
		keepGoing = false;
	}
	
	public synchronized void broadcast(String message) {
		String messageWithTime = displayMessageWithTime(message);
		
		for (ClientThread clientThread : clientThreads) {
			if (!clientThread.sendStringToClient(messageWithTime)) {
				displayer.display(String.format("Could not write to client: %s", clientThread.Username));
			}
		}
	}
	
	public String generateListOfUsers() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("List of users logged in at %s: ", dateFormat.format(new Date())));
		
		for (ClientThread clientThread : clientThreads) {
			stringBuilder.append(String.format("%s, ", clientThread.Username));
		}
		
		return stringBuilder.toString();
	}
	
	public IDisplay getDisplayer() {
		return displayer;
	}
	
	public synchronized void removeAClient(int id) {
		for(ClientThread clientThread : clientThreads) {
			if (clientThread.Id == id) {
				clientThreads.remove(clientThread);
				break;
			}
		}
	}

	private String displayMessageWithTime(String message) {
		String time = this.dateFormat.format(new Date());
		
		String messageWithTime = String.format("%s: %s", time, message);
		
		displayer.display(messageWithTime);
		
		return messageWithTime;
	}
	
	private void shutDown() {
		try {
			displayer.display("Server is shutting down...");
			serverSocket.close();
			
			for (ClientThread clientThread : this.clientThreads) {
				clientThread.close();
			}
		} catch (IOException ex) {
			displayer.display(String.format("Threw IO Exception: %s", ex.getMessage()));
		}
	}

	private void listenForClients() {		
		displayer.display(String.format("Server is running on port %d", this.port));
		
		Socket clientSocket;
		
		try {
			clientSocket = serverSocket.accept();
			
			ClientThread thread = new ClientThread(this, clientSocket, clientThreads.size() + 1);
			
			this.clientThreads.add(thread);
			
			thread.start();
		} catch (IOException ex) {
			this.keepGoing = false;
			displayer.display(String.format("Threw IO Exception: %s", ex.getMessage()));
		}
	}
}