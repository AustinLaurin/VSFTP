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

    //binary authentication array, zero indicates lack of authentication
    private static int[] authent    = {0, 0};
    private static String password  = "";

    //data transmission
    private static String line     = "";
    private static String command  = ""; 
  
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
            System.out.println("S: Hello from VSFTP Service");
  
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

                    if(line.equals("DONE"))
                        break;

                    //initial authentication sequence
                    if(command.equals("USER")){
                        if(USER(line.substring(5)))
                            authent[0] = 1;
                        continue;
                    }
                    else if(authent[0] == 0){
                        System.out.println("-You must enter a valid USER command");
                        continue;
                    } 
                    else if(command.equals("PASS")){
                        if(PASS(line.substring(5)))
                            authent[1] = 1;
                        continue;    
                    }  
                    else if(authent[1] == 0){
                        System.out.println("-You must enter a valid PASS command");
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
                    }
  
                } 
                catch(IOException i) 
                { 
                    System.out.println(i); 
                } 
            } 
            System.out.println("+Goodbye"); 
  
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
        String temppass = findUsers(argum);
        if(temppass.equals("")){
            System.out.println("-Invalid user-id, try again");
            return false;
        }   
        else{
            System.out.println("+User-id valid, send password");
            password = temppass;
            return true;
        }
    }

    //authenticates password, prints positive or negative and returns corresponding boolean
    private static boolean PASS(String argum){
        if(argum.equals(password)){
            System.out.println("! Logged in \n Password is ok and you can begin file transfers");
            return true;
        }
        else{
            System.out.println("-Wrong password, try again");
            return false;
        }
    }

    //lists the files in the root folder
    private static void LIST(){
        System.out.println("+root");
        java.io.File root  = new java.io.File("root");
        for(java.io.File f: root.listFiles())
            System.out.println(f.getName());
    }

    //deletes the file specified by client 
    private static void KILL(String filename){
        boolean deleted = false;
        java.io.File root = new java.io.File("root");
        for(java.io.File f: root.listFiles()){
            if(f.getName().equals(filename)){
                deleted = true;
                f.delete();
            }
        } 
        
        if(deleted)
            System.out.println("+" + filename + " deleted");
        else
            System.out.println("-Not deleted because file does not exist");    
   
    }

    private static void RETR(String fileName){
        try{
            java.io.File fileIfFound = null;

            java.io.File root = new java.io.File("root");
            for(java.io.File f: root.listFiles()) 
                if(f.getName().equals(fileName)) 
                    fileIfFound = f;
                                
            if(fileIfFound != null) {
                System.out.println("#" + fileIfFound.length());
                
                line = in.readUTF(); 
                System.out.println(line);
                command = line.substring(0,4);
                while(true) {
                if(command.equals("STOP")) {
                    System.out.println("+Ok, RETR aborted");
                    return;
                }
                else if(command.equals("SEND")) {
                    Scanner input = new Scanner(fileIfFound);
                    while(input.hasNextLine()) {
                        System.out.println(input.nextLine());
                    }
                    return;
                }
                else 
                    System.out.println("-Invalid input received");
                }

            }
            else {
                System.out.println("-File doesn't exist");
            }

            return;
        }
        catch (Exception e){
            System.out.println("-Houston, we have a problem");
        }
    }
  
    public static void main(String args[]) 
    { 
        Server server = new Server(50001); 
    } 
} 
