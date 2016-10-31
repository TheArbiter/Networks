import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReqPing extends Request {

	private static final long serialVersionUID = 1L;
	
	private List<Integer> postsRead;

	public ReqPing(String command, String userName, String bookName, int pageNumber, List<Integer> posts) {
		super(command, userName, bookName, pageNumber);
		this.postsRead = posts;
	}

	@Override
	public Response process(Database db, String mode) {
		Set<Integer> readPostsSet = new HashSet<Integer>(this.postsRead);
		Set<Integer> pagePostsSet = new HashSet<Integer>();
		
		Pages p = db.search1(bookName, pageNumber);
		if (p == null) {
			return new ResponseMSG("Book and/or page does not exist");
		}

		for (LineList l : p.getLines()) {			
			for (Discussions post : l.getDiscussionPost()) {
				pagePostsSet.add(post.getSerialNum());
			}
		}
		pagePostsSet.removeAll(readPostsSet);
		if (!pagePostsSet.isEmpty()) {
			return new RespPing(true);
		}
		return new RespPing(false);
	}

}
