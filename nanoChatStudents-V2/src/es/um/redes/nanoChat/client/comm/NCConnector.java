package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCRoomListMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCServerMessage;
import es.um.redes.nanoChat.messageFV.NCSimpleMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;

	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		// TODO Se crea el socket a partir de la dirección proporcionada
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
		// TODO Se extraen los streams de entrada y salida
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}

	// Método para registrar el nick en el servidor. Nos informa sobre si la
	// inscripción se hizo con éxito o no.
	public boolean registerNickname_UnformattedMessage(String nick) throws IOException {
		// Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		// TODO Enviamos una cadena con el nick por el flujo de salida
		// TODO Leemos la cadena recibida como respuesta por el flujo de entrada
		// TODO Si la cadena recibida es NICK_OK entonces no está duplicado (en función
		// de ello modificar el return)
		return true;
	}

	// Método para registrar el nick en el servidor. Nos informa sobre si la
	// inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		// Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		// Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se
		// inserte el nick
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_REGISTER_NICK, nick);
		// Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		// Escribimos el mensaje en el flujo de salida, es decir, provocamos que se
		// envíe por la conexión TCP
		dos.writeUTF(rawMessage);
		// TODO Leemos el mensaje recibido como respuesta por el flujo de entrada
		NCSimpleMessage answer = (NCSimpleMessage) NCMessage.readMessageFromSocket(dis);
		// TODO Analizamos el mensaje para saber si está duplicado el nick (modificar el
		// return en consecuencia)
		// Obetenemos el opcode
		Byte opcode = answer.getOpcode();
		// Devolvemos cierto si el opcode coincide con la operacion
		return (opcode == NCMessage.OP_OK_NICK);
	}

	// Método para obtener la lista de salas del servidor
	public List<NCRoomDescription> getRooms() throws IOException {
		// Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		// TODO completar el método
		NCSimpleMessage message = (NCSimpleMessage) NCMessage.makeSimpleMessage(NCMessage.OP_QUERY_ROOMS);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		NCRoomListMessage answer = (NCRoomListMessage) NCMessage.readMessageFromSocket(dis);
		// Devolvemos la lista de las salas
		return answer.getListRooms();
	}

	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or
		// RCV(REJECT)
		// TODO completar el método
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_ENTER_ROOM, room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		NCSimpleMessage answer = (NCSimpleMessage) NCMessage.readMessageFromSocket(dis);
		// Obetenemos el opcode
		Byte opcode = answer.getOpcode();
		// Devolvemos cierto si el opcode coincide con la operacion
		return (opcode == NCMessage.OP_OK_ENTER);
	}

	// Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(EXIT_ROOM)
		// TODO completar el método
		NCSimpleMessage message = (NCSimpleMessage) NCMessage.makeSimpleMessage(NCMessage.OP_EXIT_ROOM);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}

	// Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}

	// IMPORTANTE!!
	// TODO Es necesario implementar métodos para recibir y enviar mensajes de chat
	// a una sala
	public void sendChatMessage(String chatMessage) throws IOException {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_SEND_MESSAGE, chatMessage);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
	}

	// TODO Funcion que indica si se ha enviado un msenaje privado correctamente
	public boolean sendPrivMessage(String nickname, String privateMessage) {
		NCServerMessage message = (NCServerMessage) NCMessage.makeServerMessage(NCMessage.OP_SEND_PRIVATE, nickname,
				privateMessage);
		String rawMessage = message.toEncodedString();
		try {
			dos.writeUTF(rawMessage);
			NCSimpleMessage answer = (NCSimpleMessage) NCMessage.readMessageFromSocket(dis);
			// Obetenemos el opcode
			Byte opcode = answer.getOpcode();
			// Delvemos true si el opcode coincide con la operacion
			return (opcode == NCMessage.OP_OK_PRIVATE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	// TODO Método para solicitar un cambio de nombre de la sala
	public void renameRoom(String name) {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_RENAME, name);
		String rawMessage = message.toEncodedString();
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO Método que notofica a los usuarios de la sala que se ha cambaido el
	// nombre de la sala
	public void renameOk(String name) {
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_RENAME_ROOM, name);
		String rawMessage = message.toEncodedString();
		try {
			dos.writeUTF(rawMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NCMessage processIncomingMessage() {

		NCMessage answer = null;
		try {
			answer = NCMessage.readMessageFromSocket(dis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return answer;
	}

	// Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		// Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		// TODO Construimos el mensaje de solicitud de información de la sala específica
		NCRoomMessage message = (NCRoomMessage) NCMessage.makeRoomMessage(NCMessage.OP_INFO, room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		// TODO Recibimos el mensaje de respuesta
		NCRoomListMessage answer = (NCRoomListMessage) NCMessage.readMessageFromSocket(dis);
		// TODO Devolvemos la descripción contenida en el mensaje
		return answer.getListRooms().get(0);
	}

	// Método para cerrar la comunicación con la sala
	// TODO (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null;
		}
	}

}

