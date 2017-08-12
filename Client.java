import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.StringReader;

public class Client {

  private String host;
  private int port;

  public static void main(String[] args) throws UnknownHostException, IOException {
    new Client("127.0.0.1", 12345).run();
  }

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws UnknownHostException, IOException {
    // connect client to server
    Socket client = new Socket(host, port);
    System.out.println("Client successfully connected to server!");

    // Get Socket output stream (where the client send her mesg)
    PrintStream output = new PrintStream(client.getOutputStream());

    // ask for a nickname
    Scanner sc = new Scanner(System.in);
    System.out.print("Enter a nickname: ");
    String nickname = sc.nextLine();

    // send nickname to server
    output.println(nickname);

    // create a new thread for server messages handling
    new Thread(new ReceivedMessagesHandler(client.getInputStream())).start();

    // read messages from keyboard and send to server
    System.out.println("Messages: \n");

    // while new messages
    while (sc.hasNextLine()) {
      output.println(sc.nextLine());
    }

    // end ctrl D
    output.close();
    sc.close();
    client.close();
  }
}

class ReceivedMessagesHandler implements Runnable {

  private InputStream server;

  public ReceivedMessagesHandler(InputStream server) {
    this.server = server;
  }

  public void run() {
    // receive server messages and print out to screen
    Scanner s = new Scanner(server);
    String tmp = "";
    while (s.hasNextLine()) {
      tmp = s.nextLine();
      if (tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length()-1);
        System.out.println(
            "\nUSERS LIST: " +
            new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n"
            );
      }else{
        try {
          System.out.println("\n" + getTagValue(tmp));
          // System.out.println(tmp);
        } catch(Exception ignore){}
      }
    }
    s.close();
  }

  // I could use a javax.xml.parsers but the goal of Client.java is to keep everything tight and simple
  public static String getTagValue(String xml){
    return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
  }

}
