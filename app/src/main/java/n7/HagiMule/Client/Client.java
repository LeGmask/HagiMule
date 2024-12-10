package n7.HagiMule.Client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileInfo;

public class Client {

  public static void printHelp() {
    System.out.println("Usage : java Client <diary address> <diary port>");
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      printHelp();
      System.exit(1);
    }

    String diaryAdress = String.format("//%s:%s/Diary", args[0], args[1]);
    try {
      // reaching out to the Diary
      Diary index = (Diary) Naming.lookup(diaryAdress);
      // launching the Daemon
      DaemonImpl daemon = new DaemonImpl(index);

      if (args[2].equals("host")) {
        System.out.println("Testing daemon acting as an uploader");
        daemon.debugUploader();
      } else {
        FileInfo[] files = index.SearchFile("");
        for (FileInfo file : files) {
          System.out
              .println(String.format("\"%s\" : hash = %s, taille = %d", file.getNom(), file.getHash(), file.getTaille()));
        }
        System.out.println("Testing daemon acting as a downloader");
        daemon.debugDownloader();
      }
      

    } catch (MalformedURLException e) {
      System.out.println("The RMI registry is incorrect.");
      e.printStackTrace();
    } catch (NotBoundException e) {
      System.out.println("The RMI server exists, but no Diary Found");
      e.printStackTrace();
    } catch (RemoteException e) {
      System.out.println("Cannot reach the RMI registry.");
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
