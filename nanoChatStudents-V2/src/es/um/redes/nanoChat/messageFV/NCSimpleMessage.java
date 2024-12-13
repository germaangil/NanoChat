package es.um.redes.nanoChat.messageFV;

/*
 * ROOM
----

operation:<operation>

Defined operations:
DuplicatedNick
OkNick
*/

public class NCSimpleMessage extends NCMessage{


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCSimpleMessage(byte opcode) {
		this.opcode = opcode;
	}

	//Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer stringB = new StringBuffer();			
		// Constrimos los campos del mensaje
		stringB.append(OPCODE_FIELD+DELIMITER+opcodeToOperation(opcode)+END_LINE); 
		stringB.append(END_LINE); 
		// Devolvemos el mensaje
		return stringB.toString(); 
	}

	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCSimpleMessage readFromString(byte code, String message) {

		return new NCSimpleMessage(code);
	}

}
