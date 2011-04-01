import java.lang.*;
import java.io.*;
import java.net.*;

class NetworkCommunication {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public NetworkCommunication(Socket socket) {
		this.socket = socket;
	}

	public void initStreams() throws IOException {
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	public DataInputStream getInpuStream() {
		return in;
	}
	
	public void closeStreams() throws IOException {
		in.close();
		out.close();
		socket.close();
	}
	
	public void writeByte(byte value) throws IOException {
		out.writeByte(value);
	}
	
	public void writeInt(int value) throws IOException {
		out.writeInt(value);
	}

	public void writeDouble(double value) throws IOException {
		out.writeDouble(value);
	}

	public int readInt() throws IOException {
		return in.readInt();
	}

	public double readDouble() throws IOException {
		return in.readDouble();
	}
	
	public byte readByte() throws IOException {
		return in.readByte();
	}
	
	public short readShort() throws IOException {
		return in.readShort();
	}

	public void allowTimeout(int timeout) throws SocketException {
		socket.setSoTimeout(timeout);
	}

	public void writeString(String name) throws IOException {
		out.writeBytes(name);
	}

}

public class AI
{
	static Socket socket = null;
	static NetworkCommunication comm = null;
	
	final static int H_TEAM_NAME = 1;
	final static int H_MAP_DIM = 2;
	final static int H_INITIAL_INFO = 3;
	final static int H_CAR_CONFIG = 4;
	final static int H_CAR_CONFIRM = 4;
	final static int H_DRIVE_INFO = 5;
	final static int H_POS_CONFIRM = 6;
	final static int H_END_RACE = 7;

	AI()
	{
	}

	public static void main(String[] args)
	{
		int acc, brk,spd;
		double str;
		try
		{
			// create socket
			socket = new Socket("127.0.0.1", 6666);
			// we attempt to bypass nagle's algorithm
			socket.setTcpNoDelay(true);
			
			//initialize our communication class
			comm = new NetworkCommunication(socket);
			comm.initStreams();
			
			// send initial packet, aka the team's name
			comm.writeInt(H_TEAM_NAME);
			comm.writeInt(args[0].length());
			comm.writeString(args[0]);
		}
		catch (UnknownHostException e)
		{
			System.out.println("could not connect to server");
			System.exit(1);
		}
		catch  (IOException e)
		{
			System.out.println("No I/O");
			System.exit(1);
		}
		System.out.println("Sent team name....entering main loop");
		int [][] map;
		int width, height;
		while (true)
		{
			try
			{
				int header = comm.readInt();
				//System.out.println("Header: "+header);
				switch (header)
				{
					case H_MAP_DIM:
					{
						//PrintWriter out = null;
						//out = new PrintWriter("file.txt");
						boolean ok = false;
						width = comm.readInt();
						height = comm.readInt();
						System.out.println("Dimensiuni harta: "+width+" "+height);
						map = new int[height][width];
						int linie=0, col = 0, x;
						int rest = width * height %32;
						for (int i =0;i<width*height/32;i++){
							x = comm.readInt();
							if (x>= 0){
								ok = true;
							}
	//						System.out.println(x);
							String s = Integer.toBinaryString(x);
//							System.out.println("Sir "+s);
							int n2 = s.length(); //lungime
							if (ok){
								for (int k =0; k<32-n2; k++){
									s = "0" + s;
								}
							}
							for (int j = 0; j<s.length(); j++){
								if (col == width) {
									linie++;
									col = 0;
								}
							
								//System.out.println(j);
								map[linie][col] = Character.getNumericValue(s.charAt(j));
								col++;
							}//Am scris un int
						}
						if (rest != 0){
							x = comm.readInt();
							String s = Integer.toBinaryString(x);
							for (int i = 0; i<width - col; i++ ){
								map[linie][col] = (int)s.charAt(i);
							}
						}
						//System.out.println("Dimensiuni: "+height + width);
						/*for (int i=0; i< height; i++){
							//System.out.println();
							for (int j = 0; j<width; j++){
								//System.out.print(" "+map[i][j]);
								out.print(map[i][j]);
								out.print(" ");
							}
							out.println();
						}*/
						System.out.println("Received map dimentions packet");
					}; break;
					
					case H_INITIAL_INFO:
					{
						int xp, yp, wm,hm;
						xp = comm.readInt();
						yp = comm.readInt();
						wm = comm.readInt();
						hm = comm.readInt();
						int dir = comm.readInt();
						int nlaps = comm.readInt();
						int maxltime = comm.readInt();
						double ica = comm.readDouble();
						System.out.println("Start potint x y: "+xp+" " +yp +"     Marimi: "+ wm+" "+hm+"         Directie: "+dir);
					//	System.out.println("steaua1");
						System.out.println("Nr laps "+nlaps+ "                 Max lap time: "+maxltime+"          Initial car angle "+ica);
					//	System.out.println("steaua2");
						System.out.println("Received initial information packet");
						//Trimitere caracteristici masina
						
						acc = 10;
						brk = 10;
						spd = 100;
						str = 0.5;
						comm.writeInt(H_CAR_CONFIG);
						comm.writeInt(acc);
						comm.writeInt(brk);
						comm.writeInt(spd);
						comm.writeDouble(str);
						System.out.println("Sent car configuration");
						
						
					}; break;
					
					case H_CAR_CONFIRM:
					{
						acc = comm.readInt();
						brk = comm.readInt();
						spd = comm.readInt();
						str = comm.readDouble();
						System.out.println(acc+" "+brk+" "+spd+" "+str);
						System.out.println("Received car confirm packet");
						
						//Trimitere primul pachet
						comm.writeInt(5);
						comm.writeDouble(1);
						comm.writeDouble(0);
						comm.writeDouble(4);
						comm.writeInt(0);
					}; break;
					
					case H_POS_CONFIRM:
					{
						//Primesc confirmare pachet
						double xm = comm.readDouble();
						double ym = comm.readDouble();
						double sms = comm.readDouble();
						double ang = comm.readDouble();
						int dir = comm.readInt();
						System.out.println(xm+" "+ym+" "+sms+" "+ang+" "+dir);
						try{
							Thread.sleep(100);
						}
						catch (Exception e){
							System.out.println("Ceva naspa cu timpu'");
						}
						System.out.println("Received position confirmation packet");
						System.out.println("Trimit urmatorul pachet");
						comm.writeInt(5);
						comm.writeDouble(1);
						comm.writeDouble(0);
						comm.writeDouble(Math.PI*2.97/2);
						comm.writeInt(0);
					}; break;
					
					case H_END_RACE:
					{
						System.out.println("Received end race packet");
					}; break;
					
					default:
					{
						System.out.println("Unknown packet");
					}
				}
			}
			catch  (IOException e)
			{
				System.out.println("No I/O");
				System.exit(1);
			}
		}
	}
}
