import java.io.*;
import java.net.*;

public class client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 1234;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java client <palabraClave>");
            return;
        }
        String claveSalida = args[0];
        
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Conectando al servidor...\n");
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                // Leer primer mensaje del servidor
                String serverMessage = in.readLine();
                if (serverMessage != null && serverMessage.toLowerCase().contains("servidor lleno")) {
                    System.out.println(serverMessage);
                    socket.close();
                    return;
                }

                System.out.println("PORT_SERVIDOR: " + SERVER_PORT);
                System.out.println("PARAULA_CLAU_CLIENT: " + claveSalida);
                System.out.println("\nEl chat de clientes está disponible en el puerto " + SERVER_PORT);
                System.out.print(serverMessage);

                String name = console.readLine();
                out.println(name);

                // Hilo para recibir mensajes del servidor
                Thread readThread = new Thread(() -> {
                    try {
                        String response;
                        while ((response = in.readLine()) != null) {
                            System.out.println(response);
                            if (response.trim().toLowerCase().contains(claveSalida.toLowerCase())) {
                                System.out.println("🚪 Palabra clave recibida desde el servidor. Saliendo...");
                                System.exit(0);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("El servidor cerró la conexión.");
                    } finally {
                        System.exit(0);
                    }
                });
                readThread.setDaemon(true);
                readThread.start();

                // Leer y enviar mensajes al servidor
                String message;
                while ((message = console.readLine()) != null) {
                    if (message.toLowerCase().contains(claveSalida.toLowerCase())) {
                        System.out.println("Palabra clave detectada. Saliendo del chat...");
                        System.exit(0);
                    }
                    out.println(message);
                }

            } catch (IOException e) {
                System.out.println("Error durante la comunicación con el servidor.");
            } finally {
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor.");
        }
    }
}
