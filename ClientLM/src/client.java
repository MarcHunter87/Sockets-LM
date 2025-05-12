import java.io.*;
import java.net.*;

public class client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;
    private static volatile boolean cerrar = false;

    public static void main(String[] args) {
        Thread hiloPrincipal = new Thread(() -> {
            if (args.length < 1) {
                System.out.println("Uso: java client <palabraClave>");
                cerrar = true;
                return;
            }

            String claveSalida = args[0];
            try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.print("Conectando al servidor\n");
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    // Leer primer mensaje del servidor
                    String serverMessage = in.readLine();
                    if (serverMessage != null && serverMessage.toLowerCase().contains("servidor lleno")) {
                        System.out.println(serverMessage);
                        cerrar = true;
                        socket.close();
                        return;
                    }

                    System.out.println("PORT_SERVIDOR: " + SERVER_PORT);
                    System.out.println("PARAULA_CLAU_CLIENT: " + claveSalida);
                    System.out.println("\nEl chat de clientes está disponible en el puerto " + SERVER_PORT);
                    System.out.print(serverMessage);
                    String name = console.readLine();
                    out.println(name);

                    Thread mainLogicThread = Thread.currentThread();
                    // Hilo para recibir mensajes del servidor
                    Thread readThread = new Thread(() -> {
                        try {
                            String response;
                            while (!cerrar && (response = in.readLine()) != null) {
                                System.out.println(response);
                                if (response.trim().toLowerCase().contains(claveSalida.toLowerCase()) ||
                                    response.toLowerCase().contains("cerrando conexión") ||
                                    response.toLowerCase().contains("servidor lleno") ||
                                    response.toLowerCase().contains("servidor_cerrado") ||
                                    response.toLowerCase().contains("ya está en uso")) {
                                    cerrar = true;
                                    try { 
                                        socket.close(); 
                                    } catch (IOException ignored) {}
                                    System.out.println("Pulsa Enter para salir");
                                    mainLogicThread.interrupt();
                                }
                            }
                        } catch (IOException e) {
                            if (!cerrar)
                                System.out.println("El servidor cerró la conexión");
                            cerrar = true;
                            mainLogicThread.interrupt();
                        }
                    });
                    readThread.setDaemon(true);
                    readThread.start();

                    // Leer y enviar mensajes al servidor
                    String message;
                    while (!cerrar && (message = console.readLine()) != null) {
                        if (message.toLowerCase().contains(claveSalida.toLowerCase())) {
                            System.out.println("Palabra clave detectada. Saliendo del chat");
                            cerrar = true;
                            try { socket.close(); } catch (IOException ignored) {}
                        }
                        out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Error durante la comunicación con el servidor");
                } finally {
                    cerrar = true;
                    try { socket.close(); } catch (IOException ignored) {}
                }
            } catch (IOException e) {
                System.out.println("No se pudo conectar al servidor");
                cerrar = true;
            }
        });

        Thread killer = new Thread(() -> {
            while (!cerrar) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
            hiloPrincipal.interrupt();
            Thread.currentThread().interrupt();
        });

        hiloPrincipal.start();
        killer.start();

        try {
            hiloPrincipal.join();
            killer.join();
        } catch (InterruptedException ignored) {}
    }
}