import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DisplayRes implements Response{

	private static final long serialVersionUID = 1L;
	private Pages p;
	public List<Character> postTag = null;
	
	public DisplayRes (Pages p) {
		this.p = p;
		this.postTag = null;
	}

	public DisplayRes (Pages p, List<Character> postTag) {
		this(p);
		this.postTag = postTag;
	}

	public Pages getPage() {
		return this.p;
	}
	
	public String toString (String currentBookName, int currentPageNumber, List<Discussions> postdb, List<Integer> readPosts) {
		System.out.println("push version");
		
		List<Discussions> filteredPostDB = new ArrayList<Discussions>();
		List<Set<Integer>> filPostDBSet = new ArrayList<Set<Integer>>();
		Set<Integer> readPostSet = new HashSet<Integer>(readPosts);
		Set<Integer> filteredPostDBSet = new HashSet<Integer>();
		this.postTag = new ArrayList<Character>();
		
		for (Discussions post : postdb) {
			if (post.getBookName().equals(currentBookName) && post.getPageNumber() == currentPageNumber) {
				filteredPostDB.add(post);
			}
		}
		
		for (int i = 0; i < this.p.getLines().size(); i++) {
			filPostDBSet.add(new HashSet<Integer>());
		}
		
		for (Discussions post : filteredPostDB) {
			filteredPostDBSet.add(post.getSerialNum());
			filPostDBSet.get(post.getLineNumber()).add(post.getSerialNum());
		}
		
//		for (DiscussionPost post : filteredPostDB) {
			
//		}
		
		for (int i = 0; i < filPostDBSet.size(); i++) {
			if (filPostDBSet.get(i).size() == 0) {
				//No Post
				this.postTag.add(i, ' ');
			}
			else if (readPostSet.containsAll(filPostDBSet.get(i))) {
				//Post Read
				this.postTag.add(i, 'm');
			}
			else if (!readPostSet.containsAll(filPostDBSet.get(i))) {
				//New Post
				this.postTag.add(i, 'n');
			}
		}
		return toString();
		
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < this.p.getLines().size(); i++) {
			if (this.postTag != null) {
				out.append(postTag.get(i));
			}
			out.append(this.p.getLines().get(i).getLine() + '\n');
		}

		return out.toString();
	}

}
