package es.um.redes.nanoChat.directory.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


 // Cliente con métodos de consulta y actualización específicos del directorio

public class DirectoryConnector {
	// Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int MAX_SIZE = 128;
	// Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	// Valor del TIMEOUT
	private static final int TIMEOUT = 1000;

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // dirección del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		// TODO (hecho) A partir de la dirección y del puerto generar la dirección de
		// conexión para el Socket
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress), DEFAULT_PORT);
		// TODO (hecho) Crear el socket UDP
		socket = new DatagramSocket();
	}

	// Método para generar el mensaje de consulta (para obtener el servidor asociado
	// a un protocolo)
	private byte[] makeQuery(int protocol) {
		// TODO Devolvemos el mensaje codificado en binario según el formato acordado
		ByteBuffer bb = ByteBuffer.allocate(2);
		byte opCode = 3;
		byte protocolId = ((Integer) protocol).byteValue();
		bb.put(opCode);
		bb.put(protocolId);
		byte[] men = bb.array();
		return men;
	}

	// Método para obtener la dirección de internet a partir del mensaje UDP de
	// respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		// Pasamos el mensaje a un nuevo buffer
		ByteBuffer bb = ByteBuffer.wrap(packet.getData());
		byte opCode = bb.get();

		// Comprobamos ahora que el mensaje esta codificado correctamente
		if (opCode == 5 || opCode != 4) {
			return null;
		}
		// Si el opCode es correcto entoces obtenemos la direccion ip contenida en el
		// mensajebyte ip1 = bb.get();
		byte ip1 = bb.get();
		byte ip2 = bb.get();
		byte ip3 = bb.get();
		byte ip4 = bb.get();
		int port = bb.getInt();
		String ipFinal = ((Byte) ip1).toString() + "." + ((Byte) ip2).toString() + "." + ((Byte) ip3).toString() + "."
				+ ((Byte) ip4).toString();
		InetSocketAddress out = new InetSocketAddress(InetAddress.getByName(ipFinal), port);
		return out;
	}

	
	 // Envía una solicitud para obtener el servidor de chat asociado a un
	//determinado protocolo
	 
	public InetSocketAddress getServerForProtocol(int numProtocol) throws IOException {
		// Creamos un buffer para enviar un mensaje de consulta
		byte[] messageB = makeQuery(numProtocol);
		DatagramPacket message = new DatagramPacket(messageB, messageB.length, directoryAddress);

		// Creamos un beffer de respuesta
		byte[] answerB = new byte[MAX_SIZE];
		DatagramPacket answer = new DatagramPacket(answerB, answerB.length);
		// Establecemos el temporizador para el caso en que no haya respuesta
		socket.setSoTimeout(TIMEOUT);
		boolean ok = false;
		int attempts = 0;
		// Hacemos un bucle que comprueba constantemente si ha llegado el mensaje
		while (!ok) {
			socket.send(message);
			attempts++;
			// TODO (hecho) Recibir la respuesta
			try {
				socket.receive(answer);
				ok = true;
			} catch (SocketTimeoutException e) {
				if (attempts == 4) {
					return null;
				}
			}
		}

		return getAddressFromResponse(answer);
	}

	// Método para construir una solicitud de registro de servidor
	private byte[] makeRegistration(int numProtocol, int port) {
		// TODO Devolvemos el mensaje codificado en binario según el formato acordado
		ByteBuffer bb = ByteBuffer.allocate(6); // 2 byte de opcode + 4 byte puerto
		byte opCode = 1;
		byte idProtocol = ((Integer) numProtocol).byteValue();
		bb.put(opCode);
		bb.put(idProtocol);
		bb.putInt(port);
		byte[] mensaje = bb.array();
		return mensaje;
	}

	// Envía una solicitud para registrar el servidor de chat asociado a un
	// determinado protocolo
	public boolean registerServerForProtocol(int numProtocol, int port) throws IOException {
		// Enviamos la solicitud con un mensaje
		byte[] messageB = makeRegistration(numProtocol, port);
		DatagramPacket message = new DatagramPacket(messageB, messageB.length, directoryAddress);
		socket.send(message);

		// Creamos un buffer de respuesta
		byte[] answerB = new byte[MAX_SIZE];
		DatagramPacket answer = new DatagramPacket(answerB, answerB.length);
		int attempts = 0;
		boolean ok = false;
		socket.setSoTimeout(TIMEOUT);
		// Hacemos un bucle que comprueba constantemente si ha llegado el mensaje
		while (!ok) {
			socket.send(message);
			attempts++;
			// TODO Recibe respuesta
			try {
				socket.receive(answer);
				ok = true;
			} catch (SocketTimeoutException e) {
				// Si supera el numero de intentos suponemos que no se ha completado el
				// registro con existo
				if (attempts == 5) {
					return false;
				}
			}
		}
		// TODO Procesamos la respuesta para ver si se ha podido registrar correctamente
		ByteBuffer bb = ByteBuffer.wrap(answer.getData());
		byte opCode = bb.get();
		// Comprobamos si el opCode es correcto
		if (opCode != 2 || opCode == 5)
			return false;
		// Devolvemos que el registro se hecho con exito
		return true;
	}

	public void close() {
		socket.close();
	}
}
