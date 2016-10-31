import java.util.List;

public class RespRead implements Response {

	private static final long serialVersionUID = 1L;
	
	private List<String> unreadPosts;
	private List<Integer> readPosts;
	
	
	public RespRead (List<String> unreadPosts, List<Integer> readPosts) {
		this.unreadPosts = unreadPosts;
		this.readPosts = readPosts;
	}
	
	public List<Integer> getReadPosts() {
		return this.readPosts;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (String s : unreadPosts) {
			out.append(s + '\n');
		}
		return out.toString();
	}
	

}
