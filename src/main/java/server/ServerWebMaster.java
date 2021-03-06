package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerWebMaster implements Runnable {
    int threade;
    BlockingQueue<Socket> socketid;
    private ServerSQL sql;

    public ServerWebMaster(int threade, ServerSQL sql) {
        this.threade = threade;
        this.sql=sql;
    }

    @Override
    public void run() {

        socketid = new ArrayBlockingQueue<>(1000);

        Thread[] threadid = new Thread[threade];
        for (int i = 0; i < threade; i++) {
            ServerMain.debug(5, "Käivitame threadi " + i);
            threadid[i] = new Thread(new ServerWebThread(socketid,sql), "thread " + i);
            threadid[i].start();
        }


        System.out.println("hakkame kuulama");
        try (ServerSocket gameServerSocket = new ServerSocket(80)) {

            while (true) {
                // wait for an incoming connection
                Socket websocket = gameServerSocket.accept();
                ServerMain.debug(8, "tuli ühendus webserverisse");

                socketid.add(websocket);

                // kontrollime, et kõik threadid oleks elus
                for (int i = 0; i < threade; i++) {
                    if (!threadid[i].isAlive()) {
                        ServerMain.debug(3, "webserver thread oli maha surnud, elustame");
                        threadid[i] = new Thread(new ServerWebThread(socketid,sql));
                        threadid[i].start();
                    }
                } // kõikide threadide elusoleku kontrolli ots


            } // while
        } // try
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // run


}
