/*
 *
 * tcpServer from Kurose and Ross
 *
 * Usage: java TCPServer [server port]
 */

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Vector;

public class Server {

	private static ServerSocket welcomeSocket;

	public static void main(String[] args)throws Exception {


		// see if we do not use default server port
		int serverPort = 6789; 
		/* change above port number this if required */
		
		if (args.length >= 1)
		    serverPort = Integer.parseInt(args[0]);
	    try{
			welcomeSocket = new ServerSocket(serverPort);
			System.out.printf("The server is listening on port number [%d]\n",welcomeSocket.getLocalPort());
			
			Database db = new Database();
			List<TCP> clients = new Vector<TCP>();
//			System.out.println("Ready for clients");
			while (true){
				new TCP(welcomeSocket.accept(),db,clients).start();
//			    // accept connection from connection queue
//			    Socket connectionSocket = welcomeSocket.accept();
//			    System.out.println("connection from " + connectionSocket);
//	
//			    // create read stream to get input
//			    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//			    String clientSentence;
//			    clientSentence = inFromClient.readLine();
//	
//			    // process input
//			    String capitalizedSentence;
//			    capitalizedSentence = clientSentence.toUpperCase() + '\n';
//	
//			    // send reply
//			    DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
//			    outToClient.writeBytes(capitalizedSentence);
	
			} // end of while (true)
	    }
	    catch (BindException e) { 
	    	System.err.println("Error in setting up connection. Server on port: " + serverPort + "maybe in use");
	    }
	} // end of main()
	
	private static class TCP extends Thread {
		
		private Socket connectionSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;
		private ObjectOutputStream objectsOutToClient;
		private ObjectInputStream objectsInFromClient;
		private volatile Database db;
		private volatile List<TCP> clients;
		private String userName;
		private String mode;
		
		private TCP (Socket connectionSocket, Database db, List<TCP> clients) throws IOException {
			this.connectionSocket = connectionSocket;
			this.inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
			this.outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
			this.objectsOutToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
			this.objectsInFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			this.db = db;
			this.clients = clients;
			this.userName = null;
			this.mode = null;
		}
		
		public void run(){
			try {
				this.userName = this.inFromClient.readLine();
				this.mode = this.inFromClient.readLine();
				System.out.println("User " + userName + " connected in " + mode + " mode");
				this.outToClient.writeBytes("Connection Established\n");
				this.outToClient.flush();
				
				if(mode.equals("push")){
					System.out.println(this.userName + "has been added to the push list");
					//add the client to the push list
					this.clients.add(this);
					// Must download all the post since push method
					int pushSize = db.getAllDiscussionPosts().size();
					this.outToClient.writeBytes(pushSize + "\n");
					for(Discussions post : db.getAllDiscussionPosts()){
						String data = post.getSerialNum() + "|" + post.getUserName() + "|" + post.getBookName() 
							+ "|" + post.getPageNumber() + "|" + post.getLineNumber() + "|" + post.getPost();
						//Send each post as a string
						this.outToClient.writeBytes(data + "\n");
					}
					System.out.println("Finished push Initialisation");
				}
			}
			catch (Exception e) {
				System.err.println("Failed to establish conenction with client");
				return;
			}
			
			while(this.connectionSocket.isConnected()){
				try{
					Object req = this.objectsInFromClient.readObject();
					
					if(req instanceof Request){
						Response res = null;
						
						System.out.println(((Request) req).getCommand());
						
						//requests process on their own and create their own response object
						res = ((Request) req).process(db, mode);

						//reply to client with the response
						this.objectsOutToClient.writeObject(res);
						this.objectsOutToClient.flush();

						if (req instanceof RequestThePosts && res.toString().equals("Post Successful")) {
							if (clients.size() == 0) {
								System.out.println("Push list empty. No action required.");
							} else {
								System.out.println("Pushing posts onto post clients");
								for (TCP tcp : clients) {
									System.out.println("Pushing to: " + tcp.userName);
									tcp.objectsOutToClient.writeObject(new NotificationPushing(db.getMoreRecentPost()));
									tcp.objectsOutToClient.flush();
								}
							}
						}
					}
				}
				catch (Exception e){
					System.out.println(userName + " Disconnected");
					if (mode.equals("push")) {
						this.clients.remove(this);
						System.out.println("Removing this client from push list");
					}
					close();
					return;

				}
			}
		}
		
		private void close() {
			try {
				this.outToClient.close();
				this.inFromClient.close();
				this.objectsOutToClient.close();
				this.objectsInFromClient.close();
				this.connectionSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}// end of class TCPServer


/* Notes
	Have to init a database to store content of post, line number,book name , page number and Username
*/