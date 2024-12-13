Proyecto NanoChat

Este es un proyecto de chat que consta de tres componentes principales: un directorio (Directory.jar), un servidor de chat (NanoChatServer.jar), y un cliente de chat (NanoChat.jar). En este archivo README.md, se explica cómo ejecutar estos componentes para poner en funcionamiento el sistema de chat.

Requisitos previos

1. Java: Asegúrate de tener Java instalado en tu sistema. Puedes verificar si tienes Java instalado ejecutando el siguiente comando en la terminal:

   java -version

   Si Java no está instalado, puedes instalarlo con:

   sudo apt update
   sudo apt install openjdk-11-jre

2. Archivos necesarios:
   - Directory.jar
   - NanoChatServer.jar
   - NanoChat.jar

   Asegúrate de tener estos archivos disponibles en tu máquina.

Paso 1: Obtener la IP del Directorio

El primer paso es obtener la dirección IP de la máquina que ejecuta el archivo Directory.jar. Para hacerlo, ejecuta el siguiente comando en la terminal:

ifconfig

Busca la línea que dice inet bajo la interfaz de red que estés usando (normalmente eth0 o wlan0). La dirección IP que aparece allí será la que necesitas para conectar el servidor y el cliente al directorio.

Ejemplo:

inet 192.168.1.10  netmask 255.255.255.0  broadcast 192.168.1.255

En este ejemplo, la IP es 192.168.1.10.

Paso 2: Ejecutar el Directorio

Una vez que tengas la IP, debes ejecutar el archivo Directory.jar. Navega a la carpeta donde tienes los archivos .jar y ejecuta el siguiente comando en la terminal:

cd /ruta/donde/estan/los/jars
java -jar Directory.jar

Esto iniciará el servidor del directorio. Debes ver en la terminal algún mensaje indicando que el directorio está corriendo y listo para aceptar conexiones.

Paso 3: Ejecutar NanoChatServer

java -jar NanoChatServer.jar 192.168.1.10

Una vez configurado, ejecuta el servidor con:

Esto iniciará el servidor de chat.

Paso 4: Ejecutar NanoChat (cliente) desde uno o varios ordenadores


java -jar NanoChat.jar 192.168.10

Solución de problemas

- Si el cliente no puede conectarse al servidor, asegúrate de que la IP del directorio sea la correcta y que tanto el cliente como el servidor estén usando la misma IP.
- Si ves errores relacionados con puertos o conexión, asegúrate de que no haya firewalls bloqueando las conexiones en el puerto que usa el directorio o el servidor de chat.

Conclusión

Una vez que hayas seguido estos pasos, el servidor y el cliente deberían estar correctamente conectados al directorio y funcionando correctamente. ¡Disfruta de tu chat!

## Contacto

Si tienes alguna pregunta o sugerencia sobre este proyecto, no dudes en contactarme a través de german.gilp@um.es o por teléfono o WhatsApp 693060816.

