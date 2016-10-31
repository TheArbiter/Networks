import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Server to process ping requests over UDP. 
 * The server sits in an infinite loop listening for incoming UDP packets. 
 * When a packet comes in, the server simply sends the encapsulated data back to the client.
 */

public class Host
{
   private static final double LOSS_RATE = 0.3;
   private static final int AVERAGE_DELAY = 100;  // milliseconds

   public static void main(String[] args) throws Exception
   {
      // Get command line argument.
      if (args.length != 2) {
         System.out.println("Required arguments: port");
         return;
      }
	  String HostName = args[0];
	  int port = Integer.parseInt(args[1]);

	  
      // Create random number generator for use in simulating 
      // packet loss and network delay.
      Random random = new Random();

      // Create a datagram socket for receiving and sending UDP packets
      // through the port specified on the command line.
      DatagramSocket socket = new DatagramSocket(port);
	  InetAddress host = InetAddress.getByName(HostName);
      
	  int count = 0;
      // Processing loop.
      while (count < 10) {

		 long Time = System.currentTimeMillis();
		 String Message = "PING"+count+""+Time+"\r\n";
         // Create a datagram packet to hold incomming UDP packet.
         DatagramPacket request = new DatagramPacket(Message.getBytes(),Message.length(),host,port);

         // Block until the host receives a UDP packet.
         socket.send(request);
         
         // Print the recieved data.
         //printData(request);

		 DatagramPacket reply = new DatagramPacket(new byte[1024], 1024);
  			
		 // Wait for 1 second        
		 socket.setSoTimeout(1000);

/*		 // Decide whether to reply, or simulate packet loss.
         if (random.nextDouble() < LOSS_RATE) {
            System.out.println("   Reply not sent.");
            continue; 
         }
*/
		 try{
	     	socket.receive(reply);
		 }
		 catch(IOException E){

		 }
         // Simulate network delay.
         Thread.sleep(1000);
/*
         // Send reply.
         InetAddress clientHost = request.getAddress();
         int clientPort = request.getPort();
         byte[] buf = request.getData();
         DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
*/

        System.out.println("   Reply sent.");
		 count++;
      }
   }

   /* 
    * Print ping data to the standard output stream.
    */
   private static void printData(DatagramPacket request) throws Exception
   {
      // Obtain references to the packet's array of bytes.
      byte[] buf = request.getData();

      // Wrap the bytes in a byte array input stream,
      // so that you can read the data as a stream of bytes.
      ByteArrayInputStream bais = new ByteArrayInputStream(buf);

      // Wrap the byte array output stream in an input stream reader,
      // so you can read the data as a stream of characters.
      InputStreamReader isr = new InputStreamReader(bais);

      // Wrap the input stream reader in a bufferred reader,
      // so you can read the character data a line at a time.
      // (A line is a sequence of chars terminated by any combination of \r and \n.) 
      BufferedReader br = new BufferedReader(isr);

      // The message data is contained in a single line, so read this line.
      String line = br.readLine();

      // Print host address and data received from it.
      System.out.println(
         "Received from " + 
         request.getAddress().getHostAddress() + 
         ": " +
         new String(line) );
   }
}
