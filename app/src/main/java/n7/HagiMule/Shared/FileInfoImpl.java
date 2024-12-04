package n7.HagiMule.Shared;

public class FileInfoImpl implements FileInfo {
	private String nom;
	private int taille;
	private String hash;

    public FileInfoImpl(String nom, int taille, String hash) {
        this.nom = nom;
		this.taille = taille;
		this.hash = hash;
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
}
