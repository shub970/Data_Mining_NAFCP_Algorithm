import java.util.*;
class NList{
	private String item;
	private List<PPCode> codes;
	
	public NList()
	{
		item = "";
		codes = new ArrayList<PPCode>();
	}
	
	public List<PPCode> getCodes(){
		return this.codes;
	}
	
	public String getItems() {
		return this.item;
	}
	
	public void setItemName(String nm)
	{
		this.item = nm;
	}
	
	public int getSize() {
		return this.codes.size();
	}
	
	public PPCode getAt(int i) {
		return this.codes.get(i);
	}
	
	public long getfreq() {
		long f = 0;
		for(PPCode x : codes)
			f += x.getcount();
        return f;
	}
}