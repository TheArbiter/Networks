/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Reader {

	public static void main(String[] args) throws Exception {
		
		String serverName = "localhost";
		String mode;
		String userName;
		int serverPort = 6789;
		int pollingInterval;
		TCP tcp;

		//check args length
		if (args.length != 5) {
			System.err.println("Invalid number of Arguments Entered\n");
			System.err.println("Usage: Reader [mode] [polling_interval] [userName] [serverName] [serverPort]\n");
			return;
		}

		//check for valid mode
		if (!args[0].toLowerCase().equals("pull") && !args[0].toLowerCase().equals("push")) {
			System.err.println("Invalid mode: [push|pull]\n");
			return;
		}
		
		//check if number entered
		try {
			Integer.parseInt(args[1]);
		}
		catch (NumberFormatException nfe) {
			System.err.println("Polling Interval is not a number\n");
			return;
		}
		
		// check if number entered
		try {
			Integer.parseInt(args[4]);
		} 
		catch (NumberFormatException nfe) {
			System.err.println("Server Port is not a number\n");
			return;
		}

		// input the args
		mode = args[0];
		pollingInterval = Integer.parseInt(args[1]);
		userName = args[2];
		serverName = args[3]; // get server address
		serverPort = Integer.parseInt(args[4]); // get server port
		
		//open tcp connection
		try {
			// create socket which connects to server
			tcp = new TCP(serverName, serverPort);		
		} catch (Exception e) {
			System.err.println("Could not connect to designated server\n");
			return;
		}
		
		//push mode uses
		List<DiscussionPost> post = new ArrayList<DiscussionPost>();
		// write to server
		tcp.outToServer.writeBytes(userName + '\n');	//writes username to server for server log
		tcp.outToServer.writeBytes(mode + '\n');		//writes client operating mode, for server to process
		tcp.outToServer.flush();
		String sentenceFromServer = tcp.inFromServer.readLine();
		// print output
		System.out.println("From Server: " + sentenceFromServer);

		//both modes use 
		//initialize everything 
		String sentence; // get input from keyboard
		PageNew pageNew = new PageNew();
		Timer time = new Timer();
		List<Integer> readPosts = new ArrayList<Integer>();
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		
		//##Initialization finished##
		System.out.printf("Operating in %s mode\n", mode);

		try {
			if (mode.equals("push")) {
				int pushSize = Integer.parseInt(tcp.inFromServer.readLine());
				System.out.println("downloading " + pushSize + " posts");
				
				for (int i = 0; i < pushSize; i++) {
					String sentencefromServer = tcp.inFromServer.readLine();
					String[] components = sentencefromServer.split("\\|");
					//Serial Number, User name, Book Name, Page Number, Line Number, Post
					post.add(new DiscussionPost(Integer.parseInt(components[0]), components[1], components[2], 
							Integer.parseInt(components[3]), Integer.parseInt(components[4]), components[5]));
				}
				
				System.out.println("finished initialising posts db");
			}
		} 
		catch (Exception e) {
			System.out.println("Error during posts initialisation, shutting down, please restart client");
		}
		
		//after initialise for push, now can listen
		time.schedule(new Time(tcp, pageNew, post, readPosts, mode, userName, pollingInterval), 0);
		
		//enter input
		//loop for client input
		while ((sentence = inFromUser.readLine()) != null ) {					
			StringBuilder errorMessage = new StringBuilder();
			//request object is created with the input string
			Request request = createRequest(sentence, userName, pageNew.currentPage, 
					errorMessage, readPosts);
			if (request == null) {
				//if input string is invalid, createRequest returns null
				System.err.println(errorMessage.toString());
			} else {
				// if reading posts in push mode
				if (mode.equals("push") && request instanceof ReadRequest) { 
					Response response = ((ReadRequest) request).localProcess(post);
					System.out.println(response.toString());
				} else {
					//if request creation is successful, write and wait for response object
					tcp.writeToStream(request);
					//all returning objects are received by the time thread
				}
			}
		}
		
//		InetAddress serverIPAddress = InetAddress.getByName(serverName);

		
		
//		Socket clientSocket = new Socket(serverIPAddress, serverPort);

		// close client socket
//		clientSocket.close();

	} // end of main
	
	private static class PageNew {
		private Page currentPage = null;
	}

	private static class Time extends TimerTask {

		private TCP tcp;
		private PageNew pageNew;
		private List<DiscussionPost> post;
		private List<Integer> readPosts;
		private String mode;
		private String userName;
		private int pollingInterval;
		private Timer pingThread;
		private boolean pingNotNotified;

		private Time (TCP tcp, PageNew pageNew, List<DiscussionPost> post, List<Integer> readPosts, 
				String mode, String userName, int pollingInterval) {
			this.tcp = tcp;
			this.pageNew = pageNew;
			this.post = post;
			this.readPosts = readPosts;
			this.mode = mode;
			this.userName = userName;
			this.pollingInterval = pollingInterval;
			this.pingThread = null;
			this.pingNotNotified = true;
		}

		@Override
		public void run() {
			Object response = null;
			try {
				while ((response = tcp.objectsInFromServer.readObject()) != null ) {
					if (response instanceof DisplayResponse) {
						//update the last visited page
						pageNew.currentPage = ((DisplayResponse) response).getPage();
						if (mode.equals("push")) {
							System.out.println(((DisplayResponse) response).toString(pageNew.currentPage.getBookName(), 
									pageNew.currentPage.getPageNumber(), post, readPosts));
						}
						if (mode.equals("pull")) {
							System.out.println(((DisplayResponse) response).toString());
							if (pingThread != null) {
								pingThread.cancel();
							}
							pingThread = new Timer();
							pingThread.scheduleAtFixedRate(new Ping(tcp, userName, pageNew.currentPage.getBookName(),
									pageNew.currentPage.getPageNumber(), readPosts), pollingInterval * 1000, pollingInterval * 1000);
						}
					} 
					else if (response instanceof PushNotification) {
						((PushNotification) response).updateLocalDB(post, readPosts);
						if (this.pageNew.currentPage != null) {
							System.out.print(((PushNotification) response).toString(pageNew.currentPage.getBookName(),
									pageNew.currentPage.getPageNumber()));
						}
					}
					else if (response instanceof PingResponse) {
						if (((PingResponse) response).getStatus() == true && this.pingNotNotified) {
							System.out.println(response.toString());
							this.pingNotNotified = false;
						}
						else if (((PingResponse) response).getStatus() == false ) {
							this.pingNotNotified = true;
						}
					} else {
						//any errors on server side would return response object messageResponse with error description
						System.out.println(response.toString());
					}
				}
			} 
			catch (Exception e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			}
		}
	}

	private static class Ping extends TimerTask {

		private TCP tcp;
		private String userName;
		private String bookName;
		private int pageNumber;
		private List<Integer> readPosts;

		private Ping (TCP tcp, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
			this.tcp = tcp;
			this.userName = userName;
			this.bookName = bookName;
			this.pageNumber = pageNumber;
			this.readPosts = readPosts;
		}

		@Override
		public void run() {
			try {
				tcp.writeToStream(new PingRequest(this.userName + " pings " + this.bookName + " " + this.pageNumber, 
						this.userName, this.bookName, this.pageNumber, this.readPosts));
				//the ping response is received by the server listener thread
			} 
			catch (Exception e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			}
		}
	}

	private static class TCP {
		private InetAddress serverIPAddress;
		private Socket clientSocket;
		private DataOutputStream outToServer;
		private BufferedReader inFromServer;
		private ObjectOutputStream objectsOutToServer;
		private ObjectInputStream objectsInFromServer;

		TCP (String serverName, int serverPort) throws IOException {
			this.serverIPAddress = InetAddress.getByName(serverName);
			this.clientSocket = new Socket(this.serverIPAddress, serverPort);
			this.outToServer = new DataOutputStream(this.clientSocket.getOutputStream());
			this.objectsOutToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			this.inFromServer = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			this.objectsInFromServer = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
		}

		private synchronized void writeToStream (Object request) throws IOException {
			this.objectsOutToServer.writeObject(request);
			this.objectsOutToServer.flush();
		}
	}

	private static Request createRequest (String clientRequest, String userName, Page mostRecentPage, StringBuilder errorMessage, List<Integer> readPosts) {
		clientRequest = clientRequest.replaceAll("^ *", "").toLowerCase();
		Request request = null;
		try {
			if (clientRequest.startsWith("display")) {
				if (clientRequest.split(" ").length < 3) {
					errorMessage.append("Usage: display [bookName] [pageNumber]\n");
				} else {
					request = new DisplayRequest(userName+" requests a page", userName, clientRequest.split(" ")[1], Integer.parseInt(clientRequest.split(" ")[2]), readPosts);
				}
			}
			else if (clientRequest.startsWith("post_to_forum")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 3) {
					errorMessage.append("Usage: post_to_forum [lineNumber] [content]\n");
				} else {
					request = new PostRequest("New post received from " + userName, userName, 
							mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), 
							clientRequest.split(" ")[1], clientRequest.replaceAll("^\\w* \\w* ", ""));
				}
			} 
			else if (clientRequest.startsWith("read_post")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 2) {
					errorMessage.append("Usage: read_post [lineNumber]\n");
				} else {
					request = new ReadRequest(userName+" read posts", userName, 
							mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), 
							clientRequest.split(" ")[1], readPosts);
				}
			} else {
				errorMessage.append("Usage: (display [bookName] [pageNumber] | post_to_forum [lineNumber] [content])\n");
			}
		} catch (NumberFormatException nfe) {
			errorMessage.append("invalid Number inputed somewhere!\n");
		}
		return request;
	}
} // end of class TCPClient

