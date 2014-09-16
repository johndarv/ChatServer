package com.johndarv.chatproj.chatserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

import com.johndarv.chatproj.common.*;

public class ClientThread extends Thread {

	private int id;
	private Server server;
	private Socket socket;
	private String username;
	
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ClientThread(Server server, Socket socket, int id) {
		this.server = server;
		this.id = id;
		this.socket = socket;
		
		tryToCreateInputAndOutputStreams(socket);
	}
	
	public String Username;
	public int Id;
	
	public void run() {
		boolean keepGoing = true;
		
		while (keepGoing) {
			ChatMessage chatMessage = tryToReadInputStreamAsAChatMessage(inputStream);
			
			if (chatMessage.getType() == ChatMessage.ERROR) {
				break;
			}
			
			keepGoing = handleChatMessage(chatMessage);
		}
		
		server.removeAClient(id);
		this.close();
	}

	public void close() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {}
		
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {}
		
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {}
	}
	
	public boolean sendStringToClient(String message) {
		if (!socket.isConnected()) {
			close();
			return false;
		}
		else {
			try {
				outputStream.writeObject(message);
				return true;
			}
			catch (IOException ex) {
				server.getDisplayer().display(String.format("Exception trying to write to Output Stream: %s", ex.getMessage()));
				return false;
			}
		}
	}
	
	private void tryToCreateInputAndOutputStreams(Socket socket) {
		IDisplay displayer = server.getDisplayer();		
		displayer.display(String.format("Client thread %d trying to create Object Input/Output Streams", this.id));
		
		try {
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			
			username = tryToReadInputStreamAsAString(inputStream);
			displayer.display(String.format("%s just connected", username));			
		} catch (IOException ex) {
			displayer.display(String.format("Exception creating I/O Streams: %s", ex.getMessage()));
		}
	}
	
	private String tryToReadInputStreamAsAString(ObjectInputStream inputStream) {
		try	{
			return (String) inputStream.readObject();
		} catch (IOException ex) {
			server.getDisplayer().display(String.format("Exception reading input stream as a string: %s", ex.getMessage()));
		}
		catch (ClassNotFoundException ex) {
			server.getDisplayer().display(ex.getMessage());
		}
		
		return "";
	}
	
	private ChatMessage tryToReadInputStreamAsAChatMessage(ObjectInputStream inputStream) {
		try	{
			return (ChatMessage) inputStream.readObject();
		} catch (IOException ex) {
			server.getDisplayer().display(String.format("Exception reading input stream as a CM for user '%s': %s", username, ex.getMessage()));
		}
		catch (ClassNotFoundException ex) {
			server.getDisplayer().display(ex.getMessage());
		}
		
		return new ChatMessage(ChatMessage.ERROR, "");
	}
	
	// Returns whether to keep the client going
	private boolean handleChatMessage(ChatMessage chatMessage) {
		boolean keepGoing = true;
		
		switch (chatMessage.getType()) {
		case ChatMessage.MESSAGE:
			server.broadcast(String.format("%s: %s", username, chatMessage.getMessage()));
			break;
		case ChatMessage.LOGOUT:
			server.broadcast(String.format("'%s' has left the room.", username));
			server.getDisplayer().display(String.format("User '%s' has submitted LOGOUT request.", username));
			keepGoing = false;
			break;
		case ChatMessage.WHOISIN:
			String message = server.generateListOfUsers();
			sendStringToClient(message);
			break;
		}
		
		return keepGoing;
	}
	
//	private Tuple<Boolean, String> tryToReadInputStreamAsString(ObjectInputStream inputStream) {
//		try	{
//			String str = (String) inputStream.readObject();
//			return new Tuple<Boolean, String>(true, str);
//		} catch (IOException ex) {
//			displayer.display(String.format("Exception creating I/O Streams: %s", ex.getMessage()));
//		}
//		catch (ClassNotFoundException ex) {
//			displayer.display(ex.getMessage());
//		}
//		
//		return new Tuple<Boolean, String>(false, "");
//	}
}