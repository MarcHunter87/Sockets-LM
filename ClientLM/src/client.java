import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    private static String clientKeyword;
    private static int port = 1234;
    private static String serverKeyword;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Introduce una palabra clave: ");
        clientKeyword = scanner.nextLine();

        clientKeyword = clientKeyword.toLowerCase();
        
        System.out.println("\nPORT_SERVIDOR: " + port);
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

        PrintWriter pr;
        try {
            pr = new PrintWriter(s.getOutputStream());
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

        if ("SERVER_FULL".equals(serverKeyword)) {
            System.out.println("\nInicializing Chat: ERROR");
            System.out.println("\nThe server is full.");
            pr.close();
            in.close();
            bf.close();
            s.close();
            scanner.close();
            return;
        }

        System.out.println("\nInicializing Chat: OK");

        String str;
        boolean breakLoop = false;
        while (!breakLoop) {
            try {
                System.out.print("\nClient: ");
                str = scanner.nextLine();

                if (str.toLowerCase().contains(clientKeyword)) {
                    pr.println(str);
                    pr.flush();
                    System.out.println("\nClient Keyword Detected!");
                    breakLoop = true;
                } else {
                    pr.println(str.trim());
                    pr.flush();
                    
                    str = bf.readLine();
                    if (str == null) {
                        System.out.println("\nConexion ended by server");
                        breakLoop = true;
                        continue;
                    }
                    System.out.println("\nServer: " + str);

                    if (str.toLowerCase().contains(clientKeyword)) {
                        System.out.println("\nClient Keyword Detected!");
                        breakLoop = true;
                    } else if (str.toLowerCase().contains(serverKeyword)) {
                        System.out.println("\nServer Keyword Detected!");
                        breakLoop = true;
                    }
                }
            } catch (IOException e) {
                System.out.println("\nError: " + e.getMessage());
                breakLoop = true;
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
