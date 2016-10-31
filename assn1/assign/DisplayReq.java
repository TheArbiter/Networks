import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DisplayReq extends Request{

	private static final long serialVersionUID = 1L;
	private final List<Integer> postsRead;

	public DisplayReq(String command, String userName, String bookName, int pageNumber, List<Integer> posts) {
		super(command, userName, bookName, pageNumber);
		this.postsRead = posts;
	}

	@Override
	public Response process(Database db, String mode) {
		Pages p = db.search1(bookName, pageNumber);
		if (p == null) {
			return new ResponseMSG("Book and/or page does not exist");
		}
		
		if (mode.equals("pull")) {
			System.out.println("Querying discussion posts");
			
			//calculating which lines have unread posts
			List<Character> postTag = new ArrayList<Character>(p.getLines().size());

			Set<Integer> readPostsSet = new HashSet<Integer>(this.postsRead);
			for (LineList l : p.getLines()) {
				char mark;
				if (l.getDiscussionPost().size() == 0) {
					mark = ' ';
				} else {
					Set<Integer> lineDiscussionSet = new HashSet<Integer>();
					for (Discussions post : l.getDiscussionPost()) {
						lineDiscussionSet.add(post.getSerialNum());
					}
					//Post Read
					if (readPostsSet.containsAll(lineDiscussionSet)) {
						mark = 'm';
					//Post not read	
					} else {
						mark = 'n';
					}
				}
				postTag.add(mark);
				
			}
			return new DisplayRes(p, postTag);
		} else {	//PUSH mode
			return new DisplayRes(p);
		}
		
	}
}
