import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadRequest extends Request{

	private static final long serialVersionUID = 1L;

	private final int lineNumber;
	private final List<Integer> readPosts;

	public ReadRequest(String command, String userName, String bookName, int pageNumber, String lineNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.lineNumber = (Integer.parseInt(lineNumber)-1);
		this.readPosts = readPosts;
	}

	public Response localProcess(List<DiscussionPost> localDB) {
		
		List<DiscussionPost> filteredLocalDB = new ArrayList<DiscussionPost>();
		Set<Integer> localDBSet = new HashSet<Integer>();
		Set<Integer> readPostsSet = new HashSet<Integer>(this.readPosts);
		
		for (DiscussionPost post : localDB) {
			if (post.getBookName().equals(this.bookName) && post.getPageNumber() == this.pageNumber &&
					post.getLineNumber() == this.lineNumber) {
				filteredLocalDB.add(post);
			}
		}
		
		for (DiscussionPost post : filteredLocalDB) {
			localDBSet.add(post.getSerialNum());
		}
		
		localDBSet.removeAll(readPostsSet);
		
		if (localDBSet.size() == 0) {
			return new MessageResponse("No new posts here");
		} else {
			List<String> unreadPosts = new ArrayList<String>();
			for (int i : localDBSet) {
				for (DiscussionPost post : localDB) {
					if (post.getSerialNum() == i) {
						unreadPosts.add(post.toStrings());
						readPosts.add(post.getSerialNum());
					}
				}
			}
			return new ReadResponse(unreadPosts, readPosts);
		}
	}

	@Override
	public Response process(Database db, String mode) {
		
		Set<Integer> readSet = new HashSet<Integer>(readPosts);
		Set<Integer> lineSet = new HashSet<Integer>();
		
		Page p = db.search1(this.bookName, this.pageNumber);
		
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		
		if (this.lineNumber >= p.getLines().size() || this.lineNumber <= 0) {
			return new MessageResponse("Invalid Line Number");
		}

		//calculate which posts are new posts
		Line l = p.getLines().get(this.lineNumber);
		
		for (DiscussionPost post : l.getDiscussionPost()) {
			lineSet.add(post.getSerialNum());
		}
		
		lineSet.removeAll(readSet);
		
		if (lineSet.size() == 0) {
			return new MessageResponse("No new posts here");
		} else {
			List<String> unreadPosts = new ArrayList<String>();
			unreadPosts.add("Book by " + p.getBookName() + ", Page " + p.getPageNumber() + ", Line number " 
			+ this.lineNumber+1 + ":\n");
		
			for (int i : lineSet) {
				for (DiscussionPost post : l.getDiscussionPost()) {
					if (post.getSerialNum() == i) {
						unreadPosts.add(post.toStrings());
						readPosts.add(post.getSerialNum());
					}
				}
			}
			return new ReadResponse(unreadPosts, readPosts);
		}
	}

}