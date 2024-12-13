package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.messageFV.NCMessage;
import es.um.redes.nanoChat.messageFV.NCRoomMessage;
import es.um.redes.nanoChat.messageFV.NCServerMessage;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	// Diferentes estados del cliente de acuerdo con el autómata
	private static final byte PRE_CONNECTION = 1;
	private static final byte PRE_REGISTRATION = 2;
	private static final byte REGISTERED = 3;
	private static final byte IN_ROOM = 4;
	// Código de protocolo implementado por este cliente
	// TODO Cambiar para cada grupo
	private static final int PROTOCOL = 98936627;
	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	// Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	// Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	// Último comando proporcionado por el usuario
	private byte currentCommand;
	// Nick del usuario
	private String nickname;
	// Nick del usuario al que se le quiere enviar el mensaje privado
	private String privateNickname;
	// Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String room;
	// Mensaje enviado o por enviar al chat
	private String chatMessage;
	// Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	// Estado actual del cliente, de acuerdo con el autómata
	private byte clientStatus = PRE_CONNECTION;
	// Mensaje privado enviado o por enviarç
	private String privMessage = "";
	// Nuevo nombre que se le dará a la sala
	private String nameRoom;

	// Constructor
	public NCController() {
		shell = new NCShell();
	}

	// Devuelve el comando actual introducido por el usuario
	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	// Establece el comando actual
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	// Registra en atributos internos los posibles parámetros del comando tecleado
	// por el usuario
	public void setCurrentCommandArguments(String[] args) {
		// Comprobaremos también si el comando es válido para el estado actual del
		// autómata
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				nickname = args[0];
			break;
		case NCCommands.COM_ENTER:
			room = args[0];
			break;
		case NCCommands.COM_SEND:
			chatMessage = args[0];
			break;
		case NCCommands.COM_RENAME:
			nameRoom = args[0];
		case NCCommands.COM_PRIVATE:
			String s = args[0];
			String[] arrayS = s.split(" ");
			privateNickname = arrayS[0];
			privMessage = "";
			for (int i = 1; i < arrayS.length; i++)
				privMessage += arrayS[i] + " ";
			break;
		default:
		}
	}

	// Procesa los comandos introducidos por un usuario que aún no está dentro de
	// una sala
	public void processCommand() {
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				registerNickName();
			else
				System.out.println("* You have already registered a nickname (" + nickname + ")");
			break;
		case NCCommands.COM_ROOMLIST:
			if (clientStatus == REGISTERED)
				getAndShowRooms();
			else
				System.out.println("* You need to be registered");
			break;
		// TODO LLamar a getAndShowRooms() si el estado actual del autómata lo permite
		// TODO Si no está permitido informar al usuario
		case NCCommands.COM_ENTER:
			if (clientStatus == REGISTERED) {
				enterChat();
			} else
				System.out.println("* You need to be registered");
			// TODO LLamar a enterChat() si el estado actual del autómata lo permite
			// TODO Si no está permitido informar al usuario
			break;
		case NCCommands.COM_QUIT:
			// Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
			ncConnector.disconnect();
			directoryConnector.close();
			break;
		default:
		}
	}

	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			// Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname);// _UnformattedMessage
			// TODO: Cambiar la llamada anterior a registerNickname() al usar mensajes
			// formateados
			if (registered) {
				// TODO Si el registro fue exitoso pasamos al siguiente estado del autómata
				clientStatus = REGISTERED;
				System.out.println("* Your nickname is now " + nickname);
			} else
				// En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	// Método que solicita al servidor de NanoChat la lista de salas e imprime el
	// resultado obtenido
	private void getAndShowRooms() {
		// TODO Lista que contendrá las descripciones de las salas existentes
		List<NCRoomDescription> rooms = null;
		// TODO Le pedimos al conector que obtenga la lista de salas
		// ncConnector.getRooms()
		try {
			rooms = ncConnector.getRooms();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Una vez recibidas iteramos sobre la lista para imprimir información de
		// cada sala
		if (rooms != null) {
			for (NCRoomDescription room : rooms) {
				System.out.println(room.toPrintableString());
			}
		} else
			System.out.println("* 0 rooms");
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() {
		// TODO Se solicita al servidor la entrada en la sala correspondiente
		// ncConnector.enterRoom()
		boolean in = false;
		try {
			in = ncConnector.enterRoom(room);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Si la respuesta es un rechazo entonces informamos al usuario y salimos
		if (!in) {
			System.out.println("* You can't enter this room");
			return;
		}
		System.out.println("* You have entered room : " + room);
		clientStatus = IN_ROOM;
		// TODO En caso contrario informamos que estamos dentro y seguimos
		// TODO Cambiamos el estado del autómata para aceptar nuevos comandos
		do {
			// Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
			readRoomCommandFromShell();
			processRoomCommand();
		} while (currentCommand != NCCommands.COM_EXIT);
		System.out.println("* Your are out of the room");
		// TODO Llegados a este punto el usuario ha querido salir de la sala, cambiamos
		// el estado del autómata
	}

	// Método para procesar los comandos específicos de una sala
	private void processRoomCommand() {
		switch (currentCommand) {
		case NCCommands.COM_ROOMINFO:
			// El usuario ha solicitado información sobre la sala y llamamos al método que
			// la obtendrá
			if (clientStatus == IN_ROOM)
				getAndShowInfo();
			else
				System.out.println("* You must be in a room");
			break;
		case NCCommands.COM_SEND:
			// El usuario quiere enviar un mensaje al chat de la sala
			sendChatMessage();
			break;
		case NCCommands.COM_SOCKET_IN:
			// En este caso lo que ha sucedido es que hemos recibido un mensaje desde la
			// sala y hay que procesarlo
			processIncommingMessage();
			break;
		case NCCommands.COM_PRIVATE:
			// El usuario quiere enviar un mensaje a otro
			sendPrivateMessage();
			break;
		case NCCommands.COM_EXIT:
			// El usuario quiere salir de la sala
			exitTheRoom();
			break;
		case NCCommands.COM_RENAME:
			// El usuario quiere cambiar el nombre de la sala
			renameRoom();
			break;
		}
	}

	// Método para solicitar al servidor la información sobre una sala y para
	// mostrarla por pantalla
	private void getAndShowInfo() {
		// TODO Pedimos al servidor información sobre la sala en concreto
		NCRoomDescription rd = null;
		try {
			rd = ncConnector.getRoomInfo(room);
		} catch (IOException e) {
			// TODO Bloque catch generado automáticamente
			e.printStackTrace();
		}
		// TODO Mostramos por pantalla la información
		System.out.println(rd.toPrintableString());
	}

	// Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() {
		// TODO Mandamos al servidor el mensaje de salida
		try {
			ncConnector.leaveRoom(room);
			clientStatus = REGISTERED;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Cambiamos el estado del autómata para indicar que estamos fuera de la
		// sala
	}

	// Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() {
		// TODO Mandamos al servidor un mensaje de chat
		try {
			ncConnector.sendChatMessage(chatMessage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendPrivateMessage() {
		// ncConnector.sendPrivateMessage(privateNickname, privateMessage);
		if (nickname.equals(privateNickname)) {
			System.out.println("* El mensaje no puede ir dirigido hacia ti mismo");
		} else if (privateNickname.length() == 0 || privMessage.length() == 0)
			System.out.println("Correct use: private <nickname> <message>");
		else {
			if (!ncConnector.sendPrivMessage(privateNickname, privMessage)) {
				System.out.println("* No se ha podido enviar al usuario " + privateNickname);
			}
		}
	}

	// Método para procesar los mensajes recibidos del servidor mientras que el
	// shell estaba esperando un comando de usuario
	private void processIncommingMessage() {
		// TODO Recibir el mensaje
		NCMessage message = ncConnector.processIncomingMessage();
		// TODO En función del tipo de mensaje, actuar en consecuencia
		switch (message.getOpcode()) {
		case NCMessage.OP_SERVER_MESSAGE: {
			System.out.println(((NCServerMessage) message).getNick() + ": " + ((NCServerMessage) message).getMessage());
			break;
		}
		case NCMessage.OP_RENAME_ROOM: {
			ncConnector.renameOk(((NCRoomMessage) message).getName());
			System.out.println("La sala ha cambiado de nombre");
			room = ((NCRoomMessage) message).getName();
			break;
		}
		case NCMessage.OP_SEND_PRIVATE: {
			System.out.println("(privated) " + ((NCServerMessage) message).getNick() + ": "
					+ ((NCServerMessage) message).getMessage());
			break;
		}
		}
		// TODO (Ejemplo) En el caso de que fuera un mensaje de chat de broadcast
		// mostramos la información de quién envía el mensaje y el mensaje en sí
	}

	public void renameRoom() {
		ncConnector.renameRoom(nameRoom);
		room = nameRoom;
	}

	// MNétodo para leer un comando de la sala
	public void readRoomCommandFromShell() {
		// Pedimos un nuevo comando de sala al shell (pasando el conector por si nos
		// llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		// Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		// Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		// Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		// Intentamos obtener la dirección del servidor de NanoChat que trabaja con
		// nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			serverAddress = null;
		}
		// Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");
			return false;
		} else
			return true;
	}

	// Método para establecer la conexión con el servidor de Chat (a través del
	// NCConnector)
	public boolean connectToChatServer() {
		try {
			// Inicializamos el conector para intercambiar mensajes con el servidor de
			// NanoChat (lo hace la clase NCConnector)
			ncConnector = new NCConnector(serverAddress);
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
			serverAddress = null;
		}
		// Si la conexión se ha establecido con éxito informamos al usuario y cambiamos
		// el estado del autómata
		if (serverAddress != null) {
			System.out.println("* Connected to " + serverAddress);
			clientStatus = PRE_REGISTRATION;
			return true;
		} else
			return false;
	}

	// Método que comprueba si el usuario ha introducido el comando para salir de la
	// aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.COM_QUIT;
	}

}
