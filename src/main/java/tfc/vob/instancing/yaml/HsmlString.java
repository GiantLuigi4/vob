package tfc.vob.instancing.yaml;

public class HsmlString extends HsmlEntry {
	String value;
    
    public HsmlString(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value.substring(1, value.length() - 1);
	}
}