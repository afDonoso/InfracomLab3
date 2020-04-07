package FTP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FTPServer {
    /*
     * Number of connected clients
     */
    static int connectedClients = 0;
    static int allowedClients = 0;
    static int readyClients = 0;
    static ArrayList<transferfile> clients = new ArrayList<>();
    static String selectedFile = "";

    public static void main(String args[]) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Selection of number of allowed clients
        System.out.println("How many clients do you want to attend simultaneously?");
        allowedClients = Integer.parseInt(br.readLine());

        // Selection of file
        System.out.println("Which file do you want to transfer?");
        System.out.println("1. file100.txt (100,7 MB)");
        System.out.println("2. file250.txt (251,7 MB)");
        int selection = Integer.parseInt(br.readLine());
        if (selection == 1) {
            selectedFile = "./files/server/file100.txt";
        } else {
            selectedFile = "./files/server/file250.txt";
        }

        ServerSocket soc = new ServerSocket(5217);
        System.out.println("FTP Server Started on Port Number 5217");
        while (connectedClients < allowedClients) {
            System.out.println("Waiting for " + (allowedClients - connectedClients) + " more clients to connect...");
            transferfile t = new transferfile(soc.accept());
            clients.add(t);
        }
    }
}

class transferfile extends Thread {
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;

    transferfile(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            System.out.println("FTP Client Connected ...");
            FTPServer.connectedClients++;
            start();

        } catch (Exception ex) {

        }
    }

    void SendFile(DataOutputStream cos) throws Exception {
        File f = new File(FTPServer.selectedFile);
        if (!f.exists()) {
            cos.writeUTF("File Not Found");
            return;
        } else {
            cos.writeUTF("READY");
            FileInputStream fin = new FileInputStream(f);
            int ch;
            do {
                ch = fin.read();
                cos.writeUTF(String.valueOf(ch));
            } while (ch != -1);
            fin.close();
            cos.writeUTF("File Receive Successfully");
        }
    }

    public void run() {
        while (true) {
            if (FTPServer.readyClients == FTPServer.connectedClients
                    && FTPServer.connectedClients == FTPServer.allowedClients) {
                System.out.println("Sending files...");

                try {
                    for (transferfile client : FTPServer.clients) {
                        // client.dout.writeUTF("File sent");
                        SendFile(client.dout);
                        System.out.println("FILE SENT");
                    }
                    System.out.println("ALL FILES SENT");
                    System.exit(1);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Waiting for " + (FTPServer.connectedClients - FTPServer.readyClients)
                        + " clients to get ready...");

                try {
                    System.out.println("Waiting for Command ...");
                    String Command = din.readUTF();
                    if (Command.compareTo("READY") == 0) {
                        System.out.println("\tREADY Command Received ...");
                        FTPServer.readyClients++;
                        dout.writeUTF(FTPServer.selectedFile.split("/")[3]);

                        continue;
                    } else if (Command.compareTo("DISCONNECT") == 0) {
                        System.out.println("\tDisconnect Command Received ...");
                        System.exit(1);
                    }
                } catch (Exception ex) {

                }
            }
        }
    }
}