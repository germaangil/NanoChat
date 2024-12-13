package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCRoomListMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCServerMessage;
import es.um.redes.nanoChat.messageFV.NCSimpleMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {
	
	private Socket socket = null;
	//Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Usuario actual al que atiende este Thread
	String user;
	//RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	//Sala actual
	String currentRoom;

	//Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	//Main loop
	public void run() {
		try {
			//Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			//Mientras que la conexión esté activa entonces...
			while (true) {
				//TODO Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getOpcode()) {
				//TODO 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
				case NCMessage.OP_QUERY_ROOMS :
				{
					sendRoomList();
					break;
				}
				//TODO 2) Si se nos pide entrar en la sala entonces obtenemos el RoomManager de la sala,
				case NCMessage.OP_ENTER_ROOM :
				{
					NCRoomManager rm = serverManager.enterRoom(user, ((NCRoomMessage) message).getName(), socket);
					if(rm == null) {
						NCSimpleMessage answer = new NCSimpleMessage(NCMessage.OP_NO_ROOM);
						String response = answer.toEncodedString();
						dos.writeUTF(response);
					}
					else {
						roomManager = rm;
						currentRoom = ((NCRoomMessage) message).getName();
						NCSimpleMessage answer = new NCSimpleMessage(NCMessage.OP_OK_ENTER);
						String response = answer.toEncodedString();
						dos.writeUTF(response);
						processRoomMessages();
					}
					break;
				}
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		}
		finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	//Obtenemos el nick y solicitamos al ServerManager que verifique si está duplicado
	private void receiveAndVerifyNickname() {
		//La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
		//TODO Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado
		boolean registred = false;
		while(!registred) {
		//TODO Extraer el nick del mensaje
			NCRoomMessage message;
			try {
				message = (NCRoomMessage) NCMessage.readMessageFromSocket(dis);
				user = message.getName();
				registred = serverManager.addUser(user);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//TODO Validar el nick utilizando el ServerManager - addUser()
			
		//TODO Contestar al cliente con el resultado (éxito o duplicado)
			NCSimpleMessage response;
			if(registred) {
				response = new NCSimpleMessage(NCMessage.OP_OK_NICK);
				String answer = response.toEncodedString();
				try {
					dos.writeUTF(answer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				response = new NCSimpleMessage(NCMessage.OP_DUPLICATED_NICK);
				String answer = response.toEncodedString();
				try {
					dos.writeUTF(answer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList()  {
		//TODO La lista de salas debe obtenerse a partir del RoomManager y después enviarse mediante su mensaje correspondiente
		//NCRoomDescription rooms = roomManager.getDescription();
		List<NCRoomDescription> roomInfo = new ArrayList<>();
		roomInfo = serverManager.getRoomList();
		NCRoomListMessage response = new NCRoomListMessage(NCMessage.OP_ROOM_LIST, roomInfo);
		String answer = response.toEncodedString();
		try {
			dos.writeUTF(answer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processRoomMessages()  {
		//TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de la sala
		boolean exit = false;
		try {
			roomManager.broadcastMessage(user, "** Ha entrado en la sala **");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!exit) {
			try {
				//TODO Se recibe el mensaje enviado por el usuario
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				//TODO Se analiza el código de operación del mensaje y se trata en consecuencia
				switch(message.getOpcode()){
					case NCMessage.OP_INFO:
					{
						List<NCRoomDescription> list = serverManager.getRoomList();
						List<NCRoomDescription> resp = new ArrayList<>();
						for(NCRoomDescription rd : list) {
							if( rd.roomName.equals( ((NCRoomMessage) message).getName() ) ) {
								resp.add(rd);
								break;
							}
						}
						NCRoomListMessage response = new NCRoomListMessage(NCMessage.OP_ROOM_INFO, resp);
						String answer = response.toEncodedString();
						dos.writeUTF(answer);
						break;
					}
					case NCMessage.OP_SEND_MESSAGE:
					{
						
						roomManager.broadcastMessage(user, ((NCRoomMessage) message).getName());
						break;
					}
					case NCMessage.OP_SEND_PRIVATE:
					{
						if (roomManager.privateMessage(user, ((NCServerMessage) message).getNick(), ((NCServerMessage) message).getMessage()) == false) {
							NCSimpleMessage response = new NCSimpleMessage(NCMessage.OP_FAIL_PRIVATE);
							String answer = response.toEncodedString();
							dos.writeUTF(answer);
						}
						else {
							NCSimpleMessage response = new NCSimpleMessage(NCMessage.OP_OK_PRIVATE);
							String answer = response.toEncodedString();
							dos.writeUTF(answer);
						}
						break;
					}
					case NCMessage.OP_EXIT_ROOM:
					{
						
						roomManager.removeUser(user);
						roomManager.broadcastMessage(user, "** Ha abandonado la sala **");
						exit = true; //volvemos a procesar mensajes normales
						break;
					}
					case NCMessage.OP_RENAME:
					{
						Map<String,NCRoomManager> salas = serverManager.getRooms();
						String nuevoNombre = ((NCRoomMessage) message).getName();
						salas.remove(currentRoom);
						salas.put(nuevoNombre, roomManager);
						roomManager.setRoomName(nuevoNombre);
						serverManager.setRooms(salas);
						// Se me había olvidado esto, creo que en el que entregamos en Mayo no estaba
						roomManager.renameMessage(( (NCRoomMessage) message).getName());
						break;
					}
					case NCMessage.OP_RENAME_ROOM:
					{
						currentRoom = ( (NCRoomMessage) message).getName();
						break;
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
