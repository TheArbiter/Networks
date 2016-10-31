import java.io.Serializable;

public class Discussions implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final int serialNum;
	private final int pageNumber;
	private final int lineNumber;
	private final String userName;
	private final String bookName;
	private final String post;
	
	
	public Discussions (int serialNum, String userName, String bookName, int pageNumber, int lineNumber, String post) {
		this.serialNum = serialNum;
		this.userName = userName;
		this.bookName = bookName;
		this.pageNumber = pageNumber;
		this.lineNumber = lineNumber;
		this.post = post;
	}
	
	public int getSerialNum() {
		return this.serialNum;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getBookName() {
		return bookName;
	}
	
	public int getPageNumber() {
		return pageNumber;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getPost() {
		return this.post;
	}
	
	public String toStrings() {
		return this.getSerialNum() + " " + this.getUserName() + " " + this.getPost();
	}

	

	

}
