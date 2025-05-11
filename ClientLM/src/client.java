import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    private static String clientKeyword;
    private static int port = 1234;
    private static String serverKeyword;
    private static boolean running = true;
    private static boolean puedeEnviar = true;
    private static boolean gestionarSalto = false;

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

        Thread serverListener = new Thread(() -> {
            try {
                String mensajeServidor;
                while (running && (mensajeServidor = bf.readLine()) != null) {
                    if (gestionarSalto) {
                        System.out.println();
                    }
                    System.out.println("\nServer: " + mensajeServidor);

                    puedeEnviar = true;

                    if (mensajeServidor.toLowerCase().contains(clientKeyword)) {
                        System.out.println("\nClient Keyword Detected!");
                        System.out.println("\nClosing Chat: OK");
                        System.out.println("\nClosing Client: OK");
                        System.out.println("\nBye!");
                        running = false;
                        try {
                            s.close();
                        } catch (IOException ex) {}
                        
                        System.exit(0);
                    }
                    
                    if (mensajeServidor.toLowerCase().contains(serverKeyword)) {
                        System.out.println("\nServer Keyword Detected!");
                        System.out.println("\nClosing Chat: OK");
                        System.out.println("\nClosing Client: OK");
                        System.out.println("\nBye!");
                        running = false;
                        try {
                            s.close();
                        } catch (IOException ex) {}
                        
                        System.exit(0);
                    }
                }
                if (running) {
                    System.out.println("\nConexion ended by server");
                    running = false;
                    try {
                        s.close();
                    } catch (IOException ex) {}
                    
                    System.exit(0);
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("\nConexion ended by server");
                    running = false;
                    
                    try {
                        s.close();
                    } catch (IOException ex) {}
                    
                    System.exit(0);
                }
            }
        });

        serverListener.start();

        String str;
        while (running) {
            while (!puedeEnviar && running) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            if (!running){
                continue;
            }

            gestionarSalto = true;

            System.out.print("\nClient: ");
            
            str = scanner.nextLine();
            gestionarSalto = false;

            if (!running){
                continue;
            }
            if (str.toLowerCase().contains(clientKeyword)) {
                pr.println(str);
                pr.flush();

                puedeEnviar = false;

                System.out.println("\nClient Keyword Detected!");

                running = false;
            } else {
                pr.println(str.trim());
                pr.flush();

                puedeEnviar = false;
            }
        }

        try {
            serverListener.join();
        } catch (InterruptedException e) {}

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
