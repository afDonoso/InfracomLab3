package FTP;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FTPClient {
    public static void main(String[] args) throws IOException {
        // Step 1 : Create a socket to listen at port 1234
        DatagramSocket ds = new DatagramSocket(1234);
        File file = new File("files/FTP/client/file100.txt");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] receive = new byte[16384];

        DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
        while (true) {
            ds.receive(DpReceive);
            if (new String(DpReceive.getData(), 0, DpReceive.getLength()).equals("end")) {
                break;
            }
            bos.flush();
            bos.write(DpReceive.getData(), 0, DpReceive.getLength());
        }
    }
}