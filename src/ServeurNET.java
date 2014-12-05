import java.net.Socket;
import java.net.ServerSocket;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.swing.JFrame;

public class ServeurNET extends JFrame{
	
	static final long serialVersionUID = 1L;
	static ServerSocket server1;
	static Socket socketserveur;
	static Properties properties=new Properties();
	/**Bellier Aurélien
	 * Stage ASPerience 2013
	 */	
	public ServeurNET(){
		try{
			properties.load(new FileInputStream("config.properties"));
			String NetProp=properties.getProperty("NetServeurKey"); 
			//Choix du port lié au serveur.
			server1 = new ServerSocket(Integer.parseInt(NetProp));			
			while(true)
			{
				//Dès que quelqu'un se connecte au serveur on lance la communication.
				socketserveur = server1.accept();				
				new CommunicationServeur(socketserveur,properties);				
			}					
		}catch (IOException e ){
			//Levé d'une erreur, et fermeture de l'application si le port n'est pas disponible.
			System.out.println("le port réseau n'est pas disponible.");						
		}		 
	}	
}

class CommunicationServeur implements Runnable {
	boolean fini = true;
	String lue=null;	
	Socket client;               //liaison avec client.
    BufferedReader depuisClient; //réception de requête.
    PrintWriter versClient;      //envoi des réponses.
    Properties properties;
    public  CommunicationServeur(Socket client, Properties properties){
		this.client=client;		
		try{
			//Ouverture des flux d'entrée sorties.
			depuisClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			versClient = new PrintWriter(client.getOutputStream(),true);													
				
		}catch (IOException e ){
			//Si une Exception est levée, on ferme le flux client.
			try{
				client.close();
			}catch (IOException ee){}
				e.printStackTrace();
		}
		this.properties =properties;
		//Démmarage du Thread serveur.
		new Thread(this).start();
	}
	public void run(){
	//Thread serveur.
		while(fini==true){
			try{		
				//Lecture du flux d'entré.
				lue=depuisClient.readLine();
				if(lue.equals("null")){
					fini= false;
				}
				if(lue.equals(properties.getProperty("ValeurTemp"))){
					versClient.println("25|70|200");					
					System.out.println("25|70|200");
				}
				if(lue.equals(properties.getProperty("ValeurPoids"))){
					versClient.println("20.4|10.5|9");					
					System.out.println("20.4|10.5|9");
				}
				if(lue.equals(properties.getProperty("ValeurPression"))){
					versClient.println("60|12|17");				
					System.out.println("60|12|17");
				}
				if(lue.equals(properties.getProperty("ValeurTemp2"))){
					versClient.println("60|12");										
					System.out.println("60|12");
				}
				if(lue.equals(properties.getProperty("ValeurPoids2"))){
					versClient.println("60|12");										
					System.out.println("60|12");
				}
				else{								 
					//Sinon, on réécrit la valeur sur le flux.
					versClient.println(lue);
					System.out.println(lue);
				}			
			}catch (IOException e){
				e.printStackTrace();
			}
		}	
	}	
}