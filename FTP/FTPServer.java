package FTP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

public class FTPServer {
    public final static int PORT = 3000;
    public final static int BUFFER_SIZE = 16384;
    public final static String TERMINACION = "end";

    public static void main(String[] args) {
        Scanner scanner = null;
        DatagramSocket socket = null;
        byte[] buffer = null;
        DatagramPacket request;
        String selectedFile = "";

        try {
            scanner = new Scanner(System.in);
            int expectedClients = 0;
            int readyClients = 0;
            int connectedClients = 0;
            ServerThread[] servers;
            socket = new DatagramSocket(PORT);
            buffer = new byte[BUFFER_SIZE];

            // Selección de usuarios esperados
            System.out.println("¿Cuántos usuarios esperas que se conecten?");
            expectedClients = scanner.nextInt();
            servers = new ServerThread[expectedClients];

            // Selección del archivo a enviar
            System.out.println("¿Cuál archivo vas a enviar? Ingresa el número");
            System.out.println("1. file100.txt (100 MB)");
            System.out.println("2. file250.txt (250 MB)");
            selectedFile = scanner.nextInt() == 1 ? "file100.txt" : "file250.txt";
            System.out.println(selectedFile);

            // Conexión de los clientes
            System.out.println("Esperando a los clientes...");
            System.out.println(0 + "/" + expectedClients + " conectados");
            while (connectedClients != expectedClients) {
                // Cliente avisa que entró o que está listo
                request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Verificación de acción
                String userInput = new String(buffer, 0, request.getLength());
                System.out.println("Usuario: " + userInput);

                if (userInput.compareToIgnoreCase("Y") == 0) {
                    // Cliente indicó que está listo para recibir
                    readyClients++;
                } else {
                    // Cliente se conectó al servidor
                    // Inicialización de los servidores
                    // Se crean tantos servidores como clientes hayan

                    servers[connectedClients] = new ServerThread(connectedClients, request.getAddress(),
                            request.getPort(), selectedFile, socket);

                    connectedClients++;
                    System.out.println(connectedClients + "/" + expectedClients + " conectados");
                }
            }

            // Preparación de los clientes
            System.out.println("Todos los clientes están conectados");
            System.out.println("Esperando a que los clientes estén listos");
            System.out.println(readyClients + "/" + expectedClients + " listos");
            do {
                request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                readyClients++;

                System.out.println(readyClients + "/" + expectedClients + " listos");
            } while (readyClients != expectedClients);
            System.out.println("Todos los clientes están listos para recibir archivos");

            // Empezamos transmisión de los archivos
            for (int i = 0; i < expectedClients; i++) {
                servers[i].start();
            }

            for (int i = 0; i < connectedClients; i++) {
                servers[i].join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null)
                scanner.close();
            if (socket != null)
                socket.close();
        }
    }
}