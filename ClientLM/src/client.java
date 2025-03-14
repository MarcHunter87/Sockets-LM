import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {
	
	private static String keyword = "bye";
	private static int port = 1234;
	
	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(System.in);
		String clientQuery;
		
		System.out.println("Client chat to port" + port + "\n");
		
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
		
		
		clientQuery = sc.nextLine();
		pr.println(clientQuery);
		pr.flush();
		
		InputStreamReader in = new InputStreamReader(s.getInputStream());
		BufferedReader bf = new BufferedReader(in);
		
		String str = bf.readLine();
		System.out.println("\nServer: " + str + "\n");
	}

}
