import java.net.*;
import java.io.*;
import java.util.Scanner;

public class server {

	private String keyword = "end";
	private static int port = 1234;
	
	public static void main(String[] args) throws IOException {
		
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
			 System.out.println("Inicializing Chat: OK\n");
		} catch (IOException e) {
				System.out.println("Inicializing Chat: " + e.getMessage() + "\n");
				return; 
		}

		BufferedReader bf = new BufferedReader(in);
		
		String str = bf.readLine();
		System.out.println("Client: " + str + "\n");
		
		PrintWriter pr = new PrintWriter(s.getOutputStream());
		
		pr.println("Yes\n");
		pr.flush();
		
	}

}
