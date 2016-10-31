
public class ResponseMSG implements Response{

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public ResponseMSG (String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
