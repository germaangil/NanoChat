package es.um.redes.nanoChat.messageFV;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

/*
 * ROOMLIST
----

operation:<operation>
name:<name>
lastMessage:<lastmessage>
members:<members>

Defined operations:
RoomList
*/

public class NCRoomListMessage extends NCMessage {

	private List<NCRoomDescription> list_rooms;

	// Campo específico de este tipo de mensaje
	static protected final String NAME = "name";
	static protected final String LAST_MESSAGE = "lastMessage";
	static protected final String MEMBERS = "members";
	static protected final String COMA = ",";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCRoomListMessage(byte type, List<NCRoomDescription> list_rooms) {
		this.opcode = type;
		this.list_rooms = list_rooms;
	}

	// Pasamos los campos del mensaje a la codificación correcta en field:value
	@Override
	public String toEncodedString() {
		StringBuffer stringB = new StringBuffer();
		stringB.append(OPCODE_FIELD + DELIMITER + opcodeToOperation(opcode) + END_LINE); 
		if (list_rooms != null) {
			for (NCRoomDescription room : list_rooms) {
				// Construimos los campos del mensaje
				stringB.append(NAME + DELIMITER + room.roomName + END_LINE); 
				stringB.append(LAST_MESSAGE + DELIMITER + Long.toString(room.timeLastMessage) + END_LINE);
																																																// campo
				if (room.members.size() <= 0) {
					stringB.append(MEMBERS + DELIMITER + "No hay miembros" + END_LINE); 
				} else {
					String stringMembers = "";
					for (String member : room.members) {
						stringMembers+= member;
						if (member != room.members.get(room.members.size() - 1))
							stringMembers+= COMA;
					}
					stringB.append(MEMBERS + DELIMITER + stringMembers + END_LINE); // Construimos el campo
				}

			}
		}
		stringB.append(END_LINE); // Marcamos el final del mensaje
		return stringB.toString(); // Se obtiene el mensaje
	}

	public static NCRoomListMessage readFromString(byte code, String message) {
		String[] filas = message.split(String.valueOf(END_LINE));
		List<NCRoomDescription> listRooms = null;
		int i = 1;
		while (i < filas.length) {
			String name = null;
			Long lastMessage = null;
			List<String> members = new ArrayList<>();
			// Variable para establecer la posicion del delimitador
			int posiD = filas[i].indexOf(DELIMITER); 
			// COmprobacion del campo name
			String field = filas[i].substring(0, posiD).toLowerCase(); 
			String value = filas[i].substring(posiD + 1).trim();
			// Comprobamos si el primer campo es un nombre 
			if (field.equalsIgnoreCase(NAME))
				// SI lo es guardamos el valor del nombre en la variable value
				name = value;
			i++;
			// Comprobacion del campo Last Message
			posiD = filas[i].indexOf(DELIMITER);
			field = filas[i].substring(0, posiD).toLowerCase(); 
			value = filas[i].substring(posiD + 1).trim();
			if (field.equalsIgnoreCase(LAST_MESSAGE))
				lastMessage = Long.parseLong(value);
			i++;
			// Comporbacion del campo Miembros
			posiD = filas[i].indexOf(DELIMITER);
			field = filas[i].substring(0, posiD).toLowerCase(); 
			value = filas[i].substring(posiD + 1).trim();
			if (field.equalsIgnoreCase(MEMBERS)) {
				String[] miembros = value.split(COMA);
			
				Collections.addAll(members, miembros);
			}
			if (listRooms == null) {
				listRooms = new ArrayList<NCRoomDescription>();
			}
			NCRoomDescription sala = new NCRoomDescription(name, members, lastMessage);
			listRooms.add(sala);
			i++;
		}

		return new NCRoomListMessage(code, listRooms);
	}

	public List<NCRoomDescription> getListRooms() {
		return Collections.unmodifiableList(list_rooms);// no se si se puede pasar tal cual list_rooms
	}
}
