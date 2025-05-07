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
    private static boolean pararBucle = false;
    private static boolean breakLoop = false;
    private static int maxClients;
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

        ServerSocket ss = new ServerSocket(port);

        System.out.println("PORT_SERVIDOR: " + port);
        System.out.println("PARAULA_CLAU_SERVIDOR: " + serverKeyword);
        System.out.println("\nServer chat at port " + port);
        System.out.println("\nInicializing Server: OK");

        while (!pararBucle) {
            try {
                Socket s = ss.accept();
                if (clientThreads.size() < maxClients) {
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
                } else {
                    BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    bf.readLine();
                    PrintWriter pr = new PrintWriter(s.getOutputStream());
                    pr.println("SERVER_FULL");
                    pr.flush();
                    pr.close();
                    bf.close();
                    s.close();
                    System.out.println("\nRejected new client connection: server full");
                }
            } catch (IOException e) {
                if (!pararBucle) {
                    System.out.println("\nError handling client connection: " + e.getMessage());
                }
            }
        }

        ss.close();
        System.out.println("\nClosing Server: OK");
        System.out.println("\nBye!");
    }

    private static void handleClient(Socket s) {
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
            
            while (!breakLoop) {
                try {
                    str = bf.readLine();
                    if (str == null) {
                        breakLoop = true;
                        continue;
                    }
                    System.out.println("\nClient " + clientNumber + ": " + str);

                    if (str.toLowerCase().contains(clientKeyword)) {
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
                            breakLoop = true;
                            pararBucle = true;
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

            synchronized (clientSockets) {
                int index = clientSockets.indexOf(s);
                if (index != -1) {
                    clientSockets.remove(index);
                    clientKeywords.remove(index);
                    clientThreads.remove(index);
                }
            }

            if (clientSockets.isEmpty() && !pararBucle) {
                System.out.println("\nNo more clients connected.");
                System.out.println("\nClosing server.");
                pararBucle = true;
                System.exit(0);
            }
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
