package server;

public enum MessageType {
	SERVER_MESSAGE (0),
	CONNECT (1),
	DISCONNECT (2),
	ADD_TOWER (3),
	ADD_MONSTER (4),
	REQUEST_STATE (5),
	REQUEST_ID (6),
	PROVIDE_ID (7);
	
	private final int index;
	
	MessageType(int index){
		this.index = index;
	}
	
	public char value(){
		return (char) index;
	}
	
	public static MessageType translate(char value){
		return MessageType.values()[(int) value];
	}
}
