import java.io.*;
import java.net.*;

public class Cliente {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Escribe tu palabra clave para salir (ej: SALIR): ");
            String claveSalida = console.readLine();

            System.out.println("Conectando al servidor...");
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                // Leer primer mensaje del servidor
                String serverMessage = in.readLine();
                if (serverMessage != null && serverMessage.toLowerCase().contains("servidor lleno")) {
                    System.out.println("❌ " + serverMessage);
                    socket.close();
                    return;
                }

                System.out.println(serverMessage); // Ingresa tu nombre:
                String name = console.readLine();
                out.println(name);

                // Hilo para recibir mensajes del servidor
                Thread readThread = new Thread(() -> {
                    try {
                        String response;
                        while ((response = in.readLine()) != null) {
                            System.out.println(response);
                            if (response.trim().equalsIgnoreCase(claveSalida)) {
                                System.out.println("🚪 Palabra clave recibida desde el servidor. Saliendo...");
                                System.exit(0);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("⚠️ El servidor cerró la conexión.");
                    } finally {
                        System.exit(0);
                    }
                });
                readThread.setDaemon(true);
                readThread.start();

                // Leer y enviar mensajes al servidor
                String message;
                while ((message = console.readLine()) != null) {
                    if (message.equalsIgnoreCase(claveSalida)) {
                        System.out.println("Palabra clave detectada. Saliendo del chat...");
                        System.exit(0);
                    }
                    out.println(message);
                }

            } catch (IOException e) {
                System.out.println("❌ Error durante la comunicación con el servidor.");
            } finally {
                socket.close();
            }

        } catch (IOException e) {
            System.out.println("❌ No se pudo conectar al servidor.");
        }
    }
}
