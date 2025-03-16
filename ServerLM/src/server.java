import java.net.*;
import java.io.*;
import java.util.Scanner;

public class server {

    private static String keyword = "end";
    private static int port = 1234;

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

        System.out.println("Server chat at port " + port + "\n");
        System.out.println("Inicializing Server: OK!\n");

        ServerSocket ss = new ServerSocket(port);
        Socket s;

        try {
            s = ss.accept();
            System.out.println("Connection from Client: OK!\n");
        } catch (IOException e) {
            System.out.println("Connection from Client: " + e.getMessage() + "\n");
            return;
        }

        InputStreamReader in;
        try {
            in = new InputStreamReader(s.getInputStream());
            System.out.println("Inicializing Chat: OK");
        } catch (IOException e) {
            System.out.println("Inicializing Chat: " + e.getMessage());
            return;
        }

        BufferedReader bf = new BufferedReader(in);
        PrintWriter pr = new PrintWriter(s.getOutputStream());

        String str;
        while (true) {
            str = bf.readLine();

            if (str == null) {
                System.out.println("\nKeyword Detected!");
                break;
            }

            System.out.println("\nClient: " + str + "\n");

            System.out.print("Server: ");
            str = sc.nextLine();

            if (str.equalsIgnoreCase(keyword) || str.equalsIgnoreCase("bye")) {
                System.out.println("\nKeyword Detected!");
                break;
            }
			
            pr.println(str);
            pr.flush();
        }

        try {
			sc.close();
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
