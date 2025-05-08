import java.net.*;
import java.io.*;
import java.util.*;

public class server {

    private static String serverKeyword = "end";
    private static int port = 1234;
    
    private static ArrayList<String> clientKeywords = new ArrayList<>();
    private static ArrayList<Thread> clientThreads = new ArrayList<>();
    private static ArrayList<Socket> clientSockets = new ArrayList<>();
    private static boolean serverActive = true;
    private static ServerSocket ss;
    private static int maxClients;
    private static boolean primerClienteConectado = false;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Introduce the maximum number of clients available to connect");
            return;
        }

        try {
            maxClients = Integer.parseInt(args[0]);

            if (maxClients <= 0) {
                System.out.println("The maximum number of clients must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("The maximum number of clients must be a valid number");
            return;
        }

        ss = new ServerSocket(port);
        
        System.out.println("PORT_SERVIDOR: " + port);
        System.out.println("PARAULA_CLAU_SERVIDOR: " + serverKeyword);
        System.out.println("\nServer chat at port " + port);
        System.out.println("\nInicializing Server: OK");

        while (serverActive) {
            try {
                Socket s = ss.accept();

                synchronized (clientSockets) {
                    if (numClientesConectados() < maxClients) {
                        int clientIndex = -1;

                        for (int i = 0; i < clientSockets.size(); i++) {
                            if (clientSockets.get(i) == null && clientIndex == -1) {
                                clientIndex = i;
                                clientSockets.set(i, s);
                                clientThreads.set(i, null);
                                clientKeywords.set(i, null);
                            }
                        }

                        if (clientIndex == -1) {
                            clientSockets.add(s);
                            clientThreads.add(null);
                            clientKeywords.add(null);
                            clientIndex = clientSockets.size() - 1;
                        }

                        Thread t = new Thread(new hilo_responder_client(s, clientIndex, clientSockets, clientKeywords, clientThreads, serverKeyword, scanner));
                        clientThreads.set(clientIndex, t);

                        System.out.println("\nConnection from Client " + (clientIndex + 1) + ": OK");

                        t.start();

                        if (!primerClienteConectado) {
                            primerClienteConectado = true;
                        }
                    } else {
                        PrintWriter pr = new PrintWriter(s.getOutputStream());
                        pr.println("SERVER_FULL");
                        pr.flush();

                        try { 
                            Thread.sleep(100); 
                        } catch (InterruptedException e) {}

                        pr.close();
                        s.close();

                        System.out.println("\nRejected new client connection: server full");
                    }
                }
            } catch (IOException e) {}
        }

        System.out.println("\nBye!");
    }

    public static int numClientesConectados() {
        int count = 0;

        for (Socket socket : clientSockets) {
            if (socket != null) {
                count++;
            }
        }
        return count;
    }

    public static void cerrarServidorSinClientes() {
        synchronized (clientSockets) {
            if (primerClienteConectado && numClientesConectados() == 0) {
                System.out.println("\nNo more clients connected.");
                System.out.println("\nClosing server.");

                serverActive = false;

                try {
                    ss.close();
                } catch (IOException e) {}
            }
        }
    }
}
