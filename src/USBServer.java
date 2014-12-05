import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class USBServer{
	static SerialPort connexion;		
	private static Properties properties=new Properties();
	
	public static void main(String[] args){		
		 try {
			properties.load(new FileInputStream("config.properties"));
			String UsbProp=properties.getProperty("UsbServeurKey"); 
			CommPortIdentifier ident =CommPortIdentifier.getPortIdentifier(UsbProp);
			if (ident.isCurrentlyOwned()) {
				throw new PortInUseException();
			}			
			connexion=(SerialPort)ident.open("ServeurUSB",100);
			connexion.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);	
			connexion.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			new ServeurEnvoiUsb(connexion,properties);
			
		} catch (NoSuchPortException e) {			
			e.printStackTrace();
		} catch (PortInUseException e) {			
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {			
			e.printStackTrace();		
		} catch (IOException e) {			
			e.printStackTrace();
		}	
	}
}
class ServeurEnvoiUsb implements Runnable {
	BufferedReader Reader;	
	PrintWriter Writer;	
	static boolean fini = true;
	String result;
	Properties properties;
    public  ServeurEnvoiUsb(SerialPort connexion, Properties properties){			
		try{
			//Ouverture des flux d'entrée sorties.
			Reader = new BufferedReader(new InputStreamReader(connexion.getInputStream()));
			Writer = new PrintWriter(connexion.getOutputStream(),true);														
				
		}catch (IOException e ){
			connexion.close();				
		}
		this.properties =properties;
		//Démmarage du Thread serveur.
		new Thread(this).start();
	}
	public void run(){
	//Thread serveur.
		while(true){
			try{			
				//Lecture du flux d'entré.				
				result= Reader.readLine();									
				if(result.equals(properties.getProperty("ValeurTemp"))){
					Writer.println("25|70|200");									
					System.out.println("25|70|200");
				}
				if(result.equals(properties.getProperty("ValeurPoids"))){
					Writer.println("20.4|10.5|9");									
					System.out.println("20.4|10.5|9");
				}
				if(result.equals(properties.getProperty("ValeurPression"))){
					Writer.println("60|12|17");										
					System.out.println("60|12|17");
				}
				if(result.equals(properties.getProperty("ValeurPoids2"))){
					Writer.println("60|12");										
					System.out.println("60|12");
				}
				else{					 
					//Sinon, on réécrit la valeur sur le flux.
					Writer.println(result);					
					System.out.print(result+"\n");
				}
			}catch (IOException e){
				e.printStackTrace();
				System.exit(-1);
			}		
		}
	}	
}