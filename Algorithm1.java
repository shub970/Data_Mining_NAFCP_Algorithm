import java.io.*;
import java.util.*;



import java.text.ParseException; 

//This is the comparator used by the function sortHashMapByValues for sorting the Long value of a HashMap
class SortbyVal implements Comparator<Long>
{
    public int compare(Long a, Long b)
    {
        Long diff = b.longValue()-a.longValue();
        if (diff>0l) return 1;
        else if (diff<0l) return -1;
        else return 0;
    }
}

class Algo{
    //Path address of the transaction database file. This file is assumed to be in txt format
	private String transFile;
	
	//Number of transactions in the database
	private long numTrans;
	
	//threshold value or the minimum support count
	private long threshold;
	
	//Number of items in the database
	private int numItems;
	
	//reader for the transaction database file
	private BufferedReader reader;
	
	//HashMap to store count of individual items
	private HashMap<String, Long> count;
	
	//root element of the PPC tree
	private PPCtree root;
	
	//timestamp for pre-order traversal of the PPC tree
	private static int time_pre = -1;
	
	//timestamp for post-order traversal of the PPC tree
	private static int time_post = -1;
	
	//separator used in the transaction database to separate items, eg. , (comma)
	private String separator;
	
	//HashMap for the Subsumption Check
	private HashMap<Long, List<Integer>> Hash;
	
	//List of 1-item sets with their count
	private LinkedHashMap<String, Long> L1 = new LinkedHashMap<String, Long>();
	
	//list of frequent closed patterns corresponding to their frequency
	private String[] FCIs; static int ind = -1;
	
	//list containing maximal frequent item sets obtained from FCIs
	private List<String> maximal;
	
	//to store N-List of the item sets
	private HashMap<String, NList> NStructure;
	
	//Constructor
	public Algo(String filePath, double support, int noOfItems, long numTxn, String separator) throws IOException {
		transFile = filePath;
		double txn = numTxn;
		threshold = Math.round(support*txn);
		numItems = noOfItems;  
		numTrans = numTxn;
		time_pre = -1;
		time_post = -1;
		Hash = new HashMap<Long, List<Integer>>();
		FCIs = new String[9000];
		maximal = new ArrayList<String>();
		NStructure = new HashMap<String, NList>();
		count = new HashMap<String, Long>(numItems);
		this.separator = separator;
		generateL1();
		PPCconstruct();
	}
	
	//function to sort the passed HashMap by values
	private LinkedHashMap<String, Long> sortHashMapByValues(HashMap<String, Long> passedMap) 
		{
		    List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		    List<Long> mapValues = new ArrayList<>(passedMap.values());
		    Collections.sort(mapValues, new SortbyVal()); //sorted by value

		    LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<>();
		    Iterator<Long> valueIt = mapValues.iterator();
		    
		    while (valueIt.hasNext()) {
		        long val = valueIt.next();
		        
		        Iterator<String> keyIt = mapKeys.iterator();

		        while (keyIt.hasNext()) {
		            String key = keyIt.next();
		            long comp1 = passedMap.get(key); 
		            long comp2 = val;

		            if (comp1==comp2) {
		                keyIt.remove();
		                sortedMap.put(key, val);
		                break;
		            }
		        }
		    }
		    return sortedMap;
	}
		
    //finding maximal item sets
	public void findmaximal(String[] fcp) {
    	for(int l = 0; l<=ind; l++)
    		{
    		  int flag = 0;
    		  for(int r = 0; r<=ind; r++)
    		     if (checksubstring(fcp[l],fcp[r]) && r!=l)
    				  flag = 1;
    		  if (flag==0)
    			  maximal.add(fcp[l]);
            }
    }
	
