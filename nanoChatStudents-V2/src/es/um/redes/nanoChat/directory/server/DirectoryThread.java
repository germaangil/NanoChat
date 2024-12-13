package es.um.redes.nanoChat.directory.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DirectoryThread extends Thread {

	// Tamaño máximo del paquete UDP
	private static final int PACKET_MAX_SIZE = 128;
	// Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del
	// servidor
	protected Map<Integer, InetSocketAddress> servers;

	// Socket de comunicación UDP
	protected DatagramSocket socket = null;
	// Probabilidad de descarte del mensaje
	protected double messageDiscardProbability;

	public DirectoryThread(String name, int directoryPort, double corruptionProbability) throws SocketException {
		super(name);
		// TODO Anotar la dirección en la que escucha el servidor de Directorio
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);
		// TODO Crear un socket de servidor
		socket = new DatagramSocket(serverAddress);
		messageDiscardProbability = corruptionProbability;
		// Inicialización del mapa
		servers = new HashMap<Integer, InetSocketAddress>();
	}

	@Override
	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		while (running) {

			// TODO 1) Recibir la solicitud por el socket
			DatagramPacket answer = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(answer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// TODO 2) Extraer quién es el cliente (su dirección)
			InetSocketAddress clientAddress = (InetSocketAddress) answer.getSocketAddress();
			// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte
			double rand = Math.random();
			if (rand < messageDiscardProbability) {
				System.err.println("Directory DISCARDED corrupt request from... ");
				continue;
			}

			// TODO (Solo Boletín 2) Devolver una respuesta idéntica en contenido a la
			// solicitud

			try {
				// TODO 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
				processRequestFromClient(answer.getData(), clientAddress);
				// TODO 5) Tratar las excepciones que puedan producirse
			} catch (IOException e) {
				e.printStackTrace();
			}

			buf = new byte[PACKET_MAX_SIZE];
		}
		socket.close();
	}

	// Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// TODO 1) Extraemos el tipo de mensaje recibido
		ByteBuffer bb = ByteBuffer.wrap(data);
		byte opCode = bb.get();
		// TODO 2) Procesar el caso de que sea un registro y enviar mediante
		// sendOK
		if (opCode == 1) {
			byte idProtocol = bb.get();
			int port = bb.getInt();
			// TODO(hecho) Bitácora mensaje (Jorge)
			System.out.println("Incoming message..., opCode = " + Byte.toString(opCode) + " (register chat server) "
					+ ", protocol = " + Byte.toString(idProtocol) + ", port = " + port);
			InetAddress chatServerAddress = clientAddr.getAddress();
			InetSocketAddress chatServerSocketAddress = new InetSocketAddress(chatServerAddress, port);
			if (servers.containsKey((int) idProtocol)) {
				sendEmpty(clientAddr);
				return;
			}
			servers.put((int) idProtocol, chatServerSocketAddress);
			// TODO Mostrar servidores disponibles (Jorge)
			for (Map.Entry<Integer, InetSocketAddress> i : servers.entrySet()) {
				Integer key = i.getKey();
				InetSocketAddress value = i.getValue();
				// Obtenemos los campos de cada servidor
				String iDir = value.getAddress().toString().substring(1);
				Integer iPort = value.getPort();
				// Imprimimos los campos de cada servidor
				System.out.println(key.toString() + ": Address " + iDir + "/ Port " + iPort);
			}
			sendOK(clientAddr);
		}
		// TODO 3) Procesar el caso de que sea una consulta
		else if (opCode == 3) {
			byte idProtocol3 = bb.get();
			// TODO Bitácora mensaje (Jorge)
			if (servers.containsKey((int) idProtocol3)) {
				// TODO 3.1) Devolver una dirección si existe un servidor
				// (sendServerInfo)
				sendServerInfo(servers.get((int) idProtocol3), clientAddr);
			} else {
				// TODO 3.2) Devolver una notificación si no existe un servidor
				// (sendEmpty)
				sendEmpty(clientAddr);
			}
		} else
			throw new IllegalArgumentException("Unexpected value: " + opCode);
	}

	// Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		// TODO Construir respuesta
		ByteBuffer bb = ByteBuffer.allocate(1);
		byte opCode = 5;
		bb.put(opCode);
		byte[] messageB = bb.array();
		DatagramPacket message = new DatagramPacket(messageB, messageB.length, clientAddr);
		// TODO Enviar respuesta
		socket.send(message);
	}

	// Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		// TODO (hecho) Obtener la representación binaria de la dirección
		InetAddress dir = serverAddress.getAddress();
		String ip = dir.getHostAddress();
		String[] ipDir = ip.split("\\.");
		// guardamos en variables separadas la direccion ip completa
		String stringIp1 = ipDir[0];
		String stringIp2 = ipDir[1];
		String stringIp3 = ipDir[2];
		String stringIp4 = ipDir[3];
		// Convertimos los array en enteros gracias a la funcion de la clase integer
		// "parseInt(String s)"
		int ip1 = Integer.parseInt(stringIp1);
		int ip2 = Integer.parseInt(stringIp2);
		int ip3 = Integer.parseInt(stringIp3);
		int ip4 = Integer.parseInt(stringIp4);
		// Siguiendo el formato de mensaje con opCode=4 construimos la respuesta
		ByteBuffer bb = ByteBuffer.allocate(9);
		byte opCode = 4;
		bb.put(opCode);
		bb.put(((Integer) ip1).byteValue());
		bb.put(((Integer) ip2).byteValue());
		bb.put(((Integer) ip3).byteValue());
		bb.put(((Integer) ip4).byteValue());
		bb.putInt(serverAddress.getPort());
		byte[] messageB = bb.array();
		DatagramPacket message = new DatagramPacket(messageB, messageB.length, clientAddr);
		// TODO Enviar respuesta
		socket.send(message);
	}

	// Método para enviar la confirmación del registro
	public void sendOK(InetSocketAddress clienteDir) throws IOException {
		//TODO Construir respuesta
		// Tenemos que enviar un mensaje del tipo RegistrationOk
		ByteBuffer bb = ByteBuffer.allocate(1);
		Byte opCode = 2;
		bb.put(opCode);
		byte[] menssageB = bb.array();
		// Creamos el paquete con el nuevo mensaje y se lo enviamos al cliente
		DatagramPacket message = new DatagramPacket(menssageB, menssageB.length, clienteDir);
		//TODO Enviar respuesta
		socket.send(message);
	}
}
