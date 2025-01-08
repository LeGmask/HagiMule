package n7.HagiMule.Client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import n7.HagiMule.Diary.Diary;

// catch (ConnectException e) {
// System.out.println("Connexion refusée. Est-ce que le serveur est allumé et joingable ?");}

public class Client {
    Diary index;
    DaemonImpl daemon;
    DownloaderImpl downloader;

    public Client(Boolean tui, String host, String port, String[] files) {
        initializeComponents(host, port);
        registerHooks();
        startComponents();

        registerInitialFiles(files);

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

    public static void printHelp() {
        System.out.println("Usage : java Client <diary address> <diary port> [--no-tui]");
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

        // if --no-tui is passed, we don't start the TUI
        for (int i = 2; i < args.length; i++) {
            if (args[i].equals("--no-tui")) {
                tui = false;
            } else if (args[i].equals("--files")) {
                String pwd = System.getProperty("user.dir");
                files = args[i + 1].split(",");
                for (int j = 0; j < files.length; j++) {
                    files[j] = pwd + "/" + files[j];
                }
                i++;
            } else if (args[i].equals("--help")) {
                printHelp();
                System.exit(0);
            }
        }

        System.out.println("Starting client with Files : ");

        new Client(tui, host, port, files);
    }
}
