import java.util.List;


public class NotificationPushing implements Response {
	
	private Discussions post;
	private String pushMessage;
	
	public NotificationPushing (Discussions post) {
		this.post = post;
		this.pushMessage = "";
	}

	private static final long serialVersionUID = 1L;
	
	public void updateLocalDB (List<Discussions> postDB, List<Integer> readPosts) {
		postDB.add(this.post);
	}
	
	public String toString(String currentBookName, int currentPageNumber) {
		if (this.post.getBookName().equals(currentBookName) && this.post.getPageNumber() == currentPageNumber) {
			this.pushMessage = "There are new posts.\n";
		}
		return toString();
	}

	@Override
	public String toString() {
		return this.pushMessage;
	}

}
