import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    private static String clientKeyword = "bye";
    private static int port = 1234;
    private static String serverKeyword;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("PORT_SERVIDOR: " + port);
        System.out.println("PARAULA_CLAU_CLIENT: " + clientKeyword);

        System.out.println("\nClient chat to port " + port);

        Socket s;
        try {
            s = new Socket("localhost", port);
            System.out.println("\nInicializing Client: OK");
        } catch (IOException e) {
            System.out.println("\nInicializing Client: " + e.getMessage());
            scanner.close();
            return;
        }

        System.out.println("\n");

        PrintWriter pr;
        try {
            pr = new PrintWriter(s.getOutputStream());
            System.out.println("\nInicializing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nInicializing Chat: " + e.getMessage());
            scanner.close();
            s.close();
            return;
        }

        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        pr.println(clientKeyword);
        pr.flush();
        serverKeyword = bf.readLine();

        String str;
        boolean breakLoop = true;
        while (breakLoop) {
            try {
                System.out.print("\nClient: ");
                str = scanner.nextLine();

                if (str.toLowerCase().contains(clientKeyword)) {
                    pr.println(str);
                    pr.flush();
                    System.out.println("\nClient Keyword Detected!");
                    breakLoop = false;
                } else if (str.toLowerCase().contains(serverKeyword)) {
                    pr.println(str);
                    pr.flush();
                    System.out.println("\nServer Keyword Detected!");
                    breakLoop = false;
                } else {
                    pr.println(str.trim());
                    pr.flush();
                    
                    str = bf.readLine();
                    System.out.println("\nServer: " + str);

                    if (str.toLowerCase().contains(clientKeyword)) {
                        System.out.println("\nClient Keyword Detected!");
                        breakLoop = false;
                    } else if (str.toLowerCase().contains(serverKeyword)) {
                        System.out.println("\nServer Keyword Detected!");
                        breakLoop = false;
                    }
                }
            } catch (IOException e) {
                System.out.println("\nError: " + e.getMessage());
                breakLoop = false;
            }
        }

        try {
			scanner.close();
			pr.close();
			in.close();
            bf.close();
            System.out.println("\nClosing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Chat: " + e.getMessage());
        }

        try {
            s.close();
            System.out.println("\nClosing Client: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Client: " + e.getMessage());
        }

		System.out.println("\nBye!");
    }
}
