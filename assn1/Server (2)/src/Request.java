import java.io.Serializable;

public abstract class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	protected final String userName;
	protected final String bookName;
	protected final int pageNumber;
	private final String command;
	
	public Request(String command, String userName, String bookName, int pageNumber) {
		this.userName = userName;
		this.bookName = bookName;
		this.pageNumber = pageNumber;
		this.command = command;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public abstract Response process(Database db, String mode);

	public String getUserName() {
		return this.userName;
	}
	
}
