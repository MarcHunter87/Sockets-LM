import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private static final int PORT = 1234;
    private static Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
    	if (args.length < 1) {
    	    System.out.println("Uso: java Server <maxClientes>");
    	    return;
    	}

    	int maxClientes;
    	try {
    	    maxClientes = Integer.parseInt(args[0]);
    	} catch (NumberFormatException e) {
    	    System.out.println("El argumento debe ser un número entero.");
    	    return;
    	}

        System.out.println("PORT_SERVIDOR: " + PORT);
        System.out.println("PARAULA_CLAU_SERVIDOR: CERRAR_SERVER");
        System.out.println("\nEl chat de clientes está disponible en el puerto " + PORT);

        // Hilo para leer comandos desde consola del servidor
        new Thread(() -> {
            try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
                String command;
                while ((command = console.readLine()) != null) {
                    if (command.toLowerCase().contains("cerrar_server")) {
                        System.out.println("Servidor apagado manualmente.");
                        System.exit(0);
                    }

                    if (command.startsWith("para:")) {
                        int sep = command.indexOf(' ');
                        if (sep == -1) {
                            System.out.println("❌ Formato inválido. Usa: para:Nombre mensaje");
                            continue;
                        }

                        String targetName = command.substring(5, sep);
                        String message = command.substring(sep + 1);

                        ClientHandler target = clients.get(targetName);
                        if (target != null) {
                            target.sendMessage("[Servidor]: " + message);
                        } else {
                            System.out.println("❌ Cliente '" + targetName + "' no encontrado.");
                        }
                    } else {
                        System.out.println("Usa el formato: para:Nombre mensaje");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Aceptar conexiones entrantes
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();

                synchronized (clients) {
                    if (clients.size() >= maxClientes) {
                        PrintWriter tempOut = new PrintWriter(socket.getOutputStream(), true);
                        tempOut.println("Servidor lleno. Conexión rechazada.");
                        socket.close();
                        continue;
                    }
                }

                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clase para manejar cada cliente individualmente
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Ingresa tu nombre: ");
                name = in.readLine();

                System.out.println("Nombre recibido: '" + name + "'");

                if (name == null || name.trim().isEmpty() || name.contains(" ")) {
                    out.println("Nombre inválido (no puede estar vacío ni contener espacios). Cerrando conexión.");
                    socket.close();
                    return;
                }

                synchronized (clients) {
                    if (clients.containsKey(name)) {
                        out.println("Ese nombre ya está en uso. Cerrando conexión.");
                        socket.close();
                        return;
                    }
                    clients.put(name, this);
                }

                System.out.println(name + " se ha conectado.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("[Mensaje de " + name + "]: " + message);
                }

            } catch (IOException e) {
                System.out.println(name + " se ha desconectado.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (name != null) {
                    clients.remove(name);
                    System.out.println("Cliente eliminado: " + name);
                }
            }
        }
    }
}
