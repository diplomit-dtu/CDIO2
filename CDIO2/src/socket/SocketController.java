package socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController {
	Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	//TODO Maybe add some way to keep track of multiple connections?
	private BufferedReader inStream;
	private DataOutputStream outStream;


	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void sendMessage(SocketOutMessage message) {
		if (outStream!=null){
			//TODO send something over the socket! 
		} else {
			//TODO maybe tell someone that connection is closed?
		}
	}

	@Override
	public void run() {
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port)){ 
			while (true){
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e1) {
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 


	}

	private void waitForConnections(ServerSocket listeningSocket) {
		System.out.println("Server opened, waiting ...");
		try {
			Socket activeSocket = listeningSocket.accept(); //Blocking call
			inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
			outStream = new DataOutputStream(activeSocket.getOutputStream());
			String inLine;
			//.readLine is a blocking call 
			//TODO How do you handle simultaneous input and output on socket?
			//TODO this only allows for one open connection - how would you handle multiple connections?
			System.out.println(activeSocket.getRemoteSocketAddress()+"> Client connected to the server");
			while (true){
				inLine = inStream.readLine();
				System.out.println(inLine);
				if (inLine==null) break;
				String[] commandLine = inLine.split(" ", 2);
				String cmd=commandLine[0];
				String responseType = "";

				switch (cmd) {
				case "RM20": // Display a message in the secondary display and wait for response
					//TODO implement logic for RM command

					//Special Type
					responseType = null;

					break;
				case "D":// Display a message in the primary display
					//TODO Refactor to make sure that faulty messages doesn't break the system
					if( inLine.split(" ").length > 1 && !(commandLine[1].isEmpty())){

						String displayMessage = commandLine[1].replace('"', '#');
						displayMessage = displayMessage.split("#")[1];

						System.out.println("The GUI shall display : " + '"' + displayMessage + '"');

						notifyObservers(new SocketInMessage(SocketMessageType.D, displayMessage));
						responseType = " A";
					}else{
						responseType = " ES";
					}
					break;
				case "DW": //Clear primary display
					//TODO implement
					notifyObservers(new SocketInMessage(SocketMessageType.DW, ""));

					responseType = " A";

					break;
				case "P111": //Show something in secondary display
					//TODO implement

					if(inLine.split(" ").length > 1 && !(commandLine[1].isEmpty()) 
							&& commandLine[1].length() <= 32){

						String displayMessage = commandLine[1].split("\"")[1];
						//displayMessage = displayMessage.split("#")[1];

						System.out.println("The GUI shall display : " + '"' + displayMessage + '"');

						notifyObservers(new SocketInMessage(SocketMessageType.P111, displayMessage));
						responseType = " A";
					}else{
						responseType = " ES";
					}

					break;
				case "T": // Tare the weight
					//TODO implement
					notifyObservers(new SocketInMessage(SocketMessageType.T, ""));

					//Special type
					responseType = " S";

					break;
				case "S": // Request the current load
					//TODO implement
					notifyObservers(new SocketInMessage(SocketMessageType.S, ""));

					//Special type
					responseType = " S";

					break;
				case "K":
					responseType = " A";

					if (inLine.split(" ").length>1){
						notifyObservers(new SocketInMessage(SocketMessageType.K, inLine.split(" ")[1]));
					}
					break;
				case "B": // Set the load
					//TODO implement
					cmd = "D";
					responseType = " B";

					break;
				case "Q": // Quit
					responseType=" A";
					//TODO implement
					break;
				default: //Something went wrong?
					//TODO implement
					responseType = " ES";
					break;
				}
				sendResponse(cmd + responseType);
			}
		} catch (IOException e) {
			//TODO maybe notify mainController?
			e.printStackTrace();
		}
	}

	private void notifyObservers(SocketInMessage message) {
		for (ISocketObserver socketObserver : observers) {
			socketObserver.notify(message);
		}
	}


	private void sendResponse(String message) throws IOException{
		outStream.writeBytes(message + "\n");
	}

}

