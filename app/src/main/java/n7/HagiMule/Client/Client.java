package n7.HagiMule.Client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileInfo;

// catch (ConnectException e) {
// System.out.println("Connexion refusée. Est-ce que le serveur est allumé et joingable ?");}

public class Client {
    Diary index;
    DaemonImpl daemon;
    DownloaderImpl downloader;

    public Client(Boolean tui, String host, String port, String[] files, String[] dls) {
        initializeComponents(host, port);
        registerHooks();
        startComponents();

        registerInitialFiles(files);
        requestInitialDownloads(dls);

        if (tui)
            try {
                new Tui(index, daemon, downloader);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void initializeComponents(String host, String port) {
        try {
            index = (Diary) Naming.lookup(String.format("//%s:%s/Diary", host, port));
            daemon = new DaemonImpl(index);
            downloader = new DownloaderImpl(index);
        } catch (MalformedURLException e) {
            System.out.println("The RMI registry is incorrect.");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println("The RMI server exists, but no Diary Found");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.out.println("Cannot reach the RMI registry : " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerHooks() {
        // shutdown hook for daemon
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
    }

    private void startComponents() {
        daemon.start();
    }

    private void registerInitialFiles(String[] files) {
        for (String file : files) {
            daemon.addFichier(file);
        }
    }

    private void requestInitialDownloads(String[] dls) {
        for (String dl : dls) {
            try {
                FileInfo file = index.RequestFile(dl);
                downloader.submit(file, "/dev/null");
            } catch (RemoteException e) {
                System.out.println("Cannot download file : " + e.getLocalizedMessage());
            }

        }
    }

    public static void printHelp() {
        System.out.println("Usage : java Client <diary address> <diary port> [--no-tui] [--files <file1,file2,...>]");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printHelp();
            System.exit(1);
        }

        String host = args[0];
        String port = args[1];
        Boolean tui = true;

        String[] files = new String[0];
        String[] dls = new String[0];

        // if --no-tui is passed, we don't start the TUI
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--no-tui")) {
                tui = false;
            } else if (args[i].equals("--files")) {
                files = args[i + 1].split(",");
                i++;
            } else if (args[i].equals("--dl")) {
                dls = args[i + 1].split(",");
                i++;
            } else if (args[i].equals("--help")) {
                printHelp();
                System.exit(0);
            }
        }

        System.out.println("Starting client with Files : ");

        new Client(tui, host, port, files, dls);
    }
}
