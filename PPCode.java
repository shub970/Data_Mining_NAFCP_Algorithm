class PPCode{
	private long preorder;
    private long postorder;
    private long count;
    
    public PPCode(long pre, long post, long count) {
    	this.setpre(pre);
    	this.setpost(post);
    	this.setcount(count);
    }
    
    public void setpre(long x) {
    	this.preorder = x;
    }
    
    public void setpost(long x) {
    	this.postorder = x;
    }
    
    public void setcount(long x) {
    	this.count = x;
    }
    
    public long getpre() {
    	return this.preorder;
    }
    
    public long getpost() {
    	return this.postorder;
    }
    
    public long getcount() {
    	return this.count;
    }
    
    public void printCode() {
    	System.out.print("<(" + preorder + ", " + postorder + ") : " + count + ">");
    }
    
    public static boolean isAncestor(PPCode c1, PPCode c2) {
        return (c1.preorder<c2.preorder && c1.postorder>c2.postorder);
    }
    
    @Override
    public boolean equals(Object o) {
    	PPCode cd = (PPCode)o;
    	//System.out.println(o + "was just compared.");
    	return (this.preorder==cd.preorder && this.postorder==cd.postorder);
    }
	 
}