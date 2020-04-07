// A Java program for a Client 
import java.net.*; 
import java.io.*; 
import java.util.concurrent.TimeUnit;
  
public class Client 
{ 
    // initialize socket and input output streams 
    private static Socket socket            = null; 
    private static DataInputStream  input   = null; 
    private static DataOutputStream out     = null; 
    private static DataInputStream serverTalky = null;


    //file tranfer storage stuff
    private static String filename = "";

    // string to read message from input 
    private static String line = ""; 
    private static String command = "";
    private static String serverSpeak = "";
  
    // constructor to put ip address and port 
    public Client(String address, int port) 
    { 
        // establish a connection 
        try
        { 
            //SOCKET, CONNECT
            socket = new Socket(address, port); 
            System.out.println("(opens connection to R)"); 
  
            // takes input from terminal to know when it can talk again
            input  = new DataInputStream(System.in); 
  
            // sends output to the socket where the server hears it
            out    = new DataOutputStream(socket.getOutputStream()); 

            //hears the server SPEAKING
            serverTalky = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } 
        catch(UnknownHostException u) 
        { 
            System.out.println(u); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
  
        //read in intial greeting
        try{
            System.out.println(serverTalky.readUTF());
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 

  
        // keep reading until "DONE" is input 
        while (!line.equals("DONE")) 
        { 
            try
            { 
                System.out.print("C: ");
                line = input.readLine(); 
                out.writeUTF(line); 

                //reset Server speaking ability
                serverSpeak = "";

                //command string
                command = line.substring(0, 4);

                //file reading commands
                if(command.equals("RETR")){
                    filename = line.substring(5);
                }

                else if(command.equals("SEND")){
                    FileOutputStream junior = new FileOutputStream(filename);
                    junior.write("hewwo".getBytes());//TEST STRING, CHANGE LATER

                    File babyFile = new File(filename);

                    //timeout after 30 seconds
                    try{
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException ie){
                        Thread.currentThread().interrupt();
                    }

                    if(babyFile.length() == 0){
                        System.out.println("C: Connection Timed Out");
                        line = "DONE";
                        try{
                            out.writeUTF("DONE");
                        }catch(Exception e){
                            System.out.println(e);
                        }

                        junior.close();
                    }    

                }
                //listen to the server
                while(!(serverSpeak.equals("*"))){
                    serverSpeak = serverTalky.readUTF();
                    if(serverSpeak.equals("*"))
                    break;
                    System.out.println("S: " + serverSpeak);

                }

            }
            catch(IOException i) 
            { 
                System.out.println(i); 
            }

        } 
            // CLOSE
            try
            { 
                input.close(); 
                out.close(); 
                socket.close(); 
            } 
            catch(IOException i) 
            { 
                System.out.println(i); 
            } 
    } 
  

    public static void main(String args[]) 
    { 
        Client client = new Client("127.0.0.1", 50001); 
    } 
} 
