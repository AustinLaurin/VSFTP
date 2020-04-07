// A Java program for a Server 
import java.net.*; 
import java.io.*; 
import java.util.Scanner;
  
public class Server 
{ 
    //initialize socket and input stream 
    private Socket          socket   = null; 
    private ServerSocket    server   = null; 
    private static DataInputStream in       = null;
    private static DataOutputStream clientSendy = null; 

    //binary authentication array, zero indicates lack of authentication
    private static int[] authent    = {0, 0};
    private static String password  = "";

    //data transmission
    private static String line     = "";
    private static String command  = ""; 

    //let's say we said done after retrieve
    private static boolean doneInRetrieve = false;
  
    // constructor with port 
    public Server(int port) 
    { 
       
        try
        { 
            //SOCKET, BIND
            server = new ServerSocket(port); 
            System.out.println("S: Server started");
            //LISTEN
            System.out.println("S: (listening for connection)");
            //ACCEPT
            socket = server.accept(); 
            //sends output back to client
            clientSendy = new DataOutputStream(socket.getOutputStream());
            System.out.println("S: Hello from VSFTP Service");
            clientSendy.writeUTF("S: Hello from VSFTP Service");
  
            // takes input from the client socket 
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream())); 


  
            // reads message from client until "DONE" is sent 
            while (!line.equals("DONE")) 
            { 
                try
                { 
                    //converts client input to a string
                    line = in.readUTF(); 
                    System.out.println("C: " + line);
                    System.out.print("S: ");
                    command = line.substring(0,4);//extract first four letters

                    if(line.equals("DONE") || doneInRetrieve)
                        break;

                    //initial authentication sequence
                    if(command.equals("USER")){
                        if(USER(line.substring(5)))
                            authent[0] = 1;
                        continue;
                    }
                    else if(authent[0] == 0){
                        System.out.println("-You must enter a valid USER command");
                        clientSendy.writeUTF("-You must enter a valid USER command");
                        clientSendy.writeUTF("*");
                        continue;
                    } 
                    else if(command.equals("PASS")){
                        if(PASS(line.substring(5)))
                            authent[1] = 1;
                        continue;    
                    }  
                    else if(authent[1] == 0){
                        System.out.println("-You must enter a valid PASS command");
                        clientSendy.writeUTF("-You must enter a valid PASS command");
                        clientSendy.writeUTF("*");
                        continue;
                    }

                    //main commands switch case
                    switch(command){
                        case "LIST":
                            LIST();
                            break;
                        case "KILL":
                            KILL(line.substring(5));
                            break;
                        case "RETR":
                            RETR(line.substring(5));
                            break;
                        default:
                            System.out.println("-Invalid command");
                            clientSendy.writeUTF("-Invalid command");
                            clientSendy.writeUTF("*");             
                    }
  
                } 
                catch(IOException i) 
                { 
                    System.out.println(i); 
                } 
            } 
            System.out.println("+Goodbye");
            clientSendy.writeUTF("+Goodbye");
            clientSendy.writeUTF("*");
  
