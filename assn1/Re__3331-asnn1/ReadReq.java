import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadReq extends Request{

	private static final long serialVersionUID = 1L;

	private final int lineNumber;
	private final List<Integer> readPosts;

	public ReadReq(String command, String userName, String bookName, int pageNumber, String lineNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.lineNumber = (Integer.parseInt(lineNumber)-1);
		this.readPosts = readPosts;
	}

	public Response localProcess(List<Discussions> localDB) {
		
		List<Discussions> filteredLocalDB = new ArrayList<Discussions>();
		Set<Integer> localDBSet = new HashSet<Integer>();
		Set<Integer> readPostsSet = new HashSet<Integer>(this.readPosts);
		
		for (Discussions post : localDB) {
			if (post.getBookName().equals(this.bookName) && post.getPageNumber() == this.pageNumber &&
					post.getLineNumber() == this.lineNumber) {
				filteredLocalDB.add(post);
			}
		}
		
		for (Discussions post : filteredLocalDB) {
			localDBSet.add(post.getSerialNum());
		}
		
		localDBSet.removeAll(readPostsSet);
		
		if (localDBSet.size() == 0) {
			return new ResponseMSG("No new posts here");
		} else {
			List<String> unreadPosts = new ArrayList<String>();
			for (int i : localDBSet) {
				for (Discussions post : localDB) {
					if (post.getSerialNum() == i) {
						unreadPosts.add(post.toStrings());
						readPosts.add(post.getSerialNum());
					}
				}
			}
			return new RespRead(unreadPosts, readPosts);
		}
	}

	@Override
	public Response process(Database db, String mode) {
		
		Set<Integer> readSet = new HashSet<Integer>(readPosts);
		Set<Integer> lineSet = new HashSet<Integer>();
		
		Pages p = db.search1(this.bookName, this.pageNumber);
		
		if (p == null) {
			return new ResponseMSG("Book and/or page does not exist");
		}
		
		if (this.lineNumber >= p.getLines().size() || this.lineNumber <= 0) {
			return new ResponseMSG("Invalid Line Number");
		}

		//calculate which posts are new posts
		LineList l = p.getLines().get(this.lineNumber);
		
		for (Discussions post : l.getDiscussionPost()) {
			lineSet.add(post.getSerialNum());
		}
		
		lineSet.removeAll(readSet);
		
		if (lineSet.size() == 0) {
			return new ResponseMSG("No new posts here");
		} else {
			List<String> unreadPosts = new ArrayList<String>();
			unreadPosts.add("Book by " + p.getBookName() + ", Page " + p.getPageNumber() + ", Line number " 
			+ this.lineNumber+1 + ":\n");
		
			for (int i : lineSet) {
				for (Discussions post : l.getDiscussionPost()) {
					if (post.getSerialNum() == i) {
						unreadPosts.add(post.toStrings());
						readPosts.add(post.getSerialNum());
					}
				}
			}
			return new RespRead(unreadPosts, readPosts);
		}
	}

}