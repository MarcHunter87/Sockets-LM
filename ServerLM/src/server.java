import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private static final int PORT = 1234;
    private static Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());
    private static boolean huboAlMenosUnCliente = false;
    private static volatile boolean cerrar = false;
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        Thread hiloPrincipal = new Thread(() -> {
            if (args.length < 1) {
                System.out.println("Uso: java Server <maxClientes>");
                cerrar = true;
                return;
            }

            int maxClientes;
            try {
                maxClientes = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("El argumento debe ser un número entero");
                cerrar = true;
                return;
            }

            System.out.println("PORT_SERVIDOR: " + PORT);
            System.out.println("PARAULA_CLAU_SERVIDOR: CERRAR_SERVER");
            System.out.println("\nEl chat de clientes está disponible en el puerto " + PORT);

            // Hilo para leer comandos desde consola del servidor
            Thread consola = new Thread(() -> {
                try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
                    String command;
                    while ((command = console.readLine()) != null && !cerrar) {
                        if (command.toLowerCase().contains("cerrar_server")) {
                            System.out.println("Palabra Clave del servidor usada, cerrando todo");
                            synchronized (clients) {
                                for (ClientHandler handler : clients.values()) {
                                    handler.sendMessage("SERVIDOR_CERRADO");
                                }
                            }
                            cerrar = true;

                            try {
                                if (serverSocket != null && !serverSocket.isClosed()) {
                                    serverSocket.close();
                                }
                            } catch (IOException ignored) {}
                            return;
                        }

                        if (command.startsWith("para:")) {
                            int sep = command.indexOf(' ');
                            if (sep == -1) {
                                System.out.println("Formato inválido. Usa: para:Nombre mensaje");
                                continue;
                            }
                            String targetName = command.substring(5, sep);
                            String message = command.substring(sep + 1);
                            ClientHandler target = clients.get(targetName);
                            if (target != null) {
                                target.sendMessage("[Servidor]: " + message);
                            } else {
                                System.out.println("Cliente '" + targetName + "' no encontrado");
                            }
                        } else {
                            System.out.println("Usa el formato: para:Nombre mensaje");
                        }
                    }
                } catch (IOException e) {}
            });
            consola.setDaemon(true);
            consola.start();

            // Aceptar conexiones entrantes
            try {
                serverSocket = new ServerSocket(PORT);
                while (!cerrar) {
                    try {
                        Socket socket = serverSocket.accept();
                        synchronized (clients) {
                            if (clients.size() >= maxClientes) {
                                PrintWriter tempOut = new PrintWriter(socket.getOutputStream(), true);
                                tempOut.println("Servidor lleno. Conexión rechazada");
                                socket.close();
                                continue;
                            }
                        }

                        ClientHandler handler = new ClientHandler(socket);
                        handler.start();
                    } catch (SocketException se) {
                        cerrar = true;
                    }
                }
            } catch (IOException e) {} finally {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (IOException ignored) {}
                synchronized (clients) {
                    List<ClientHandler> copia = new ArrayList<>(clients.values());
                    for (ClientHandler handler : copia) {
                        handler.cerrar();
                    }
                }
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

                if (!cerrar) {
                    System.out.println("Nombre recibido: '" + name + "'");
                }

                if (name == null || name.trim().isEmpty() || name.contains(" ")) {
                    out.println("Nombre inválido (no puede estar vacío ni contener espacios). Cerrando conexión");
                    socket.close();
                    return;
                }

                synchronized (clients) {
                    if (clients.containsKey(name)) {
                        if (!cerrar) System.out.println("Nombre rechazado por ya estar en uso");
                        out.println("Ese nombre ya está en uso. Cerrando conexión");
                        socket.close();
                        return;
                    }
                    clients.put(name, this);
                    huboAlMenosUnCliente = true;
                }

                if (!cerrar) {
                    System.out.println(name + " se ha conectado.");
                }

                String message;
                while (!cerrar && (message = in.readLine()) != null) {
                    if (!cerrar) {
                        System.out.println("[Mensaje de " + name + "]: " + message);
                    }
                }

            } catch (IOException e) {
                if (!cerrar) {
                    System.out.println(name + " se ha desconectado");
                }
            } finally {
                cerrar();
            }
        }

        public void cerrar() {
            try { 
                if (out != null) {
                    out.close();
                }
            } catch (Exception ignored) {}
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {}
            if (name != null) {
                synchronized (clients) {
                    if (clients.get(name) == this) {
                        if (!cerrar) System.out.println("Cliente eliminado: " + name);
                        clients.remove(name);

                        if (clients.isEmpty() && huboAlMenosUnCliente) {
                            if (!cerrar) System.out.println("Todos los clientes se han desconectado. Cerrando servidor.");
                            cerrar = true;

                            try { 
                                if (serverSocket != null && !serverSocket.isClosed()) {
                                    serverSocket.close();
                                }
                            } catch (IOException ignored) {}
                        }
                    }
                }
            }
        }
    }
}