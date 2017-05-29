package direct.get;

// OK - I feel like am going to regret this but this closed design makes things
//        much simpler and it might worth the thread off.
public enum ProvidingLevel {
	
	Default, Normal, Dictate;
	
	public boolean is(ProvidingLevel level) {
		return this == level;
	}
	
	public <T> boolean is(Providing<T> providing) {
		if(providing == null) {
			return false;
		}
		return is(providing.getLevel());
	}
	
}
