package thenoob.blackbox.joystickdroid;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network extends Thread {
	private Socket client;
	private String hostname;
	private int port;
	
	public Network(String hostname){
		this.hostname = hostname;
		port = 6066;
	}

	public Network(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
	}
	
	public void connect()throws IOException {
		System.out.println("View Trying to connect");
		client = new Socket(InetAddress.getByName(hostname), port);
	}
	public void close()throws IOException {
		if(client != null) {
			client.close();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			connect();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public void send(String msg)throws IOException, UnknownHostException {
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeUTF(msg);
	}
	public void send(char msg)throws IOException, UnknownHostException {
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeInt(msg);
	}
}
