import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.net.Socket;
import java.util.Properties;

public class MainWindow{	
	JFrame frame =new JFrame();
	public static final long serialVersionUID = 10L;	
	Properties properties=new Properties();
	
	//Déclaration des scénarios et des ports choisis.
	String[] scenarios ={"Choix du Scénario","Scénario1", "Scénario2"};
	String[] ports ={"Choix du Port","Port Série","Port USB","Port Réseaux"};
	
	//Déclaration des labels, des panneau et des boutons.
	JButton Envoi = new JButton("Envoyer");		
	JLabel text1 = new JLabel("Durée entre deux points(ms): ");
	JLabel text2 = new JLabel("Nombre de points(max 650000): ");		
	JLabel text3 = new JLabel("N° du port: (5430)");
	JLabel text4 = new JLabel("Adresse IP: (172.26.44.115)");	
	
	JPanel PanSud = new JPanel();
	JPanel PanNord = new JPanel();	
	JPanel PanCenteru = new JPanel();
	JPanel PanCenter1 = new JPanel();	
	JPanel PanCenter2 = new JPanel();	
	JPanel PanCenter3 = new JPanel();	
	JPanel PanCenter4 = new JPanel();	
	JPanel PanT1 = new JPanel();	
	JPanel PanT2 = new JPanel();
	JPanel PanT3 = new JPanel();
	JPanel PanT4 = new JPanel();	
	
	JComboBox MenuCom = new JComboBox(ports);
	JComboBox MenuScenarios = new JComboBox(scenarios);
	JTextField Duree = new JTextField(10);		
	JTextField Cycle = new JTextField(10);
	JTextField NPort = new JTextField(10);
	JTextField ip = new JTextField(10);
	JTextArea results = new JTextArea();
	JTextArea Information = new JTextArea(2,10);
	
