import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class LineList implements Serializable{

	private static final long serialVersionUID = 1L;
	private final int lineNumber;
	private final String line;
	private List<Discussions> discussionPost;
	
	public LineList(int lineNumber, String line) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.discussionPost = new Vector<Discussions>();
	}

	public void addPost (Discussions post) {
		this.discussionPost.add(post);
		System.out.println("Post added to database and given the serial number " + post.getSerialNum());
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public String getLine() {
		return line;
	}
		
	public List<Discussions> getDiscussionPost () {
		return this.discussionPost;
	}
	
	

}
