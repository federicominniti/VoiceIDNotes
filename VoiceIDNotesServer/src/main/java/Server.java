import org.iq80.leveldb.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class Server {
    private static DB database;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        database = openDB();
        try {
            start(5500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void start(int port) throws IOException {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while (true)
            new EchoClientHandler(serverSocket.accept()).start();
    }

    private static DB openDB() {
        Options options = new Options();
        options.createIfMissing(true);
        DB db = null;
        try {
            db = factory.open(new File("voiceidnotes_db"), options);
        } catch (IOException e) {
            System.err.println("Cannot open database");
            System.exit(1);
            e.printStackTrace();
        }

        return db;
    }

    private static void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.exit(1);
            e.printStackTrace();
        }
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String inputLine = null;
                while (true) {
                    try {
                        if (!((inputLine = in.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (".".equals(inputLine)) {
                        out.println("bye");
                        break;
                    }
                    out.println(inputLine);
                }

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
}