	/** Instancie la fenêtre principal du programme.
	 *  Elle permet l'affichage des boutons et des champs de texte.
	 */
	public MainWindow(){	
	//Creation de la JFrame principale.	
		frame.setLocation(970,300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("OpenDAS Profiler");
		frame.setSize(380, 490);			
		frame.setVisible(true);
		//Ajout des labels dans les différents panneaux.
		PanNord.add(MenuScenarios); PanNord.add(MenuCom); PanNord.add(Envoi);	
		PanCenter2.add(text3); PanCenter2.add(NPort);	
		PanT2.add(PanCenter2);	
		PanCenter4.add(text4); PanCenter4.add(ip);	
		PanT4.add(PanCenter4);	
		PanCenter1.add(text1); PanCenter1.add(Duree);	
		PanT1.add(PanCenter1);	
		PanCenter3.add(text2); PanCenter3.add(Cycle);	
		PanT3.add(PanCenter3);	
		PanCenteru.add(PanT2); PanCenteru.add(PanT4); PanCenteru.add(PanT1); PanCenteru.add(PanT3);			
		PanSud.add(Information);		
		
		//Placement des panneaux.		
		PanNord.setLayout(new BoxLayout(PanNord, BoxLayout.LINE_AXIS));	
		PanT1.setLayout(new BoxLayout(PanT1, BoxLayout.LINE_AXIS));
		PanT2.setLayout(new BoxLayout(PanT2, BoxLayout.LINE_AXIS));
		PanT3.setLayout(new BoxLayout(PanT3, BoxLayout.LINE_AXIS));
		PanT4.setLayout(new BoxLayout(PanT4, BoxLayout.LINE_AXIS));	
		PanCenteru.setLayout(new GridLayout(5,0));
		PanSud.setLayout(new GridLayout(0, 2));	
		
		Information.setEditable(false);	
		text4.setEnabled(false);
		ip.setEnabled(false);
		text3.setEnabled(false);
		NPort.setEnabled(false);	
		results.setEditable(false);
		
		//Ajout des panneaux principaux dans la fenêtre.
		frame.getContentPane().add(PanSud, BorderLayout.SOUTH);		
		frame.getContentPane().add(PanNord, BorderLayout.NORTH);	
		frame.getContentPane().add(PanCenteru, BorderLayout.CENTER);
		
		//Configuration du bouton pour la capture, ainsi que les comboBox.	
		MenuCom.addActionListener(new ItemCom());	
		MenuScenarios.addActionListener(new ItemScenarios());
		Envoi.addActionListener(new ActionEnvoi());	
		Cycle.addKeyListener(new ClavierListener());
		Duree.addKeyListener(new ClavierListener());
		NPort.addKeyListener(new ClavierListener());
		ip.addKeyListener(new ClavierListener());
		try {
			properties.load(new FileInputStream("config.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//Mise en place de la comboBox liées aux ports.
	class ItemCom implements ActionListener{		
		public void actionPerformed(ActionEvent Com){
			//Retour de la valeur choisit par l'utilisateur.
			String ValCom = (String)MenuCom.getSelectedItem();			
			Information.setText(null);
			if(ValCom =="Port Série"){
				text3.setEnabled(true);								
				NPort.setEditable(true);
				NPort.setEnabled(true);						
				NPort.setText("/dev/ttyUSB0");
				text3.setText("N° du Port: (/dev/ttyS20)");				
				text4.setEnabled(false);
				ip.setEnabled(false);					
			
			}if(ValCom == "Port Réseaux"){
				//Les JTextArea sont débloqué quand le port réseau est sélectionné.
				NPort.setEditable(true);
				text3.setText("N° du Port: (5431)");
				text4.setText("Adresse IP: (172.26.44.115)");
				text4.setEnabled(true);
				ip.setEnabled(true);
				text3.setEnabled(true);
				NPort.setEnabled(true);				
				NPort.setText("5431");				
			}
			if(ValCom == "Port USB"){
				text3.setEnabled(true);								
				NPort.setEditable(true);
				NPort.setEnabled(true);						
				NPort.setText("/dev/ttyUSB0");
				text3.setText("N° du Port: (/dev/ttyUSB0)");				
				text4.setEnabled(false);
				ip.setEnabled(false);								
			}
		}
	}	
	class ItemScenarios implements ActionListener{
		public void actionPerformed(ActionEvent Sc){			
		}
	}	
	class ActionEnvoi implements ActionListener{		
		public void actionPerformed(ActionEvent Env){
			Envoi();			
		}			
	}
	class ClavierListener implements KeyListener{
		public void keyPressed(KeyEvent event){			
			if(event.getKeyChar()==KeyEvent.VK_ENTER){
				Envoi();
			}
		}		
		public void keyTyped(KeyEvent e) {}		
		public void keyReleased(KeyEvent e) {}
	}
	public void Envoi(){
		//Permet de sélectionner la valeur choisi dans les comboBox.
		String ValCom = (String)MenuCom.getSelectedItem();
		String ValSc = (String)MenuScenarios.getSelectedItem();
		Information.setText(null);
		
		try {					
			int Cyc;
			int Du;				
			SerialPort PortCom;
			String Port=NPort.getText();
			String IP=ip.getText();
			String ScanFile=properties.getProperty("ScanKey");
			String TextFile=properties.getProperty("TxtKey");
			//Vérifie l'existance du scénario choisit.
			File f = new File(ScanFile+ValSc+TextFile);				
			if(f.exists()){					
				Cyc = Integer.parseInt(Cycle.getText());		
				Du = Integer.parseInt(Duree.getText());
				//Affiche un message d'erreur lorsque les variables Cycle et Duree sont nulles.
				if(((!Cycle.getText().isEmpty())|(!Duree.getText().isEmpty()))&&(Cyc<=500000)){							
					if(ValCom == "Port Réseaux"){							
						//Créer une connexion au serveur à l'aide de l'ip et du port sélectionné.
						Socket client = new Socket(IP,Integer.parseInt(Port));						
						//Récupération des variables et envoi à travers la méthode EnvoiInformations.
						new NETCommunication(ValSc,Du,Cyc,client);						
						Information.setText("Les valeurs ont été Envoyées");
					}	
					if(ValCom =="Port USB"){
						String PortTitle = "USB";
						CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(Port);  
						PortCom=(SerialPort)portIdentifier.open("Main",100);						
						PortCom.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						PortCom.enableReceiveTimeout(1);
						new COMCommunication(ValSc,Du,Cyc,PortCom,PortTitle);
						Information.setText("Les valeurs ont été Envoyées");
					}
					if(ValCom =="Port Série"){	
						String PortTitle = "COM";
						CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(Port);
						PortCom=(SerialPort)portIdentifier.open("Main",100); 
						PortCom.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);	
						PortCom.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						PortCom.enableReceiveTimeout(1);
						new COMCommunication(ValSc,Du,Cyc,PortCom,PortTitle);
						Information.setText("Les valeurs ont été Envoyées");
						
					}
				}
				else Information.setText("Valeurs incorrects");							
			}
			else Information.setText("Le Scénario n'existe pas.");
		} catch (Exception e) {
			// Exception levé si la machine hôte n'est pas en écoute, ou si le port choisit n'est pas disponible.
			Information.setText("Le serveur est hors-ligne");
		}
	}
}

