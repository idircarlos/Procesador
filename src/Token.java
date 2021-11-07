public class Token {
	
	private Integer id;
	private Object  val;
	
	public Token(Integer id, Object val) {
		this.id = id;
		this.val = val;
	}
	
	public Integer getId() {
		return id;
	}

	public Object getVal() {
		return val;
	}

	public String toString() {
		if (val == null) val = new String("");
		return "<" + id + "," + val + ">";
	}
	
}