	//finding frequent closed patterns as per NAFCP Algorithm
    public void findFCP(List<String> pattern) {
    	//System.out.println("Called on "+pattern);
    	List<String> nextPattern = new ArrayList<String>(); 
 
    	outer: for(int i = pattern.size()-1; i>=0; i--) 
    	{
    		NList N1 = NStructure.get(pattern.get(i)), N2 = new NList();
    		
    		int j; String tp = "";
    		for(j = i-1; j>=0; j--)
    		{   //System.out.println("Candidate itemset: "+ NStructure.get(pattern.get(i)).getCodes().size());
    			N2 = NStructure.get(pattern.get(j));
    			
    			if (NListCheck(N1, N2)) //if N1 is the subset of N2
    			   
    				if (N1.getfreq()==N2.getfreq()) //Theorem 1 and 2 satisfied 
    				{
    					tp = merge(pattern.get(j),pattern.get(i));
    					if (!NStructure.containsKey(tp))
    					   NStructure.put(tp, getNlist(tp)); 
    					
    					pattern.add(i, tp);  pattern.remove(i+1);
    					pattern.remove(j);
    					continue outer;
    				}
    			    else 
    		        {                          
    					tp = merge(pattern.get(j), pattern.get(i));
    					if (!NStructure.containsKey(tp))
    					  NStructure.put(tp, getNlist(tp));
    					//System.out.println("N-List added for: "+tp+" with frequency "+ NStructure.get(tp).getfreq());
    					pattern.add(i, tp); pattern.remove(i+1);
    					//nextPattern.add(pattern.get(j));
    					
    					if (nextPattern.size()!=0)
    						for(int k=0; k<nextPattern.size();k++)
    							  {
    							      String chng = merge(pattern.get(j), nextPattern.get(k));
    							      nextPattern.add(k, chng);
    							      nextPattern.remove(k+1);
    							      if (!NStructure.containsKey(chng))
    							        NStructure.put(chng, getNlist(chng)); 
    							      //System.out.println("\nNStructure now contains Nlist of "+chng);
    							  }
    					continue;
    		        }
    			
    					String FCI = merge(pattern.get(j), pattern.get(i));
    				//	System.out.println("FCI: "+ FCI);
    			        NList inter = getNlist(FCI);
    			      //  System.out.println("N-List: "+inter);
	        		    if (!NStructure.containsKey(FCI))
    			            NStructure.put(FCI, inter);
	        		    
	        		    if (inter!=null && inter.getfreq()>=threshold)
	        		    {
	        		      if (!nextPattern.contains(FCI)) nextPattern.add(FCI); 
	        		    }
    		        
    		} // inner for loop ends here
    		            
    		           if (SubsumptionCheck(pattern.get(i))==false)
    		           {	
    		        	    FCIs[++ind] = pattern.get(i);
    		                long fk = getNlist(pattern.get(i)).getfreq();
    			            if (!Hash.containsKey(fk))
    			            	{
    			            	  List<Integer> index_list = new ArrayList<Integer>();
    			            	  index_list.add(ind);
    			            	  Hash.put(fk, index_list);
    			            	}
    			            else
    			            {
    			            	Hash.get(fk).add(ind);
    			            }
    		           }       
    		          
          } // outer for loop ends here
                         Collections.reverse(nextPattern);
    	              
    	//here comes the recursive call
    	if (nextPattern.size()!=0)
    	findFCP(nextPattern);
    }

