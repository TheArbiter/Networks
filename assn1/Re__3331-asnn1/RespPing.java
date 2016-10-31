
public class RespPing implements Response {

	private static final long serialVersionUID = 1L;
	
	private final boolean status;
	
	public RespPing (boolean status) {
		this.status = status;
	}
	
	public boolean getStatus() {
		return this.status;
	}
	
	public String toString() {
		return this.status ? "There are new posts.\n" : "";
	}

}
