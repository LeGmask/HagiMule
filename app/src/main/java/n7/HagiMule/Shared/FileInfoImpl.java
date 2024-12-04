package n7.HagiMule.Shared;

public class FileInfoImpl implements FileInfo {
	private String nom;
	private int taille;
	private String hash;
    private int fragmentSize;

    public FileInfoImpl(String nom, int taille, String hash, int fragmentSize) {
        this.nom = nom;
		this.taille = taille;
		this.hash = hash;
        this.fragmentSize = fragmentSize;
    }

    @Override
    public int getTaille() {
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
    public int getFragmentSize() {
        return this.fragmentSize;
    }
}