    //Subsumption Check of a given itemset
    private boolean SubsumptionCheck(String t)
    {	
        Long n = NStructure.get(t).getfreq();
        if (n<threshold) return true;
    	List<Integer> indx = Hash.get(n); 
    	if (indx != null) 
    	for(int j : indx)
    		if (checksubstring(t, FCIs[j]))// && NStructure.get(t).getfreq()==NStructure.get(FCIs[j]).getfreq())
    			return true;
        return false;
    }
	
	
	//generating frequent 1-itemsets and sorting them by their count
	private void generateL1() throws IOException {
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(transFile)));
		long counter = numTrans;
		
		while (counter>0) 
		{
		  String line = reader.readLine();
		  StringTokenizer stFile = new StringTokenizer(line, separator); 
		    while (stFile.hasMoreTokens())
		    {
		        String item = stFile.nextToken();
		     
				if (!count.containsKey(item)) count.put(item, 1l);
				else count.replace(item, count.get(item)+1);
		    }
		  counter--;
		}
		
		HashMap<String, Long> F1 = new HashMap<>();
		for(String i : count.keySet())
		{	if (count.get(i)>=threshold)
		      F1.put(i, count.get(i));
		}
	   L1 = sortHashMapByValues(F1);
	}
	
	//Inserting nodes of 1-itemsets into the PPC tree
	static int y = 0;
	private void InsertTree(String[] itemset, PPCtree Tr) 
	{
		List<PPCtree> child = Tr.getChildren(); 
	    int fg = 0;
	    String p = itemset[y];
	    y++;
		PPCtree childnode = null;
		
		if (child.size()==0) 
				childnode = new PPCtree(p, Tr, 1);
	    else
	    {
	    	for(PPCtree xx : child)
		   {
	    		
			if (xx.getName().compareTo(p)==0)
				{
				  xx.incrementCount(); 
				  childnode = xx;
				  fg = 1;
				}
		   }
			    if (fg==0)
				childnode = new PPCtree(p, Tr, 1);
	    }
	
		if (y<itemset.length)
		  InsertTree(itemset, childnode); 
	    
	}
	
	//Method to calculate pre-order ranks of the nodes in the PPC tree
    private void PreTraverse(PPCtree root) {
    	
    	List<PPCtree> child = root.getChildren();
    	time_pre++;
    	root.setPre(time_pre);
    	for(PPCtree xx : child)
    		  PreTraverse(xx);  
    }
    
    //Method to calculate post-order ranks of the nodes in the PPC tree
    private void PostTraverse(PPCtree root) {
    	List<PPCtree> child = root.getChildren();
    	for(PPCtree xx:child)     		
    		PostTraverse(xx);
    	time_post++;
    	root.setPost(time_post);
    }
	
    //Method to construct a PPC tree and return the root node
	public void PPCconstruct() throws IOException {
		root = new PPCtree("null", null, 0);
		root.setRoot();
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(transFile)));
		long counter = numTrans;
		//List<String> tranx = new ArrayList<String>();
		//for each transaction
		while (counter>0) 
		{
	      //sorting items in each transaction
		  String line = reader.readLine();
		 // System.out.println(line);
		  StringTokenizer stFile = new StringTokenizer(line, separator); 
		  HashMap<String, Long> local = new HashMap<String, Long>();
		    while (stFile.hasMoreTokens())
		    {
		        String item = stFile.nextToken();
				if (count.get(item)>=threshold)
					local.put(item, count.get(item));
		    }
		  LinkedHashMap<String, Long> res = sortHashMapByValues(local); 
		  String[] temp = new String[local.size()];
		  int i = 0;
		  for ( String key : res.keySet() ) {
			   temp[i++] = key;
			}
		
		  //call insert-tree
		  y = 0; 
		  InsertTree(temp, root);
		  counter--;
		 }
		
		 //Calculation of the Pre-Order and Post-Order rank
		 PreTraverse(root);
		 PostTraverse(root);
		
		 //Generating N-Lists of all 1-itemsets
		 genNlist(root);
	}
	
	//method to generate N-list of a 1-itemset
	private void genNlist(PPCtree xx) {
		List<PPCtree> children = xx.getChildren();
		if (xx!=null)
		{
			if (!NStructure.containsKey(xx.getName()))
			  {
				PPCode nw = new PPCode(xx.getPre(), xx.getPost(), xx.getCount());
				NList ls = new NList();
				ls.getCodes().add(nw);
				NStructure.put(xx.getName(), ls);
	
			  }
			else {
				NList ls = NStructure.get(xx.getName());
				PPCode nw = new PPCode(xx.getPre(), xx.getPost(), xx.getCount());
				ls.getCodes().add(nw);
			}
		}
		for(PPCtree y : children)
            genNlist(y);
	}
	
	//method to generate N-list of a k-itemset
	private NList getNlist(String itemset) {
		
		int l = itemset.length();
		if (l==1) return NStructure.get(itemset);
		NList n1 = NStructure.get(itemset.charAt(l-1)+"");
		NList n2 = NStructure.get(itemset.charAt(l-2)+"");
		NList res = NL_intersection(n1, n2);
		//System.out.println("N-List of "+itemset+" for "+ itemset.charAt(l-1) + " and "+itemset.charAt(l-2));
		for(int x = l-3; x>=0; x--) {
			
			NList cur = NL_intersection(res, NStructure.get(itemset.charAt(x)+""));
			res = cur;
		}
		
		return res;
	}
	
	//Auxilliary function to NL_intersection to check if the intersection is plausible
	private boolean AncestorCheck(NList n1, NList n2) 
	{
			PPCode x, y;
			int size_n1 = n1.getCodes().size();
		    int size_n2 = n2.getCodes().size();
			 
			for(int i=0, j=0; i<size_n1 && j<size_n2;) 
			{   
				x = n1.getCodes().get(i);
		    	y = n2.getCodes().get(j);
		    		
		    		if (PPCode.isAncestor(y, x))
		    		{
		    			return true;
		    		}
		    		else if (x.getpre()<y.getpre() && x.getpost()<y.getpost())  //not an ancestor - Case 1
		    			    i++;
		    		else if (x.getpre()>y.getpre())  //not an ancestor - Case 2
		    		        j++;
		    		else break;
			}
			return false;
	}
	
	//method to output N-List intersection of the two passed N-Lists
	private NList NL_intersection(NList n1, NList n2)
	{
	    NList res = new NList();
		PPCode x, y;
		if (!AncestorCheck(n1, n2)) {
			NList temp = n1;
			n1 = n2;
			n2 = temp;
		}
		int size_n1 = n1.getCodes().size();
	    int size_n2 = n2.getCodes().size();
		 
		for(int i=0, j=0; i<size_n1 && j<size_n2;) 
		{   
			x = n1.getCodes().get(i);
	    	y = n2.getCodes().get(j);
	    		
	    		if (PPCode.isAncestor(y, x))
	    		{
	    			PPCode newcode = new PPCode(y.getpre(), y.getpost(), x.getcount());
	    			if (res.getCodes().contains(newcode)) 
	    			{  
	    				int in = res.getCodes().indexOf(newcode);  //getting matching PPCode, if any
	    				res.getCodes().get(in).setcount(newcode.getcount()+res.getCodes().get(in).getcount());
	    			}
	    			else
	    				res.getCodes().add(newcode);
	    			i++;
	    		}
	    		else if (x.getpre()<y.getpre() && x.getpost()<y.getpost())  //not an ancestor - Case 1
	    			    i++;
	    		else if (x.getpre()>y.getpre())  //not an ancestor - Case 2
	    		        j++;
	    		else break;
		}
		
	    if (res.getCodes().size()!=0) {
	    	res.setItemName(merge(n2.getItems(),n1.getItems()));
	    	return res;
	    }
	    else return null;
	    
	}
	
	//to check if N-List N1 is a subset of N-List N2
	private boolean NListCheck(NList N1, NList N2) 
	{
		int i=0, j=0;
		while (j<N1.getSize() && i<N2.getSize())
		{
			if (PPCode.isAncestor(N2.getAt(i), N1.getAt(j)))
					j++;
			else i++;
		}
		if (j==N1.getSize())
			return true;
		else return false;
	}
	
	//To print Top K closed frequent itemsets
	private void printTopK(int k, int size) {
		if (k>ind+1) k=ind;
		LinkedHashMap<String, Long> fcps = new LinkedHashMap<String, Long>();
		if (size==-1)
			System.out.println("\nTop "+k+" closed frequent itemsets are:");
		else
			System.out.println("\nTop "+k+" closed frequent "+size+"-itemsets are:");
		for(int m=0; m<=ind; m++)
		{
			String s = FCIs[m];
			fcps.put(s, NStructure.get(s).getfreq());
		}
			
		fcps = sortHashMapByValues(fcps);
		//System.out.println(fcps);
		Iterator<String> itr = fcps.keySet().iterator();
		Long prev = 0l;
		Long sz = (long)size;
		List<String> topk = new ArrayList<String>();
		 for(int i=0;i<=k;)
		   {
			 if (!itr.hasNext()) break;
			 String pr = itr.next();
			 Long cur;
			 cur = NStructure.get(pr).getfreq();
			 if (cur==prev)
			 {     
				   if (sz==-1l || sz==pr.length() )
				   topk.add(pr);
			 }
			 else
			   {
				 i++; if (i<=k) {
					 if (size==-1l || sz==pr.length())
						 topk.add(pr);
				 }
			   }
			 prev = cur;
		   }
		   System.out.println(topk);
	}

	public static void main(String[] args) {
		try{
			Algo a = new Algo("C:\\Users\\asus\\Desktop\\sample.txt", 0.33 , 5, 6, "," );
			
			//Print fcps
		    System.out.println("\nClosed Itemsets:");
		    List<String> Is = new ArrayList<String>(a.L1.keySet());
		    a.findFCP(Is);
		    for(String i : a.FCIs)
		    {	
		    	if (i!=null)
		    	System.out.println(i + " # "+a.NStructure.get(i).getfreq()); 
		    }
		    
		    //Print Maximal FCPs
		    System.out.println("\nMaximal Itemsets: ");
		    a.findmaximal(a.FCIs);
		    for(String j : a.maximal)
		    	System.out.println(j);
		   
		    //printTopK(K, size_of_the_itemset) NOTE: Give size_of_the_itemset as -1 if not restricted
		    a.printTopK(10, -1);
		   
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	//merges s1 and s2 according to the support of item sets 
	private String merge(String s1, String s2) 
	{
		HashMap<String, Long> temp = new HashMap<String, Long>();
		for(int i=0; i<s1.length(); i++)
			temp.put(s1.charAt(i)+"", NStructure.get(s1.charAt(i)+"").getfreq());
		for(int i=0; i<s2.length(); i++)
			temp.put(s2.charAt(i)+"", NStructure.get(s2.charAt(i)+"").getfreq());
		LinkedHashMap<String, Long> resultant = sortHashMapByValues(temp);
		String f = "";
		for(String n:resultant.keySet())
			f+=n;
		return f;
	}
	
	//to check if the string s1 is a substring of s2
	private boolean checksubstring(String s1, String s2) {
		Set<Character> st1 = new LinkedHashSet<Character>(); //unordered set of chars
		Set<Character> st2 = new LinkedHashSet<Character>();
		int l1 = s1.length(), l2 = s2.length();
		for(int i=0; i<l1; i++)
			st1.add(s1.charAt(i));
		for(int i=0; i<l2; i++)
			st2.add(s2.charAt(i));
		//System.out.print(s1+" is changed to ");
		st1.retainAll(st2);   s1.contains(s2);
		//System.out.println(st1.toString()+" for "+s2);
		if (st1.size()==l1) return true;
		else return false;
	}
}