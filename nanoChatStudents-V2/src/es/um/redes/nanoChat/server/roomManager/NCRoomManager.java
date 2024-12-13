package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;

public abstract class NCRoomManager {
	String roomName;

	//Método para registrar a un usuario u en una sala (se anota también su socket de comunicación)
	public abstract boolean registerUser(String u, Socket s);
	//Método para hacer llegar un mensaje enviado por un usuario u
	public abstract void broadcastMessage(String u, String message) throws IOException;
	//Método para hacer notificar a todos los usuarios que se ha cambiado el nombre de la sala
	public abstract void renameMessage(String name) throws IOException;
	//Método para hacer llegar un mensaje privado enviado por un usuario u a un usuario r
	public abstract boolean privateMessage(String u, String r, String message) throws IOException;
	//Método para eliminar un usuario de una sala
	public abstract void removeUser(String u);
	//Método para nombrar una sala
	public abstract void setRoomName(String roomName);
	//Método para devolver la descripción del estado actual de la sala
	public abstract NCRoomDescription getDescription();
	//Método para devolver el número de usuarios conectados a una sala
	public abstract int usersInRoom();

}
