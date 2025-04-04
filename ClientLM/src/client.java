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
        System.out.println("PARAULA_CLAU_CLIENT: " + clientKeyword + "\n");

        System.out.println("Client chat to port " + port + "\n");

        Socket s;
        try {
            s = new Socket("localhost", port);
            System.out.println("Inicializing Client: OK!\n");
        } catch (IOException e) {
            System.out.println("Inicializing Client: " + e.getMessage() + "\n");
            scanner.close();
            return;
        }

        PrintWriter pr;
        try {
            pr = new PrintWriter(s.getOutputStream());
            System.out.println("Inicializing Chat: OK!\n");
        } catch (IOException e) {
            System.out.println("Inicializing Chat: " + e.getMessage() + "\n");
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
            System.out.print("Client: ");
            str = scanner.nextLine();

            if (str.toLowerCase().contains(clientKeyword)) {
                pr.println(clientKeyword);
                pr.flush();
                System.out.println("\nClient Keyword Detected!");
                breakLoop = false;
            } else if (str.toLowerCase().contains(serverKeyword)) {
                pr.println(serverKeyword);
                pr.flush();
                System.out.println("\nServer Keyword Detected!");
                breakLoop = false;
            } else {
                pr.println(str.trim());
                pr.flush();
                
                str = bf.readLine();
                if (str.toLowerCase().contains(clientKeyword)) {
                    System.out.println("\nClient Keyword Detected!");
                    breakLoop = false;
                } else if (str.toLowerCase().contains(serverKeyword)) {
                    System.out.println("\nServer Keyword Detected!");
                    breakLoop = false;
                } else {
                    System.out.println("\nServer: " + str);
                }
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
