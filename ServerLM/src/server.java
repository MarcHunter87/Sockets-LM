import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class server {

    private static String serverKeyword = "end";
    private static int port = 1234;
    
    private static ArrayList<String> clientKeywords = new ArrayList<>();
    private static ArrayList<Thread> clientThreads = new ArrayList<>();
    private static ArrayList<Socket> clientSockets = new ArrayList<>();
    private static boolean cerrarServidor = false;
    private static boolean pararBucle = false;
    private static int maxClients = 3;

    public static void main(String[] args) throws IOException {
        boolean haHabidoClientes = false;
        ServerSocket ss = new ServerSocket(port);

        System.out.println("PORT_SERVIDOR: " + port);
        System.out.println("PARAULA_CLAU_SERVIDOR: " + serverKeyword);
        System.out.println("\nServer chat at port " + port);
        System.out.println("\nInicializing Server: OK");

        while (!pararBucle) {
            for (int i = 0; i < clientThreads.size(); i++) {
                if (!clientThreads.get(i).isAlive()) {
                    clientThreads.remove(i);
                    clientKeywords.remove(i);
                    clientSockets.remove(i);
                    i--;
                }
            }

            if (haHabidoClientes && clientThreads.isEmpty()) {
                System.out.println("\nNo more Clients connected. Closing server.");
                pararBucle = true;
            }

            if (cerrarServidor) {
                for (Socket cs : clientSockets) {
                    try {
                        cs.close();
                    } catch (IOException e) {}
                }
                pararBucle = true;
            }

            if (!pararBucle && clientThreads.size() < maxClients) {
                try {
                    Socket s = ss.accept();
                    haHabidoClientes = true;
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            handleClient(s);
                        }
                    });
                    clientThreads.add(t);
                    clientSockets.add(s);
                    clientKeywords.add(null);
                    int clientNumber = -1;
                    int i = 0;
                    while (i < clientSockets.size() && clientNumber == -1) {
                        if (clientSockets.get(i) == s) {
                            clientNumber = i + 1;
                        }
                        i++;
                    }
                    System.out.println("\nConnection from Client " + clientNumber + ": OK");
                    t.start();
                } catch (IOException e) {
                    System.out.println("\nConnection from Client: " + e.getMessage());
                    pararBucle = true;
                }
            }
        }

        ss.close();
        System.out.println("\nClosing Server: OK");
        System.out.println("\nBye!");
    }

    private static void handleClient(Socket s) {
        Scanner scanner = new Scanner(System.in);
        int clientNumber = -1;

        try {
            InputStreamReader in = new InputStreamReader(s.getInputStream());
            int i = 0;

            while (i < clientSockets.size() && clientNumber == -1) {
                if (clientSockets.get(i) == s) {
                    clientNumber = i + 1;
                }
                i++;
            }

            System.out.println("\nInicializing Chat for Client " + clientNumber + ": OK");
            BufferedReader bf = new BufferedReader(in);
            PrintWriter pr = new PrintWriter(s.getOutputStream());
            String clientKeyword = bf.readLine();

            synchronized (clientKeywords) {
                int j = 0;
                boolean encontrado = false;
                while (j < clientSockets.size() && !encontrado) {
                    if (clientSockets.get(j) == s) {
                        clientKeywords.set(j, clientKeyword);
                        encontrado = true;
                    }
                    j++;
                }
            }

            pr.println(serverKeyword);
            pr.flush();
            String str;
            boolean breakLoop = false;
            
            while (!breakLoop && !cerrarServidor) {
                try {
                    str = bf.readLine();
                    System.out.println("\nClient " + clientNumber + ": " + str);

                    if (str == null) {
                        breakLoop = true;
                    } else if (str.toLowerCase().contains(clientKeyword)) {
                        System.out.println("\nClient " + clientNumber + " Keyword Detected!");
                        breakLoop = true;
                    } else {
                        System.out.print("\nServer (to Client " + clientNumber + "): ");
                        str = scanner.nextLine();

                        if (str.toLowerCase().contains(serverKeyword)) {
                            synchronized (clientSockets) {
                                for (Socket cs : clientSockets) {
                                    try {
                                        PrintWriter prEspecifico = new PrintWriter(cs.getOutputStream());
                                        prEspecifico.println(serverKeyword);
                                        prEspecifico.flush();
                                    } catch (IOException e) {}
                                }
                            }
                            
                            System.out.println("\nServer Keyword Detected!");
                            cerrarServidor = true;
                            breakLoop = true;
                        } else if (str.toLowerCase().contains(clientKeyword)) {
                            pr.println(str);
                            pr.flush();
                            System.out.println("\nClient " + clientNumber + " Keyword Detected!");
                            breakLoop = true;
                        } else if (contienePalabraClaveDeOtroCliente(str, clientKeyword)) {
                            pr.println(str);
                            pr.flush();
                        } else {
                            pr.println(str.trim());
                            pr.flush();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nError: " + e.getMessage());
                    breakLoop = true;
                }
            }
            
            in.close();
            bf.close();
            pr.close();
            System.out.println("\nClosing Chat for Client " + clientNumber + ": OK");
            s.close();
        } catch (IOException e) {
            System.out.println("\nInicializing Chat for Client " + clientNumber + ":  " + e.getMessage());
        }
    }

    private static boolean contienePalabraClaveDeOtroCliente(String mensaje, String miClave) {
        synchronized (clientKeywords) {
            for (String clave : clientKeywords) {
                if (clave != null && !clave.equals(miClave) && mensaje.toLowerCase().contains(clave.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
