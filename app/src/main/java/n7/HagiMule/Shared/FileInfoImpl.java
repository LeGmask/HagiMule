package n7.HagiMule.Shared;

public class FileInfoImpl implements FileInfo {
    private String nom;
    private long taille;
    private String hash;
    private long fragmentSize;

    public FileInfoImpl(String nom, long taille, String hash, long fragmentSize) {
        this.nom = nom;
        this.taille = taille;
        this.hash = hash;
        this.fragmentSize = fragmentSize;
    }

    @Override
    public long getTaille() {
        return taille;
    }

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public long getFragmentSize() {
        return this.fragmentSize;
    }

    public static int getFragmentsCount(FileInfo info) {
        long a =
                info.getTaille() / info.getFragmentSize()
                        + (info.getTaille() % info.getFragmentSize() == 0 ? 0 : 1);
        return (int) a;
    }

    public static int getTailleOfFrag(FileInfo info, int number) {
        long a =
                number < (getFragmentsCount(info) - 1)
                        ? info.getFragmentSize()
                        : (info.getTaille() % info.getFragmentSize());
        return (int) a;
    }
}
