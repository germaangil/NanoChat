package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada con cada sala particular)
 */
class NCServerManager {

	//Primera habitación del servidor
	final static byte INITIAL_ROOM = 'A';
	final static String ROOM_PREFIX = "Room";
	//Siguiente habitación que se creará
	byte nextRoom;
	//Usuarios registrados en el servidor
	private Set<String> users = new HashSet<String>();
	//Habitaciones actuales asociadas a sus correspondientes RoomManagers
	private Map<String,NCRoomManager> rooms = new HashMap<String,NCRoomManager>();

	NCServerManager() {
		nextRoom = INITIAL_ROOM;
	}
	public Map<String, NCRoomManager> getRooms() {
		return rooms;
	}
	public void setRooms(Map<String, NCRoomManager> rooms) {
		this.rooms = rooms;
	}
	//Método para registrar un RoomManager
	public void registerRoomManager(NCRoomManager rm) {
		//TODO Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = ROOM_PREFIX + (char) nextRoom;
		nextRoom++;
		rooms.put(roomName, rm);
		rm.setRoomName(roomName);
	}
	
	public void setRoomName(NCRoomManager rm, String room, String name) {
		rooms.remove(room);
		rooms.put(name, rm);
		rm.setRoomName(name);
	}

	//Devuelve la descripción de las salas existentes
	public synchronized List<NCRoomDescription> getRoomList() {
		List<NCRoomDescription> salas = new ArrayList<>();
		//TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		for(NCRoomManager name : rooms.values()) {
			salas.add(name.getDescription());
		}
		//TODO Añade la información al ArrayList
		return salas;
	}


	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		//TODO Devuelve true si no hay otro usuario con su nombre
		//TODO Devuelve false si ya hay un usuario con su nombre
		if(!users.contains(user)) {
			users.add(user);
			return true;
		}
		else return false;
	}

	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//TODO Elimina al usuario del servidor
		users.remove(user);
	}

	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//TODO Verificamos si la sala existe
		if(!rooms.containsKey(room)) {
			return null;//error
		}
		//TODO Decidimos qué hacer si la sala no existe (devolver error O crear la sala)
		//TODO Si la sala existe y si es aceptado en la sala entonces devolvemos el RoomManager de la sala
		NCRoomManager rm = rooms.get(room);
		if(rm.registerUser(u, s))
			return rm;
		else return null;
	}

	//Un usuario deja la sala en la que estaba
	public synchronized void leaveRoom(String u, String room) {
		//TODO Verificamos si la sala existe
		if(rooms.containsKey(room)) {
			rooms.get(room).removeUser(u);
		}
		//TODO Si la sala existe sacamos al usuario de la sala
		//TODO Decidir qué hacer si la sala se queda vacía
	}
}