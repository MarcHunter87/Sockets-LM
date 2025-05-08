import java.io.*;
import java.net.*;
import java.util.*;

public class hilo_responder_client implements Runnable {
    private Socket s;
    private int clientIndex;
    private ArrayList<Socket> clientSockets;
    private ArrayList<String> clientKeywords;
    private ArrayList<Thread> clientThreads;
    private final String serverKeyword;
    private String clientKeyword;
    private Scanner scanner;

    public hilo_responder_client(Socket s, int clientIdx, ArrayList<Socket> clientSockets, ArrayList<String> clientKeywords, ArrayList<Thread> clientThreads, String serverKeyword, Scanner scanner) {
        this.s = s;
        this.clientIndex = clientIdx;
        this.clientSockets = clientSockets;
        this.clientKeywords = clientKeywords;
        this.clientThreads = clientThreads;
        this.serverKeyword = serverKeyword;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        int clientNumber = clientIndex + 1;
        boolean breakLoop = false;
        
        try {
            InputStreamReader in = new InputStreamReader(s.getInputStream());
            System.out.println("\nInicializing Chat for Client " + clientNumber + ": OK");

            BufferedReader bf = new BufferedReader(in);
            PrintWriter pr = new PrintWriter(s.getOutputStream());
            clientKeyword = bf.readLine();

            synchronized (clientKeywords) {
                clientKeywords.set(clientIndex, clientKeyword);
            }

            pr.println(serverKeyword);
            pr.flush();

            String str;

            while (!breakLoop) {
                try {
                    str = bf.readLine();

                    if (str == null) {
                        breakLoop = true;
                    } else {
                        System.out.println("\nClient " + clientNumber + ": " + str);

                        String clientKeyword = clientKeywords.get(clientIndex);
                        
                        if (str.toLowerCase().contains(clientKeyword.toLowerCase())) {
                            System.out.println("\nClient " + clientNumber + " Keyword Detected!");
                            breakLoop = true;
                        } else {
                            System.out.print("\nServer (to Client " + clientNumber + "): ");
                            String respuesta = scanner.nextLine();

                            pr.println(respuesta.trim());
                            pr.flush();

                            if (respuesta.toLowerCase().contains(clientKeyword.toLowerCase())) {
                                System.out.println("\nClient " + clientNumber + " Keyword Detected!");
                                breakLoop = true;
                            } else if (respuesta.toLowerCase().contains(serverKeyword)) {
                                pr.println(serverKeyword);
                                pr.flush();
                                
                                System.out.println("\nServer Keyword Detected!");
                                breakLoop = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    String msg = e.getMessage();
                    if (msg == null || (!msg.contains("Connection reset") && !msg.contains("Connection aborted") && !msg.contains("An existing connection was forcibly closed"))) {
                        System.out.println("\nError: " + e.getMessage());
                    }

                    breakLoop = true;
                }
            }

            in.close();
            bf.close();
            pr.close();

            System.out.println("\nClosing Chat for Client " + clientNumber + ": OK");

            s.close();
            
            synchronized (clientSockets) {
                clientSockets.set(clientIndex, null);
                clientKeywords.set(clientIndex, null);
                clientThreads.set(clientIndex, null);

                server.cerrarServidorSinClientes();
            }
        } catch (IOException e) {
            System.out.println("\nInicializing Chat for Client " + clientNumber + ":  " + e.getMessage());
        }
    }
} 