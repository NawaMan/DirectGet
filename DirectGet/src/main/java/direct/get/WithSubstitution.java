package direct.get;

import java.util.ArrayList;
import java.util.List;

public class WithSubstitution {
	
	@SuppressWarnings("rawtypes")
	private List<Providing> providings = new ArrayList<>();

	public <T> WithSubstitution(Providing<T> providing) {
		providings.add(providing);
	}
	
	public <T> WithSubstitution withSubstitution(Providing<T> providing) {
		providings.add(providing);
		return this;
	}
	
	public void run(Runnable runnable) {
		
	}
	
	// TODO - Add callable or somethign

}
