/*Name- Akshay Bahuguna
ID- 1001624304*/

package com.soc;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerNew {

	/*server listens on this port- 9998*/
	private static int port = 9998;
	private static ServerSocket ss;
	private static Socket client;
	private static DataOutputStream dos;
	
	/*Arraylist to store the usernames of clients online*/
	private ArrayList<String> userNames;
	
	/*Arraylist to store the dataoutput streams of online clients 
	 in order for server to broadcast messages*/
	private ArrayList<DataOutputStream> streams;
	
	/*These 4 file reader/writer variables used for Server Logging
	i.e. maintaing the messages and keeping backup in case server 
	shuts down.*/
	private FileWriter fw;
	private BufferedWriter bw;
	private FileReader fr;
	private BufferedReader br;
	
	private JFrame frame;
	private static JTextArea textArea;
	private final static String host = "Host: localhost";
	private final static String userAgent = "User-Agent: MultiChat/2.0";
	private final static String contentType = "Content-Type: text/html";
	private final static String contentlength = "Content-Length: ";
	private final static String date = "Date: ";
	
	/*Button for list of online users*/
	private JButton btnOnlineUsers;
	private JButton btnClrScr;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerNew window = new ServerNew();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	/**
	 * Create the application.
	 */
	public ServerNew() {
		initialize();
	}

	/**
	 * Initialize the contents of the server frame.
	 */
	private void initialize() {
		frame = new JFrame("Server Screen");
		frame.setBounds(500, 500, 501, 519);
	//	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        	try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            System.exit(0);
		    }
		});
		frame.getContentPane().setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 21, 464, 413);
		frame.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		
		btnOnlineUsers = new JButton("Online Users");
		/*Action Listener to implement what happens when you click the button "Online Users" */
		btnOnlineUsers.addActionListener(new ActionListener() {
			/*Displays any available/online users else gives message: No users online*/
			public void actionPerformed(ActionEvent arg0) {
				
				if(userNames.isEmpty()) {
					textArea.append("No User is online. \n");
				}
				
				else {
					textArea.append("Online users are: \n");
					for (String user : userNames) {
						textArea.append(user+"\n");
					}
				}
			}
		});
		btnOnlineUsers.setBounds(178, 446, 113, 23);
		frame.getContentPane().add(btnOnlineUsers);
		
		// - - - - 
		btnClrScr = new JButton("Clear Screen");
		/*Action Listener to implement what happens when you click the button "Online Users" */
		btnClrScr.addActionListener(new ActionListener() {
			/*Clears the screen messages*/
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		//btnClrScr.setBounds(178, 446, 113, 23);
		btnClrScr.setBounds(288, 446, 113, 23);
		frame.getContentPane().add(btnClrScr);
		//- - - - 
		
		/*This sole thread is dedicated for starting and maintaining server connection. 
		  It calls the startsServerConnection method that actually initializes the server.*/
		Thread t = new Thread () {
			
			@Override
			public void run() {
				startServerConnection();
			};
		};
		t.start();
	}
	
	/*Method to initialize the server connection.*/
	protected void startServerConnection() {

		try {
			ss = new ServerSocket(port);
			userNames = new ArrayList<>(12);
			streams = new ArrayList<>(12);
			textArea.append("-------SERVER STARTED------\n");
			
		//	String path = System.getProperty("user.dir");
		//	textArea.append(path);
			fr = new FileReader("ServerLog.txt");
			br = new BufferedReader(fr);
			
			String smh = "";
			while(!((smh = br.readLine())==null)) {
				
				textArea.append(smh);
				textArea.append("\n");
			}
			br.close();
			
			fw = new FileWriter("ServerLog.txt");
			bw = new BufferedWriter(fw);
			
			/*This loop is used for listening client connections on server port*/
			while(true) {
				
				/*Client has connected to server socket*/
				client = ss.accept();
				
				dos = new DataOutputStream(client.getOutputStream());
				streams.add(dos);
				
				
				/*We create an instance of this client socket's handler(which is a nested class within 
				  this ServerNew java class and pass the parameters: client socket 
				and that socket's dataoutput stream in the serverclienthandler's constructor*/
				
				ServerClientHandler sch = new ServerClientHandler(client, dos);
				
				/*Initiate the thread for handling this sole client session*/
				sch.start();
				
			}
			
		}
		
		/*In case the ServerLog.txt file is missing this application will not work, as it will throw 
		this exception*/
		catch (FileNotFoundException fe) {
			textArea.append("No previous logs to fetch. \n");
		}
		catch (IOException e) {
			// TODO: handle exception
			e.getMessage();
		}
	}
	
	/*Method for BROADCASTING client messages and 
	 all events like client login, logout etc
	 to all online clients.*/
	public void SendDataAllClients(String msg) {
		
		for (DataOutputStream dataOutputStream : streams) {
			try {
				dataOutputStream.writeUTF(msg);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	
	/*Nested Class used for MultiThreading and Handling multiple clients at same time.*/
	public class ServerClientHandler extends Thread {
		
		private Socket csoc;
		private String cname;
		private DataInputStream diss;
		
		public ServerClientHandler(Socket client, DataOutputStream dosss) {
			// TODO Auto-generated constructor stub
			this.csoc = client;
			try {
				diss = new DataInputStream(csoc.getInputStream());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.getMessage();
			}
		}
		
		/*overriding the thread's run method. this method runs as soons as we send command:
		thread.start();*/
		
		@Override
		public void run() {
			
			String line = "",msgin;
			String arr[];
			
			try {

				while(true) {
				
					line = diss.readUTF();
					
					arr = line.split("\n");
					
					/*Reconstructing the message body from the Http Header.
					  This code decodes the Http message body.*/
					if(arr[0].contains("POST")) {
						
						msgin = arr[0].split("/")[1];
						
						if(msgin.contains("{")) {
							cname = msgin.split("\\{")[1];
							cname = cname.replace(cname.substring(cname.length()-1),"");
							textArea.append(line);
							textArea.append("New Client connected: "+cname+"\n");
							SendDataAllClients("CONNECTED:"+cname);
							userNames.add(cname);
							bw.write(line);
							bw.write("New Client connected: "+cname);
							bw.newLine();
						}
						else {
							textArea.append(line);
							textArea.append(cname+": "+msgin+"\n");
							SendDataAllClients(cname+": "+msgin);
							bw.write(line);
							bw.write(cname+": "+msgin+"\n");
							bw.newLine();
						}
					}
					else {
							textArea.append(cname+" has LOGGED OUT\n");
							SendDataAllClients("LOGGEDOFF:"+cname);
							/*Removing the client's username from it's memory or arraylist*/
							userNames.remove(cname);
							
							/*Removing the client socket's dataoutput stream from it's memory or arraylist*/
						//	streams.remove(dos);
							
							/*the client socket's datainput stream is set to null so that
							it does not receive any data even after client has been logged out.*/
							this.diss = null;
							
							/*Logging the logoff event in the db/file*/
							bw.write(cname+" has LOGGED OUT\n");
							bw.newLine();
							break;
					}
					
				}
					
			} 
			
			/*In case client connection is disconnected, even if client does not press LOGOUT button,
			the server will close the client connection and log it off*/
			catch (IOException e) {

					textArea.append(cname+" has LOGGED OUT\n");
					SendDataAllClients("LOGGEDOFF:"+cname);
					
					/*Removing the client's username from it's memory or arraylist*/
					userNames.remove(cname);
					/*Removing the client socket's dataoutput stream from it's memory or arraylist*/
				//	streams.remove(dos);
					try {
						/*writing in the file for Server Logging*/
						bw.write(cname+" has LOGGED OUT\n");
						bw.newLine();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

	}
