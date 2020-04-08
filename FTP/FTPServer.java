package FTP;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class FTPServer {
    public static void main(String args[]) throws IOException {
        DatagramSocket ds = new DatagramSocket();

        InetAddress ip = InetAddress.getLocalHost();
        byte buf[] = new byte[16384];
        Scanner sc = new Scanner(System.in);

        // Selección de usuarios esperados
        System.out.println("¿Cuántos usuarios esperas que se conecten?");
        int expectedUsers = Integer.parseInt(sc.nextLine());

        String ruta = sc.nextLine();
        File a = new File(ruta);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(a));
        int b;
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, 1234);
        while ((b = bis.read(buf)) != -1) {
            DatagramPacket nuevo = new DatagramPacket(buf, b, DpSend.getAddress(), DpSend.getPort());
            ds.send(nuevo);
        }
        buf = "end".getBytes();
        DatagramPacket nuevo = new DatagramPacket(buf, buf.length, DpSend.getAddress(), DpSend.getPort());
        ds.send(nuevo);

    }
}