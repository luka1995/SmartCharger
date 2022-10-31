import java.net.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class OcppManager {
	
	// Functions
	
	public OcppManager() {
		
	}
	
	public void Connect() {
		try {
			URI serverUrl = new URI("ws://echo.websocket.org/");
			
			WebSocketClient webSocketClient = new WebSocketClient(serverUrl) {
				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					System.out.println("Connection close");
				}

				@Override
				public void onError(Exception arg0) {
					System.out.println("Connection error");
				}

				@Override
				public void onMessage(String arg0) {
					System.out.println("Connection message");
				}

				@Override
				public void onOpen(ServerHandshake arg0) {
					System.out.println("Connection open");
				}
			};
			
			webSocketClient.connect();
		} catch (URISyntaxException ex) {
			System.out.println("OCPP Manager Connect exception: " + ex.getMessage());
		}
	}
}
