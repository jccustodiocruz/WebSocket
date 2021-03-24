/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package websockets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author lv1013
 * Es un ejemplo de websocket donde cada mensaje que llega un cliente
 * es replicado a quienes estén conectados
 */
@ServerEndpoint("/websocketendpoint")
public class WebSockete {
	//para guardar la sesión de cada cliente y poder replicar el mensaje a cada uno
	//se hace una colección sincronizada para el manejo de la concurrencia
    private static Set<Session> clients = 
            Collections.synchronizedSet(new HashSet<Session>());    
    
    public String getId(Session s){
        return s.getId();        
    }
    
    @OnOpen
    public void onOpen(Session sesion) throws JSONException, EncodeException{
        System.out.println("Open Connection ...");
		//al conectarse un cliente se abre el websocket y se guarda su sesión.
        clients.add(sesion);
        JSONObject obj = new JSONObject();
        obj.put("tipo", "cliente");
        obj.put("id", sesion.getId());
        for(Session client : clients){
            if (!client.equals(sesion)){
                try {
                    client.getBasicRemote().sendObject(obj);                    
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
          }
    }
     
    @OnClose
    public void onClose(Session sesion){
        System.out.println("Close Connection ...");
		//al cerrarse la conexión por parte del cliente se elimina su sesión en el servidor
        clients.remove(sesion);
    }
     
    @OnMessage
    public void onMessage(String message, Session sesion) throws JSONException, EncodeException{            
        System.out.println("Message from the client: " + message);
        String echoMsg = "Echo from the client " + sesion.getId() + ": " + message;    
        JSONObject obj = new JSONObject();
        obj.put("tipo", "mensaje");
        obj.put("mensaje", echoMsg);
		//se hace un bloque sincronizado para manejar la concurrencia, tal como en los sockets e hilos
        synchronized(clients){
          // Se itera sobre la sesiones (clientes) guardados para transmitir el mensaje
          for(Session client : clients){
            if (!client.equals(sesion)){
                try {
                    client.getBasicRemote().sendObject(obj);                    
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
          }
        }        
    }
 
    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }    
}
