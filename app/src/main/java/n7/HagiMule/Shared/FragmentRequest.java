package n7.HagiMule.Shared;

public class FragmentRequest {
	public final String fileHash;
	public final int fragmentNumber;

	public FragmentRequest(String fileHash, int fragmentNumber) {
		this.fileHash = fileHash;
		this.fragmentNumber = fragmentNumber;
	}
}
