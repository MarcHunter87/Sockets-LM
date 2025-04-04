import java.net.*;
import java.io.*;
import java.util.Scanner;

public class server {

    private static String serverKeyword = "end";
    private static int port = 1234;
    private static String clientKeyword;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("PORT_SERVIDOR: " + port);
        System.out.println("PARAULA_CLAU_SERVIDOR: " + serverKeyword + "\n");

        System.out.println("Server chat at port " + port + "\n");
        System.out.println("Inicializing Server: OK!\n");

        ServerSocket ss = new ServerSocket(port);
        Socket s;

        try {
            s = ss.accept();
            System.out.println("Connection from Client: OK!\n");
        } catch (IOException e) {
            System.out.println("Connection from Client: " + e.getMessage() + "\n");
            scanner.close();
            ss.close();
            return;
        }

        InputStreamReader in;
        try {
            in = new InputStreamReader(s.getInputStream());
            System.out.println("Inicializing Chat: OK");
        } catch (IOException e) {
            System.out.println("Inicializing Chat: " + e.getMessage());
            scanner.close();
            ss.close();
            s.close();
            return;
        }

        BufferedReader bf = new BufferedReader(in);
        PrintWriter pr = new PrintWriter(s.getOutputStream());

        clientKeyword = bf.readLine();
        pr.println(serverKeyword);
        pr.flush();

        String str;
        boolean breakLoop = true;
        while (breakLoop) {
            str = bf.readLine();

            if (str.toLowerCase().contains(serverKeyword)) {
                System.out.println("\nServer Keyword Detected!");
                breakLoop = false;
            } else if (str.toLowerCase().contains(clientKeyword)) {
                System.out.println("\nClient Keyword Detected!");
                breakLoop = false;
            }

            System.out.println("\nClient: " + str + "\n");

            System.out.print("Server: ");
            str = scanner.nextLine();

            if (str.toLowerCase().contains(serverKeyword)) {
                pr.println(serverKeyword);
                pr.flush();
                System.out.println("\nServer Keyword Detected!");
                breakLoop = false;
            } else if (str.toLowerCase().contains(clientKeyword)) {
                pr.println(clientKeyword);
                pr.flush();
                System.out.println("\nClient Keyword Detected!");
                breakLoop = false;
            }
            
            pr.println(str.trim());
            pr.flush();
        }

        try {
			scanner.close();
            in.close();
			bf.close();
            pr.close();
            System.out.println("\nClosing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Chat: " + e.getMessage() + "\n");
        }

        try {
			ss.close();
            s.close();
            System.out.println("\nClosing Server: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Server: " + e.getMessage());
        }
		
		System.out.println("\nBye!");
    }
}
