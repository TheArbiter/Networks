import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Pages implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final String bookName;
	private final int pageNumber;
	private List<LineList> lines;
	
	public Pages(List<String> lines, String book, String page){
		this.bookName = book;
		this.pageNumber = Integer.parseInt(page);
		this.lines = new ArrayList<LineList>();
		for(int i = 0; i < lines.size(); i++){
			this.lines.add(new LineList(i,lines.get(i)));
		}
	}
	
	public void addDiscussionPost(Discussions post) {
		LineList l = this.lines.get(post.getLineNumber());
		l.addPost(post);
	}
	
	public String getBookName() {
		return this.bookName;
	}

	public int getPageNumber() {
		return this.pageNumber;
	}
	
	public List<LineList> getLines() {
		return this.lines;
	}
	
}