            // CLOSE
            socket.close(); 
            in.close(); 
        } 
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
    } 

    //searches for username in file and returns corresponding password
    private static String findUsers(String user){
        String  password = "";
        String  attempt  = "";
        File    data     = new File("users.txt");
        Scanner scan;

        try{
            scan = new Scanner(data);

            while(scan.hasNext()){
                attempt = scan.next(); //only reads user values
                if(attempt.equals(user)){
                    password = scan.next();
                    break;
                }
                else if (scan.hasNext())
                    scan.next(); //skips reading password
            }
        } catch (FileNotFoundException e){
            System.out.println("File was not found");
        }

        return password;
    }

    //authenticates username, prints positive or negative and returns corresponding boolean
    private static boolean USER(String argum){
        try{
            String temppass = findUsers(argum);
            if(temppass.equals("")){
                System.out.println("-Invalid user-id, try again");
                clientSendy.writeUTF("-Invalid user-id, try again");
                clientSendy.writeUTF("*");
                return false;
            }   
            else{
                System.out.println("+User-id valid, send password");
                clientSendy.writeUTF("+User-id valid, send password");
                clientSendy.writeUTF("*");
                password = temppass;
                return true;
            }
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
            return false;
        } 
    }

    //authenticates password, prints positive or negative and returns corresponding boolean
    private static boolean PASS(String argum){
        try{
            if(argum.equals(password)){
                System.out.println("! Logged in \n Password is ok and you can begin file transfers");
                clientSendy.writeUTF("! Logged in \n Password is ok and you can begin file transfers");
                clientSendy.writeUTF("*");
                return true;
            }
            else{
                System.out.println("-Wrong password, try again");
                clientSendy.writeUTF("-Wrong password, try again");
                clientSendy.writeUTF("*");
                return false;
            }
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
            return false;
        } 
    }

    //lists the files in the root folder
    private static void LIST(){
        try{
            System.out.println("+root");
            clientSendy.writeUTF("+root");
            java.io.File root  = new java.io.File("root");
            for(java.io.File f: root.listFiles()){
                System.out.println(f.getName());
                clientSendy.writeUTF(f.getName());
            }    
             clientSendy.writeUTF("*");
        }
        catch(IOException i) 
        { 
            System.out.println(i);
        } 
    }

    //deletes the file specified by client 
    private static void KILL(String filename){
        try{
            boolean deleted = false;
            java.io.File root = new java.io.File("root");
            for(java.io.File f: root.listFiles()){
                if(f.getName().equals(filename)){
                    deleted = true;
                    f.delete();
                }
            } 
            
            if(deleted){
                System.out.println("+" + filename + " deleted");
                clientSendy.writeUTF("+" + filename + " deleted");
                clientSendy.writeUTF("*");
            }    
            else{
                System.out.println("-Not deleted because file does not exist");
                clientSendy.writeUTF("-Not deleted because file does not exist");
                clientSendy.writeUTF("*");  
            }        
        }
        catch(IOException i) 
        { 
            System.out.println(i); 
        } 
    }

    private static void RETR(String fileName){
        try{
            String packet = "";
            java.io.File fileIfFound = null;

            java.io.File root = new java.io.File("root");
            for(java.io.File f: root.listFiles()) {
                if(f.getName().equals(fileName)) 
                    fileIfFound = f;
            }        
                                
            if(fileIfFound != null) {
                System.out.println("#" + fileIfFound.length());
                clientSendy.writeUTF("#" + fileIfFound.length());
                clientSendy.writeUTF("*"); 
                
                line = in.readUTF(); 
                System.out.println(line);
                command = line.substring(0,4);
                while(true) {
                    if(command.equals("STOP")) {
                        System.out.println("+Ok, RETR aborted");
                        clientSendy.writeUTF("+Ok, RETR aborted");
                        clientSendy.writeUTF("*"); 
                        return;
                    }
                    else if(command.equals("SEND")) {
                        Scanner input = new Scanner(fileIfFound);
                        while(input.hasNextLine()) {
                            packet = input.nextLine();
                            System.out.println(packet);
                            clientSendy.writeUTF(packet);
                        }
                        clientSendy.writeUTF("*"); 
                        input.close();
                        return;
                    }
                    else if(command.equals("DONE")) {
                        doneInRetrieve = true;
                        return;
                    }
                    else {
                        System.out.println("-Invalid input received");
                        clientSendy.writeUTF("-Invalid input received");
                        clientSendy.writeUTF("*"); 
                    }
                }
            }
            else {
                System.out.println("-File doesn't exist");
                clientSendy.writeUTF("-File doesn't exist");
                clientSendy.writeUTF("*"); 
            }

            return;
        }
        catch (Exception e){
            System.out.println("-Houston, we have a problem: " + e);; 
        }
    }
  
    public static void main(String args[]) 
    { 
        Server server = new Server(50001); 
    } 
} 
