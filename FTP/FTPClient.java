package FTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class FTPClient {
    public static void main(String[] args) {
        DatagramSocket socket = null;
        Scanner scanner = null;
        BufferedOutputStream bos = null;
        byte[] buffer = new byte[FTPServer.BUFFER_SIZE];
        DatagramPacket request, response;
        String selectedFile = "";

        try {
            InetAddress address = InetAddress.getLocalHost();
            socket = new DatagramSocket();

            // Aviso al servidor
            request = new DatagramPacket(buffer, buffer.length, address, FTPServer.PORT);
            socket.send(request);

            // ¿Cliente listo para recibir?
            scanner = new Scanner(System.in);
            while (true) {
                System.out.println("¿Estás listo para recibir archivos? (Y / N)");
                String userInput = scanner.nextLine().trim();

                if (userInput.compareToIgnoreCase("Y") == 0) {
                    buffer = userInput.getBytes();

                    request = new DatagramPacket(buffer, buffer.length, address, FTPServer.PORT);
                    socket.send(request);

                    break;
                }
            }

            // Recepción del nombre del archivo
            buffer = new byte[256];
            response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);
            selectedFile = new String(buffer, 0, response.getLength());
            System.out.println("Se va a recibir el archivo " + selectedFile);

            // Recepción del archivo
            System.out.println("Esperando transferencia de archivo...");
            buffer = new byte[FTPServer.BUFFER_SIZE];
            File file = new File("files/FTP/client/" + selectedFile);
            bos = new BufferedOutputStream(new FileOutputStream(file));
            response = new DatagramPacket(buffer, buffer.length);
            System.out.println("Recibiendo paquetes...");
            int paquetes = 0;
            long endTime;
            while (true) {
                socket.receive(response);

                // Verificar si es la terminación de la transmisión
                if (new String(response.getData(), 0, response.getLength()).equals(FTPServer.TERMINACION)) {
                    endTime = System.currentTimeMillis();
                    break;
                }
                paquetes++;
                bos.flush();
                bos.write(response.getData(), 0, response.getLength());
            }

            // Envío del tiempo de terminación
            buffer = Long.toString(endTime).getBytes();
            request = new DatagramPacket(buffer, buffer.length, address, FTPServer.PORT);
            socket.send(request);

            // Recepción de paquetes esperados
            buffer = new byte[256];
            response = new DatagramPacket(buffer, buffer.length, address, FTPServer.PORT);
            socket.receive(response);
            int paquetesEsperados = Integer.parseInt(new String(response.getData(), 0, response.getLength()));
            System.out.println(paquetes + " paquetes recibidos de " + paquetesEsperados + " paquetes esperados.");

            // Envío de paquetes recibidos
            buffer = Integer.toString(paquetes).getBytes();
            request = new DatagramPacket(buffer, buffer.length, address, FTPServer.PORT);
            socket.send(request);

            // TODO Recibir Hash del servidor y calcular Hash

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();

            if (scanner != null)
                scanner.close();

            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}