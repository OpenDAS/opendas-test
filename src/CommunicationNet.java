import java.text.DecimalFormat;
import java.util.List;
import java.awt.Event;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import javax.swing.JButton;

public class CommunicationNet extends JButton{	
	static final long serialVersionUID = 1L;
	boolean fini = true,Fin=false, wasSignalled = false;
	double TempsF,TempsD,diff;
	long Abs=0,diff2;	
	int i=0,y=0,Var=1;	
	static Thread NetEnv,NetRet;	
	String ValeurDuree;
	static int Duree=0,Cycle=0;	
	static String Scénario,str=null,ScanFile,BuffFile,TextFile;	
	long[] tabX = new long[500001];	
	double[] tabY = new double[500001];
	static Socket Socket;
	PrintWriter Du;
	BufferedReader in;
	Scanner Scan;	
    BufferedWriter buffer;    
    List<Event> eventList = new LinkedList<Event>();    
    DecimalFormat DF =new DecimalFormat("0.00");
    
    /**La classe CommunicationNet permet l'envoit et la réception de trames sur un port sélectionné.
	* Elle calcul le temps écoulé, et renvoit les informations sous formes de deux tableaux dans la classe graphique.
	* @param Socket contient le port utilisé et l'adresse IP de la machine hôte.
	* @param Sc: les trames utilisées, Dur: la durée entre deux points, Cyc: le nombre de points. 
	*/
	public CommunicationNet(String Sc,int Dur,int Cyc,Socket client){	
		
		CommunicationNet.Cycle = Cyc;		
		CommunicationNet.Duree = Dur;
		CommunicationNet.Scénario = Sc;		
		CommunicationNet.Socket=client;	
		Properties properties = new Properties();
		try {			
			properties.load(new FileInputStream("config.properties"));
			BuffFile=properties.getProperty("BuffKey");			
			ScanFile=properties.getProperty("ScanKey");
			TextFile=properties.getProperty("TxtKey");			
			//Ecriture du fichier
			buffer = new BufferedWriter(new FileWriter(BuffFile));
			buffer.write("Port utilisé: Réseau");
			buffer.write("\nScénario utilisé: "+ScanFile+Scénario+TextFile);
			//Ouverture du port d'écriture.
			Du= new PrintWriter(Socket.getOutputStream());
			//Ouverture du port de lecture.
			in = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
			
		} catch (IOException e) {			
			System.out.println("Les ports d'entrée/sortie ne sont pas disponibles");
		}
		//Démarrage du Thread de lecture.
		NetRet = new Thread(new CommunicationRéceptionNet());		
		NetRet.start();
		//Démarrage du thread d'écriture.			
		NetEnv=new Thread(new CommunicationEmissionNet());			
		NetEnv.start();		
		diff2=System.currentTimeMillis();
    }
	/**
	 * Recoit le temps d'émission et de réception et calcul la différence des deux.
	 * Renvoit les valeurs en appelant le constructeur graphique.
	 * @param TempsDepart
	 * @param TempsFinal
	 */
	public void TempsNetEmission(double Temps){	
		
		//Affichage du temps à l'émission.
		TempsD=Temps;							
		System.out.println("Heure début:"+DF.format(TempsD/1000));					
	}
	public void TempsNetReception(double Temps){	
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
	            buffer.write("\n\nHeure de début: "+DF.format(TempsD));		            
	            buffer.write("\nHeure de fin: "+DF.format(TempsF)); 		          
	            buffer.write("\nTemps écoulé:"+DF.format(diff)+"ms");        		           
	            buffer.write("\nNombre de Cycles choisi: "+Cycle);		                			            
	            buffer.write("\nNombre de Cycles effectués: "+i);		                		            
	            buffer.write("\nTemps de l'acquisition: "+Abs+" ms");		                		            
	            buffer.write("\nLe résultat est: "+ValeurDuree);	           
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
				Graphique GraphPanelFinal = new Graphique("Graphique", "Test OpenDAS",tabX,tabY,i,Fin);        
				GraphPanelFinal.pack();							
			}
			else{
				//Taux de rafraichissement du graphique plus faible quand il y a plus de points. 
				if((Var==(Cycle/100))|(Cycle<100)){						
					Graphique GraphPanel = new Graphique("Graphique", "Test OpenDAS",tabX,tabY,i,Fin);        
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
		for(i=0;i<Cycle;i++){
			tabX[i]=0;
			tabY[i]=0;
		}
		i=0;
		Fin=false;wasSignalled=false;
		Du.close();
		in.close();
		Scan.close();				
		Socket.close();
	}
	
	public class CommunicationEmissionNet implements Runnable  {			
		/**
		 * Thread qui va lire un fichier Scénario et envoyer périodiquement, selon la valeur de Cycle, une ligne du fichier.
		 * Le thread se met en pose pour durée indiqué par la variable Duree. 
		 * @param Appel de la méthode TempsNet avec en paramètre le temps actuel de la machine.
		 */
		public CommunicationEmissionNet(){							
		}
	
		public void run(){			
		//Thread d'émission qui va émettre l'ensemble des trames et se terminer.			
			try{				
				//Tant que le nombre de cycle n'est pas atteint, on lit les trames du fichier.
				while(y!=Cycle){					
					//Lecture du fichier à l'emplacement sélectionnée.
					 Scan=new Scanner(new FileReader(ScanFile+Scénario+TextFile));						
					//Tant qu'il y a une ligne à lire et que le nombre de cycle n'est pas atteint, lecture de cette ligne.					
					while ((Scan.hasNextLine())&&(y != Cycle)){							
						y++;							
						//Lecture de la ligne du fichier et replacement du pointeur sur la ligne suivante.
						str=Scan.nextLine();
						//Calcul et envoi du temps de la machine.							
						TempsNetEmission(System.nanoTime());
						//Ecriture de la donnée, on force le buffer à ce vider.
						Du.println(str);
						Du.flush();							
						//Synchronization et mise en attente du Thread.							
						synchronized(eventList){
							//Permet d'éviter de perdre des notify.
							if(!wasSignalled){
								eventList.wait();
								//Attend le nbr de millis sélectionnées.
								Thread.sleep(Duree);
							}
							wasSignalled = false;
						}
					}	
				}
				//On écrit null pour signifier que toutes les trames ont été transmisent.
				Du.println("null");
				Du.flush();					
			}catch (IOException e){				
				System.out.println("Le port d'écriture est indisponible");
			}catch (IllegalStateException a){				
				a.printStackTrace();
			}catch (InterruptedException e){				
				System.out.println("Déconnexion");
			}		
		}
	}
	public class CommunicationRéceptionNet implements Runnable{				
		/**
		 * Thread de réception qui va lire un port en boucle.
		 * @param Retourne la valeur du temps de la machine grâce  à la méthode TempsNet.  
		 */
		public CommunicationRéceptionNet(){			
		}
		
		public void  run(){
		//Thread d'écoute du port.			
			try{				
				while(fini==true){					
					//Lecture de la ligne émise par le serveur, méthode bloquante.
					ValeurDuree = in.readLine();																			
					if(ValeurDuree.equals("null")){	
						//A la fin du transfert, le thread s'arrête.						
						fini=false;	
						Stop();
					}					
					else{							
						//Lecture une seconde fois du temps de la machine et appel de le fonction Temps.					
						TempsNetReception(System.nanoTime());
						//Affichage de la trame reçue.
						System.out.println("Le résultat final est:"+ValeurDuree+"\n");								
						//Synchronisation du thread sur l'objet sélectionné.
						synchronized (eventList){	
							//Déblocage du thread d'émission.
							wasSignalled = true;
							eventList.notify();							
						}						
					}				
				}			
			}catch (IOException e ){
				System.out.println("Le port de lecture est indisponible");				
			}catch(IllegalMonitorStateException ex){
				ex.printStackTrace();
			}
		}		
	}	
}