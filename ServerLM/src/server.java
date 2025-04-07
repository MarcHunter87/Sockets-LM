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
        System.out.println("PARAULA_CLAU_SERVIDOR: " + serverKeyword);

        System.out.println("\nServer chat at port " + port);
        System.out.println("\nInicializing Server: OK");

        ServerSocket ss = new ServerSocket(port);
        Socket s;

        try {
            s = ss.accept();
            System.out.println("\nConnection from Client: OK");
        } catch (IOException e) {
            System.out.println("\nConnection from Client: " + e.getMessage());
            scanner.close();
            ss.close();
            return;
        }

        InputStreamReader in;
        try {
            in = new InputStreamReader(s.getInputStream());
            System.out.println("\nInicializing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nInicializing Chat: " + e.getMessage());
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
            try{
                str = bf.readLine();

                System.out.println("\nClient: " + str);

                if (str.toLowerCase().contains(serverKeyword)) {
                    System.out.println("\nServer Keyword Detected!");
                    breakLoop = false;
                } else if (str.toLowerCase().contains(clientKeyword)) {
                    System.out.println("\nClient Keyword Detected!");
                    breakLoop = false;
                } else {
                    System.out.print("\nServer: ");
                    str = scanner.nextLine();

                    if (str.toLowerCase().contains(serverKeyword)) {
                        pr.println(str);
                        pr.flush();
                        System.out.println("\nServer Keyword Detected!");
                        breakLoop = false;
                    } else if (str.toLowerCase().contains(clientKeyword)) {
                        pr.println(str);
                        pr.flush();
                        System.out.println("\nClient Keyword Detected!");
                        breakLoop = false;
                    } else {
                        pr.println(str.trim());
                        pr.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("\nError: " + e.getMessage());
                breakLoop = false;
            }
        }

        try {
			scanner.close();
            in.close();
			bf.close();
            pr.close();
            System.out.println("\nClosing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Chat: " + e.getMessage());
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
