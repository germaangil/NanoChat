package es.um.redes.nanoChat.messageFV;

/*
 * ROOM
----

operation:<operation>
nick:<nick>
message:<message>

Defined operations:

*/

public class NCServerMessage extends NCMessage {
	
private String nick;
private String message;
	
	//Campo específico de este tipo de mensaje
	static protected final String NICK= "nick";
	static protected final String MESSAGE = "message";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCServerMessage(byte type, String nick, String message) {
		this.opcode = type;
		this.nick = nick;
		this.message = message;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();			
		sb.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); //Construimos el campo
		sb.append(NICK+DELIMITER+nick+END_LINE); //Construimos el campo
		sb.append(MESSAGE+DELIMITER+message+END_LINE); //Construimos el campo
		sb.append(END_LINE);  //Marcamos el final del mensaje
		return sb.toString(); //Se obtiene el mensaje
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCServerMessage readFromString(byte code, String message) {
		String[] lines = message.split(String.valueOf(END_LINE));
		String nick = null;
		String messageContent = null;
		for(int i=1; i<3; i++) {
			int idx = lines[i].indexOf(DELIMITER); // Posición del delimitador
			String field = lines[i].substring(0, idx).toLowerCase();                                                                                                                                                // minúsculas
			String value = lines[i].substring(idx + 1).trim();
			if (field.equalsIgnoreCase(NICK))
				nick = value;
			if (field.equalsIgnoreCase(MESSAGE))
				messageContent = value;
		}

		return new NCServerMessage(code, nick, messageContent);
	}

	public String getNick() {
		return nick;
	}
	
	public String getMessage() {
		return message;
	}
}
