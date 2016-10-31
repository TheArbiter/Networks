
public class PostRequest extends Request{

	private static final long serialVersionUID = 1L;

	private final int lineNumber;
	private final String content;

	public PostRequest(String command, String userName, String bookName, int pageNumber, String lineNumber, String content) {
		super(command, userName, bookName, pageNumber);
		this.lineNumber = Integer.parseInt(lineNumber)-1;
		this.content = content;
	}

	@Override
	public Response process(Database db, String mode) {
		Page p = db.search1(this.bookName, this.pageNumber);
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		if (this.lineNumber >= p.getLines().size() || this.lineNumber < 0) {
			return new MessageResponse("Invalid Line Number");
		}
		p.addDiscussionPost(new DiscussionPost(db.generateSerialId(), this.userName, this.bookName,
				this.pageNumber, this.lineNumber, this.content));
		return new MessageResponse("Post Successful");
	}

}
