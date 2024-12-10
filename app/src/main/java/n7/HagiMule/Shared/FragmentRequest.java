package n7.HagiMule.Shared;

import java.io.Serializable;

public class FragmentRequest implements Serializable {
	public final String fileHash;
	public final int fragmentNumber;

	public FragmentRequest(String fileHash, int fragmentNumber) {
		this.fileHash = fileHash;
		this.fragmentNumber = fragmentNumber;
	}
}
