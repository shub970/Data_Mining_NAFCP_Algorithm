import java.util.*;

public class PPCtree {

	private String name;
	private List<PPCtree> children;
	private long count = 0;
	private long pre=0, post=0;
	private boolean root = false;


	public PPCtree(String name, PPCtree parent, int ct) {
		this.name = name;
		if (parent==null) this.setRoot();
		this.children = new ArrayList<>(0);
		this.count = ct;
		if (!this.isRoot())
		{
			parent.children.add(this);
		}
		//System.out.println(this.name + " child of "+ this.getAncestor());
	}
	
	public void setRoot() {
		this.root = true;
	}

	public void addChild(PPCtree child) {
		this.children.add(child);
	}

	public List<PPCtree> getChildren() {
		return this.children;
	}
	

	public String getName() {
		return this.name;
	}
	
	public long getCount() {
		return this.count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void incrementCount() {
		this.count++;
	}

	public Boolean isRoot() {
		return this.root;
	}
	
	public void setPost(int a) {
		this.post = a;
	}
	
	public void setPre(int a) {
		this.pre = a;
	}
	
	public long getPost() {
		return this.post;
	}
	
	public long getPre() {
		return this.pre;
	}
}