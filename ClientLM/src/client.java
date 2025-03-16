import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    private static String keyword = "bye";
    private static int port = 1234;

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

        System.out.println("Client chat to port " + port + "\n");

        Socket s;
        try {
            s = new Socket("localhost", port);
            System.out.println("Inicializing Client: OK!\n");
        } catch (IOException e) {
            System.out.println("Inicializing Client: " + e.getMessage() + "\n");
            return;
        }

        PrintWriter pr;
        try {
            pr = new PrintWriter(s.getOutputStream());
            System.out.println("Inicializing Chat: OK!\n");
        } catch (IOException e) {
            System.out.println("Inicializing Chat: " + e.getMessage() + "\n");
            return;
        }

        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        String str;
        while (true) {
            System.out.print("Client: ");
            str = sc.nextLine();

            if (str.equalsIgnoreCase(keyword) || str.equalsIgnoreCase("end")) {
                System.out.println("\nKeyword Detected!");
                break;
            }

            pr.println(str);
            pr.flush();

            str = bf.readLine();

            if (str == null) {
                System.out.println("\nKeyword Detected!");
                break;
            }

            System.out.println("\nServer: " + str + "\n");
        }

        try {
			sc.close();
			pr.close();
			in.close();
            bf.close();
            System.out.println("\nClosing Chat: OK");
        } catch (IOException e) {
            System.out.println("\nClosing Chat: " + e.getMessage() + "\n");
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
