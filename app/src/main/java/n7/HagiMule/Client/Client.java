package n7.HagiMule.Client;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import n7.HagiMule.Diary.Diary;

public class Client {

    public static void printHelp() {
        System.out.println("Usage : java Client <diary address> <diary port>");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printHelp();
            System.exit(1);
        }

        String diaryAdress = String.format("//%s:%s/Diary", args[0], args[1]);
        try {
            // reaching out to the Diary
            Diary index = (Diary) Naming.lookup(diaryAdress);
            // launching the Daemon
            DaemonImpl daemon = new DaemonImpl(index);
            daemon.start();

            DownloaderImpl downloader = new DownloaderImpl(index);
            downloader.start();

            // adding a shutdown hook to unregister the Daemon and free the port
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread() {
                                public void run() {
                                    try {
                                        daemon.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

            // Start the TUI
            Tui tui = new Tui(index, daemon, downloader);
        } catch (MalformedURLException e) {
            System.out.println("The RMI registry is incorrect.");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("The RMI server exists, but no Diary Found");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Cannot reach the RMI registry.");
            e.printStackTrace();
        } catch (ConnectException e) {
            System.out.println(
                    "Connexion refusée. Est-ce que le serveur est allumé et joingable ?");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
