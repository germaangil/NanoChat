package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCServerMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NCManager extends NCRoomManager{
	
	private Map<String, Socket> miembros;
	private long tiempoDesdeUltimoMensaje = 0;
	
	public NCManager() {
		miembros = new HashMap<String, Socket>();
	}
	
	//Método para registrar a un usuario u en una sala (se anota también su socket de comunicación)
		public boolean registerUser(String u, Socket s) {
			// si no está, se mete
			if(!miembros.containsKey(u)) {
				miembros.put(u, s);
				return true;
			}
			return false;
		}
		
		//Método para hacer llegar un mensaje enviado por un usuario u
		public void broadcastMessage(String u, String message) throws IOException{
			NCServerMessage msj = new NCServerMessage(NCMessage.OP_SERVER_MESSAGE, u, message);
			for(Socket s : miembros.values()) {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(msj.toEncodedString());
			}
			if(!message.equals("** Ha entrado en la sala **") && !message.equals("** Ha abandonado la sala **")) {
				tiempoDesdeUltimoMensaje = System.currentTimeMillis();
			}
		}
		
		public void renameMessage(String name) throws IOException{
			NCRoomMessage msj = new NCRoomMessage(NCMessage.OP_RENAME_ROOM, name);//no lo tengo claro
			for( Socket s : miembros.values() ) {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(msj.toEncodedString());
			}
		}
		
		//Método para hacer llegar un mensaje privado enviado por un usuario u a un usuario r
		public boolean privateMessage(String u, String r, String message) throws IOException{
			Socket s = miembros.get(r);
			if(s != null) {
				NCServerMessage msj = new NCServerMessage(NCMessage.OP_SEND_PRIVATE, u, message);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF(msj.toEncodedString());
				return true;
			}
			return false;
		}
		
		//Método para eliminar un usuario de una sala
		public  void removeUser(String u) {
			miembros.remove(u);
		}
		
		//Método para nombrar una sala
		public void setRoomName(String roomName) {
			this.roomName = roomName;			
		}
		
		//Método para devolver la descripción del estado actual de la sala
		public NCRoomDescription getDescription() {
			NCRoomDescription sala;
			Set<String> miembrosEnSala = miembros.keySet();
			List<String> nueva = new ArrayList<>();
			for(String dato : miembrosEnSala) {
				nueva.add(dato);
			}
			sala = new NCRoomDescription(roomName, nueva, tiempoDesdeUltimoMensaje);
			return sala;
		}

		//Método para devolver el número de usuarios conectados a una sala
		public int usersInRoom() {
			return miembros.size();
		}

}
