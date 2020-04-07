package FTP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class FTPClient {
    public static void main(String args[]) throws Exception {
        Socket soc = new Socket("127.0.0.1", 5217);
        transferfileClient t = new transferfileClient(soc);
        t.displayMenu();

    }
}

class transferfileClient {
    public final static String FILE_PATH = "./files/client/";

    private String selectedFile;
    private int fileSize;

    Socket ClientSoc = null;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;

    transferfileClient(Socket soc) {
        try {
            ClientSoc = soc;
            din = new DataInputStream(ClientSoc.getInputStream());
            dout = new DataOutputStream(ClientSoc.getOutputStream());
            br = new BufferedReader(new InputStreamReader(System.in));
            selectedFile = "";
        } catch (Exception ex) {
        }
    }

    void ReceiveFile() throws Exception {
        String msgFromServer = din.readUTF();

        if (msgFromServer.compareTo("File Not Found") == 0) {
            System.out.println("File not found on Server ...");
            return;
        } else if (msgFromServer.compareTo("READY") == 0) {
            System.out.println("Receiving File ...");
            File f = new File(FILE_PATH + selectedFile);
            if (f.exists()) {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option = br.readLine();
                if (Option == "N") {
                    dout.flush();
                    return;
                }
            }
            FileOutputStream fout = new FileOutputStream(f);
            int ch;
            String temp;
            do {
                temp = din.readUTF();
                ch = Integer.parseInt(temp);
                if (ch != -1) {
                    fout.write(ch);
                }
            } while (ch != -1);
            fout.close();
            System.out.println(din.readUTF());

        }

    }

    public void displayMenu() throws Exception {
        while (true) {
            System.out.println("Are you ready to receive files? [Y/N]");
            String choice;
            choice = br.readLine();

            if (choice.equals("Y")) {
                dout.writeUTF("READY");
                selectedFile = din.readUTF();
                System.out.println("File to receive: " + selectedFile + " (" + fileSize + " B)");

                try {
                    ReceiveFile();
                    System.exit(1);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}