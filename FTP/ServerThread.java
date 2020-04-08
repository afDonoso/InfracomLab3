package FTP;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerThread extends Thread {
    private int id;
    private InetAddress clientAddress;
    private int clientPort;
    private String selectedFile;
    private DatagramSocket socket;

    public ServerThread(int id, InetAddress clientAddress, int clientPort, String selectedFile, DatagramSocket socket) {
        this.id = id;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.selectedFile = selectedFile;
        this.socket = socket;

        System.out.println("Servidor " + this.id + " comunicándose con cliente" + "\n\tIP: " + this.clientAddress
                + "\n\tPuerto: " + this.clientPort);
    }

    @Override
    public void run() {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        DatagramPacket response, request;
        byte[] buffer = new byte[FTPServer.BUFFER_SIZE];

        try {
            System.out.println(
                    "Enviando información a" + "\n\t" + "IP: " + clientAddress + "\n\t" + "Puerto: " + clientPort);

            // Envío del nombre del archivo
            buffer = selectedFile.getBytes();
            response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            this.socket.send(response);

            // Lectura del archivo
            System.out.println("Leyendo archivo...");
            buffer = new byte[FTPServer.BUFFER_SIZE];
            File file = new File("files/FTP/server/" + selectedFile);
            bis = new BufferedInputStream(new FileInputStream(file));
            int b;
            int paquetes = 0;
            long totalTransmitido = 0;
            long tamañoArchivo = file.length();
            System.out.println("Comenzando transferencia a los clientes...");
            long startTime = System.currentTimeMillis();
            while ((b = bis.read(buffer)) != -1) {
                // Envío de los paquetes (tamaño: buffer.length)
                response = new DatagramPacket(buffer, b, clientAddress, clientPort);
                this.socket.send(response);
                paquetes++;

                totalTransmitido += b;
                double progreso = ((double) totalTransmitido / tamañoArchivo) * 100;
                System.out.println("Progreso Servidor " + id + ": " + progreso + "%");
            }

            // Último paquete que se envía para avisar la terminación
            buffer = FTPServer.TERMINACION.getBytes();
            response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            this.socket.send(response);
            System.out.println("¡Paquetes enviados!");
            System.out.println("Se enviaron " + paquetes + " paquetes");

            // Calcular tiempo de transmisión total
            buffer = new byte[512];
            request = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.receive(request);
            long endTime = Long.parseLong(new String(buffer, 0, request.getLength()));
            long time = endTime - startTime;
            System.out.println("Servidor " + id + ": La transmisión duró " + ((double) time / 1000) + " segundos.");

            // Enviar paquetes esperados al cliente
            buffer = Integer.toString(paquetes).getBytes();
            response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);

            // Recibir paquetes del cliente
            buffer = new byte[256];
            request = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.receive(request);
            int paquetesRecibidos = Integer.parseInt(new String(buffer, 0, request.getLength()));

            // TODO Enviar Hash al cliente

            // Generación del log
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaLog = dtf.format(LocalDateTime.now());

            File logFile = new File("files/FTP/logs/Prueba1");
            bos = new BufferedOutputStream(new FileOutputStream(logFile, true));
            String log = "Servidor: " + id + "\n" + "Fecha: " + fechaLog + "\n";

            // LOG: Archivo enviado
            log += "Archivo enviado: " + selectedFile + " (" + ((double) file.length() / Math.pow(10, 6)) + " MB)"
                    + "\n";

            // LOG: Info cliente
            // TODO Agregar si fue exitosa la entrega (Hash)
            log += "Cliente: " + "\n\t" // eslint-disable-line
                    + "IP: " + clientAddress + "\n\t" // eslint-disable-line
                    + "Puerto: " + clientPort + "\n\t" // eslint-disable-line
                    + "Tiempo transferencia: " + ((double) time / 1000) + " s" + "\n\t" // eslint-disable-line
                    + "Paquetes enviados: " + paquetes + "\n\t"// eslint-disable-line
                    + "Paquetes recibidos por el cliente: " + paquetesRecibidos + "\n"
                    + "------------------------------------------------\n";

            bos.write(log.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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