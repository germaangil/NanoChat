package es.um.redes.nanoChat.messageFV;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import java.util.List;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public abstract class NCMessage {
	protected byte opcode;

	// TODO Implementar el resto de los opcodes para los distintos mensajes
	public static final byte OP_INVALID_CODE = 0;
	public static final byte OP_REGISTER_NICK = 1;
	public static final byte OP_DUPLICATED_NICK = 2;
	public static final byte OP_OK_NICK = 3;
	public static final byte OP_ROOM_LIST = 4;
	public static final byte OP_QUERY_ROOMS = 5;
	public static final byte OP_ENTER_ROOM = 6;
	public static final byte OP_OK_ENTER = 7;
	public static final byte OP_NO_ROOM = 8;
	public static final byte OP_EXIT_ROOM = 9;
	public static final byte OP_INFO = 10;
	public static final byte OP_ROOM_INFO = 11;
	public static final byte OP_SEND_MESSAGE = 12;
	public static final byte OP_SERVER_MESSAGE = 13;
	public static final byte OP_RENAME = 14;
	public static final byte OP_RENAME_ROOM = 15;
	public static final byte OP_SEND_PRIVATE = 16;
	public static final byte OP_FAIL_PRIVATE = 17;
	public static final byte OP_OK_PRIVATE = 18;

	// Constantes con los delimitadores de los mensajes de field:value
	public static final char DELIMITER = ':'; // Define el delimitador
	public static final char END_LINE = '\n'; // Define el carácter de fin de línea

	public static final String OPCODE_FIELD = "operation";

	/**
	 * Códigos de los opcodes válidos El orden es importante para relacionarlos con
	 * la cadena que aparece en los mensajes
	 */
	private static final Byte[] _valid_opcodes = { OP_REGISTER_NICK, OP_DUPLICATED_NICK, OP_OK_NICK, OP_QUERY_ROOMS,
			OP_ROOM_LIST, OP_ENTER_ROOM, OP_OK_ENTER, OP_NO_ROOM, OP_EXIT_ROOM, OP_INFO, OP_ROOM_INFO, OP_SEND_MESSAGE,
			OP_SERVER_MESSAGE, OP_RENAME, OP_RENAME_ROOM, OP_SEND_PRIVATE, OP_FAIL_PRIVATE, OP_OK_PRIVATE };

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_operations_str = { "RegisterNick", "DuplicatedNick", "OkNick", "roomList",
			"SendRooms", "enterRoom", "OkEnter", "NoRoom", "ExitRoom", "Info", "RoomInfo", "SendMessage",
			"ServerMessage", "Rename", "RenameRoom", "SendPrivate", "FailPrivate", "OkPrivate" };

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OP_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	protected static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}

	// Devuelve el opcode del mensaje
	public byte getOpcode() {
		return opcode;
	}

	// Método que debe ser implementado específicamente por cada subclase de
	// NCMessage
	protected abstract String toEncodedString();

	// Extrae la operación del mensaje entrante y usa la subclase para parsear el
	// resto del mensaje
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {
		String message = dis.readUTF();
		String[] lines = message.split(String.valueOf(END_LINE));
		if (!lines[0].isEmpty()) { // Si la línea no está vacía
			int idx = lines[0].indexOf(DELIMITER); // Posición del delimitador
			String field = lines[0].substring(0, idx).toLowerCase();// minúsculas
			String value = lines[0].substring(idx + 1).trim();
			if (!field.equalsIgnoreCase(OPCODE_FIELD))
				return null;
			byte code = operationToOpcode(value);
			if (code == OP_INVALID_CODE)
				return null;
			switch (code) {
			case OP_REGISTER_NICK: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_DUPLICATED_NICK: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_OK_NICK: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_QUERY_ROOMS: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_ROOM_LIST: {
				return NCRoomListMessage.readFromString(code, message);
			}
			case OP_ENTER_ROOM: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_OK_ENTER: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_NO_ROOM: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_INFO: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_ROOM_INFO: {
				return NCRoomListMessage.readFromString(code, message);
			}
			case OP_SEND_MESSAGE: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_SERVER_MESSAGE: {
				return NCServerMessage.readFromString(code, message);
			}
			case OP_EXIT_ROOM: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_RENAME: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_RENAME_ROOM: {
				return NCRoomMessage.readFromString(code, message);
			}
			case OP_SEND_PRIVATE: {
				return NCServerMessage.readFromString(code, message);
			}
			case OP_FAIL_PRIVATE: {
				return NCSimpleMessage.readFromString(code, message);
			}
			case OP_OK_PRIVATE: {
				return NCSimpleMessage.readFromString(code, message);
			}
			default:
				System.err.println("Unknown message type received:" + code);
				return null;
			}
		} else
			return null;
	}

	// Método para construir un mensaje de tipo Room a partir del opcode y del
	// nombre
	public static NCMessage makeRoomMessage(byte code, String name) {
		return new NCRoomMessage(code, name);
	}

	public static NCMessage makeSimpleMessage(byte code) {
		return new NCSimpleMessage(code);
	}

	public static NCMessage makeRoomListMessage(byte code, List<NCRoomDescription> list_rooms) {
		return new NCRoomListMessage(code, list_rooms);
	}

	public static NCMessage makeServerMessage(byte code, String nick, String content) {
		return new NCServerMessage(code, nick, content);
	}

}
