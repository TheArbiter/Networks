import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PingRequest extends Request {

	private static final long serialVersionUID = 1L;
	
	private List<Integer> postsRead;

	public PingRequest(String command, String userName, String bookName, int pageNumber, List<Integer> posts) {
		super(command, userName, bookName, pageNumber);
		this.postsRead = posts;
	}

	@Override
	public Response process(Database db, String mode) {
		Set<Integer> readPostsSet = new HashSet<Integer>(this.postsRead);
		Set<Integer> pagePostsSet = new HashSet<Integer>();
		
		Page p = db.search1(bookName, pageNumber);
		if (p == null) {
			System.out.println("Book and/or page does not exist");
		}

		for (Line l : p.getLines()) {			
			for (DiscussionPost post : l.getDiscussionPost()) {
				pagePostsSet.add(post.getSerialNum());
			}
		}
		pagePostsSet.removeAll(readPostsSet);
		if (!pagePostsSet.isEmpty()) {
			return new PingResponse(true);
		}
		return new PingResponse(false);
	}

}
