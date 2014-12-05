import java.awt.Event;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import gnu.io.SerialPort;

public class COMCommunication  {		
	static Thread ComRet,ComEnv;		
	static SerialPort Port;
	BufferedReader inStream;	
	PrintWriter outstream;	
	Scanner Scan;
	static final long serialVersionUID = 1L;
	boolean Fin=false, wasSignalled = false,fini=true;
	long Abs=0,diff2;	
	int i=0,y=0,Var=1;	
	String result;
	double TempsF,TempsD,diff;
	static int Duree=0,Cycle=0;	
	static String Scénario,str=null,BuffFile,ScanFile,TextFile;;	
	long[] tabX = new long[500001];	
	double[] tabY = new double[500001];
    BufferedWriter buffer;    
    JPanel Pan = new JPanel();
    JFrame C = new JFrame();   
    List<Event> eventList = new LinkedList<Event>();   
    DecimalFormat DF =new DecimalFormat("0.00");
    
	public COMCommunication(String Sc,int Du,int Cyc,SerialPort Port,String portTitle) {
		COMCommunication.Scénario = Sc;
		COMCommunication.Cycle = Cyc;
		COMCommunication.Duree = Du;
		COMCommunication.Port=Port;
		Properties properties=new Properties();
		try{
			properties.load(new FileInputStream("config.properties"));
			BuffFile=properties.getProperty("BuffKey");			
			ScanFile=properties.getProperty("ScanKey");
			TextFile=properties.getProperty("TxtKey");
			//Ouvre un fichier text et écrit l'en-tête.
			buffer = new BufferedWriter(new FileWriter(BuffFile));
			buffer.write("Port utilisé: "+portTitle);
			buffer.write("\nScénario utilisé: "+ScanFile+Scénario+TextFile);
			//Ouverture des ports d'écoute et de lecture
			inStream = new BufferedReader(new InputStreamReader(Port.getInputStream()));			
			outstream = new PrintWriter(Port.getOutputStream());
			//Démarrage des thread d'écoute et de lecture.				 
			ComRet = new Thread(new CommunicationReceptionCom());
			ComRet.start();			 		
			ComEnv=new Thread(new CommunicationEmissionCom());			 
			ComEnv.start();
			//Lecture du temps machine.
			diff2=System.currentTimeMillis();
			 
		}catch (IOException e){			
			e.printStackTrace();
		}		
	}
public void TempsCOMEmission(double Temps){	
		
		//Affichage du temps à l'émission.
		TempsD=Temps;							
		System.out.println("Heure début:"+DF.format(TempsD/1000));					
	}
	public void TempsCOMReception(double Temps){	
		//Si le nombre de cycle n'est pas atteint faire:
		if(i<=Cycle){					
			//Affichage du temps à la réception. 
			TempsF=Temps;			
			//Calcul du temps d'exécution du serveur en us.
			diff = (double)((TempsF-TempsD)/1000000);						
			//Ajout des points dans le tableau des abcisses et des ordonnées. 						
			tabY[i] = diff;					
			tabX[i] =(int)Abs;			
			System.out.println("Heure fin:"+DF.format(TempsF/1000));
			System.out.println("Temps écoulé:"+DF.format(diff)+"ms");
			System.out.println("Nombre de Cycles choisi: "+Cycle);
			System.out.println("Nombre de Cycles effectués: "+(i+1));
			System.out.println("Temps de l'acquisition: "+Abs+" ms");
			i++;
			try{						 
				//Ecriture des variables dans un fichier texte.						
	            buffer.write("\n\nHeure de début: "+DF.format(TempsD/1000));
	            buffer.write("\nHeure de fin: "+DF.format(TempsF/1000)); 		                
	            buffer.write("\nTemps écoulé:"+DF.format(diff)+"ms");		                
	            buffer.write("\nNombre de Cycles choisi: "+Cycle);		                	
	            buffer.write("\nNombre de Cycles effectués: "+i);		                
	            buffer.write("\nTemps de l'acquisition: "+Abs+" ms");		                
	            buffer.write("\nLe résultat est: "+result);
	            if(i==Cycle) buffer.close();
			}catch(Exception z){
				System.out.println("Impossible d'écrire dans le fichier sélectionné");
			}
			//Calcul la durée totale entre deux points, temps d'exécution du programme comprise, le tout en ms.
			Abs=(Abs+(System.currentTimeMillis())-diff2);
			diff2=System.currentTimeMillis();
			
			if(i==Cycle){						
				Fin=true;
				//Lorsque le nombre de cycle est atteint, on appel la méthode Graphique.
				Graphic GraphPanelFinal = new Graphic("Graphique", "Test OpenDAS",tabX,tabY,i,Fin);        
				GraphPanelFinal.pack();							
			}
			else{
				//Taux de rafraichissement du graphique plus faible quand il y a plus de points. 
				if((Var==(Cycle/1000))|(Cycle<1000)){						
					Graphic GraphPanel = new Graphic("Graphique", "Test OpenDAS",tabX,tabY,i,Fin);        
					GraphPanel.pack();
					Var=0;					
				}
				Var++;							
			}		
		}										
	}
	/**
	 * Permet l'arrêt des différents sockets, et la remise à zéros des variables.
	 * @throws IOException
	 */
	public void Stop() throws IOException{
		System.out.println("Communication terminée");
		//Réinitialisations des variables pour un futur appel.		
		y=0;Var=0;Abs=0;		
		Fin=false;wasSignalled=false;fini=true;
		for(i=0;i<Cycle;i++){
			tabX[i]=0;
			tabY[i]=0;
		}
		i=0;
		//Fermeture des ports		
		outstream.close();
		inStream.close();
		Scan.close();
		Port.close();
	}
	public class CommunicationEmissionCom implements Runnable{		
		public  CommunicationEmissionCom()  {		
		}
		
