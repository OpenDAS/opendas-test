import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class COMServer {
	private static SerialPort connection;	
	private static Properties properties=new Properties();
	
	public static void main(String[] args){
		 try {
			properties.load(new FileInputStream("config.properties"));
			String ComProp=properties.getProperty("ComServeurKey"); 
			CommPortIdentifier ident =CommPortIdentifier.getPortIdentifier(ComProp);
			if (ident.isCurrentlyOwned()) {
				throw new PortInUseException();
			}			
			connection=(SerialPort)ident.open("ServeurCOM",100);
			connection.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);	
			connection.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			new ServeurCom(connection,properties);
			
		} catch (NoSuchPortException e) {			
			e.printStackTrace();
		} catch (PortInUseException e) {			
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {			
			e.printStackTrace();
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}	
	}
}
class ServeurCom implements Runnable {
	BufferedReader inStream;	
	PrintWriter outStream;
	String result;
	Properties properties;
	public static boolean fini = true;
			
    public  ServeurCom(SerialPort connexion, Properties properties){			
		try{
			//Opening input and output flows
			inStream = new BufferedReader(new InputStreamReader(connexion.getInputStream()));
			outStream = new PrintWriter(connexion.getOutputStream(),true);														
				
		}catch (IOException e ){
			connexion.close();				
		}
		this.properties =properties;
		//Starting up Thread
		new Thread(this).start();
	}
	public void run(){
	//Thread server.
		while(true){
			try{			
				//Reading input flow.
				result = inStream.readLine();							
				if(result.equals(properties.getProperty("ValeurTemp"))){
					outStream.println("25|70|200");									
					System.out.println("25|70|200");
				}
				if(result.equals(properties.getProperty("ValeurPoids"))){
					outStream.println("20.4|10.5|9");									
					System.out.println("20.4|10.5|9");
				}
				if(result.equals(properties.getProperty("ValeurPression"))){
					outStream.println("60|12|17");										
					System.out.println("60|12|17");
				}
				if(result.equals(properties.getProperty("ValeurTemp2"))){
					outStream.println("60|12");										
					System.out.println("60|12");
				}else{
					//Else, overrides the value on the flow.
					outStream.println(result);					
					System.out.println(result);
				}			
			}catch (IOException e){
				e.printStackTrace();
				//System.exit(-1);
			}
		}
	}	
}