		public void run(){
			try{
				while(y!=Cycle){					
					Scan=new Scanner(new FileReader(ScanFile+Scénario+TextFile));					
					while ((Scan.hasNextLine())&&(y != Cycle)){
						//Calcul du temps de la machine et appel de la fonction correspondante.						
						TempsCOMEmission(System.nanoTime());		
						y++;						
						str=Scan.nextLine();
						//Envoi des trames et on vide le buffer.
						outstream.println(str);
						outstream.flush();						
						synchronized(eventList){
							//Si le thread à bien été débloqué, on évite de perdre des notify.
							if(!wasSignalled){
								eventList.wait();
								//Attend le nbr de millis sélectionnées.
								Thread.sleep(Duree);
							}
							wasSignalled = false;
						}
					}					
				}
				//Qand toute les trames ont été envoyées, on envoit un caractère final.
				outstream.println("n");
				outstream.flush();
			}catch(Exception e){				
			}			
		}		
	}
	public  class CommunicationReceptionCom implements Runnable{			
		public CommunicationReceptionCom(){			
		}
		
		public void run(){	
			while(fini==true){	
				try {					
					result = inStream.readLine();												
					if(result.equals("n")){	
						//A la fin du transfert, le thread s'arrête et ferme l'ensemble des ports.						
						Stop();
					}					
					else{							
						//Lecture une seconde fois du temps de la machine et appel de le fonction Temps.					
						TempsCOMReception(System.nanoTime());
						//Affichage de la trame reçue.
						System.out.println("Le résultat final est:"+result+"\n");								
						//Synchronisation du thread sur l'objet sélectionné.
						synchronized (eventList){	
							//Déblocage du thread d'émission.
							wasSignalled = true;
							eventList.notify();							
						}					
					}						
				} catch (IOException e){
					//Quand le flux est vide, on attend qu'il se remplisse.
					try{
						Thread.sleep(0);
					}catch(InterruptedException ex ){
						fini=false;
					}
				}
			}
		}			
	}			
}
