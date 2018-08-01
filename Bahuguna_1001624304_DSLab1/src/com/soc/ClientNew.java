/*Name- Akshay Bahuguna
ID- 1001624304*/

package com.soc;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class ClientNew {

	private JTextField textMessage;
	private int port = 9998;
	private String ip = "localhost";
	private DataOutputStream dos;
	private static DataInputStream dis;
	private Socket clientsoc;
	private static String clientname;
	
	/*ArrayList to store names of all online clients, 
	including the username of the current client*/
	private static ArrayList<String> otherusers;
	
	/*HashMap to store the mapping of client's username and timestamp of their last message.
	It is used to implement the timer.*/
	private static HashMap<String, String> usertimestamp;
	
	/*Boolean flag to indicate whether the client is logged in or logged out*/
	private boolean connected = false;
	
	/*Regex for filtering bad usernames (not alpha-numeric)*/
	public String regex = "^[a-zA-Z0-9]+$";
	
	/*Static Http variables used for building http request headers*/
	private final static String host = "Host: localhost";
	private final static String userAgent = "User-Agent: MultiChat/2.0";
	private final static String contentType = "Content-Type: text/html";
	private final static String contentlength = "Content-Length: ";
	private final static String date = "Date: ";
	private final static String connection = "Connection: close";
	
	private JFrame frame;
	private JTextField textRegName;
	private static JTextArea chatArea;
	private JScrollPane scrollPane;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientNew window = new ClientNew();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		String readChat = ""; String arr[];
		SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
		
		while(true) {
			
			try {
				readChat = dis.readUTF();
				arr = readChat.split(":");
				
				if(arr[0].equals("CONNECTED")) {
					otherusers.add(arr[1]);
					readChat = arr[1]+" has Connected";
				}
				else if(arr[0].equals("LOGGEDOFF")) {
					readChat = arr[1]+" has LOGGED OUT.";
					otherusers.remove(arr[1]);
					usertimestamp.remove(arr[1]);
				}
				else {
					if(!usertimestamp.containsKey(arr[0])) {
						String time1 = sdf.format(Calendar.getInstance().getTime());
						usertimestamp.put(arr[0], time1);
						
						readChat = arr[0]+" :(00:00) - "+arr[1];
					}
					else {
						
						Calendar cl = Calendar.getInstance();
						String curtime = sdf.format(cl.getTime());
						
						String timedif = subtractTime(curtime , usertimestamp.get(arr[0]));
						readChat = arr[0]+" :("+timedif+") - "+arr[1];
						usertimestamp.put(arr[0], curtime);
					}
					
				}
				chatArea.append(readChat+"\n");
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}

	/*Method for calculating time difference between the current and last message for that particular client*/
	private static String subtractTime(String curtime, String string) {

		String ff = "";
		
		int m2 = Integer.parseInt(curtime.substring(0,2));
		int m1 = Integer.parseInt(string.substring(0,2));
		
		int s2 = Integer.parseInt(curtime.substring(3,5));
		int s1 = Integer.parseInt(string.substring(3,5));
		
		if(s2<s1) {
			ff = ((m2-m1)<0?Math.abs(m2-m1+59):m2-m1-1)+":"+Math.abs(s2-s1+60);
		}
		else {
			ff = ((m2-m1)<0?Math.abs(m2-m1+60):m2-m1)+":"+(s2-s1);
		}
		return ff;
	}

	/**
	 * Create the application.
	 */
	public ClientNew() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(500, 500, 500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msgg = textMessage.getText();
				StringBuilder sbpostreq = new StringBuilder();
				
				try {
					
					/*Building HTTP Post method header to send to client*/
					sbpostreq.append("POST /").append(msgg).append("/ HTTP/1.1\r\n").append(host).append("\r\n").
					append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(msgg.length()).append("\r\n").
					append(date).append(new Date()).append("\r\n");
					
					dos.writeUTF(sbpostreq.toString());
				} catch (IOException e1) {
					e1.getMessage();
				}
				textMessage.setText("");
			}
		});
		btnSend.setBounds(380, 328, 89, 40);
		frame.getContentPane().add(btnSend);
		
		textMessage = new JTextField();
		textMessage.setBounds(25, 320, 345, 57);
		frame.getContentPane().add(textMessage);
		textMessage.setColumns(10);
		
		JButton btnlogin = new JButton("Connect");
		btnlogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/*Code to make sure the already logged in client can't connect again by using boolean flag
*/				if(connected == true) {
					JOptionPane.showMessageDialog(null, "You are already connected !");
				}

				/*Only if client is not already logged in, will it be able to 
				send connection request to server*/
				else if(connected == false) {
					
					clientname = textRegName.getText();
					
					/*Checking for bad client usernames and accepting only alphanumeric names*/
					if(clientname.equals(null)||clientname.trim().isEmpty()||(!Pattern.matches(regex, clientname)))
					{
						JOptionPane.showMessageDialog(null, "Please enter an alphanumeric username to connect to server! ");
					}
					
					else {
						
						/*calling the method to start client connection.*/
						startClientConnection();
					}
				}
			}
		});
		btnlogin.setBounds(270, 60, 89, 23);
		frame.getContentPane().add(btnlogin);
		
		/*Implementation of Client's LogOut feature by clicking LogOut button*/
		JButton btnlogout = new JButton("LOGOUT");
		btnlogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					StringBuilder sbconnreq = new StringBuilder();
					
					/*Building the HTTP Request for LogOut i.e Closing the connection*/ 
					sbconnreq.append("GET /").append(" ").append("/ HTTP/1.1\r\n").append(host).append("\r\n").
					append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(clientname.length()).append("\r\n").
					append(date).append(new Date()).append("\r\n").append(connection).append("\r\n");
					
					dos.writeUTF(sbconnreq.toString());
					
					JOptionPane.showMessageDialog(null, "You have logged out.");
					
					/*Boolean flag set to false to indicate this client can login again*/
					connected = false;
					
					clientsoc.close();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnlogout.setBounds(177, 406, 89, 23);
		frame.getContentPane().add(btnlogout);
		
		textRegName = new JTextField();
		textRegName.setBounds(64, 61, 188, 20);
		frame.getContentPane().add(textRegName);
		textRegName.setColumns(10);
		
		/*Button for regsitering username to server*/
		JLabel lblRegisterMsg = new JLabel("Register username with server");
		lblRegisterMsg.setBounds(65, 31, 195, 14);
		frame.getContentPane().add(lblRegisterMsg);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(25, 96, 428, 193);
		frame.getContentPane().add(scrollPane);
		
		chatArea = new JTextArea();
		scrollPane.setViewportView(chatArea);
		chatArea.setEditable(false);
		
	}

	/*Requests the server for Connection by creating as stream socket fotr the server port number- 9998*/
	private void startClientConnection() {
			
	//	StringBuilder sbreq = new StringBuilder();
		
		try {
			
			/*making connection request*/
			clientsoc = new Socket(ip,port);
			
			/*Input and output streams for data sending and receiving through client and server sockets.*/
			dis = new DataInputStream(clientsoc.getInputStream());	
			dos = new DataOutputStream(clientsoc.getOutputStream());
			
			StringBuilder sbconnreq = new StringBuilder();

			/*Building the Http Connection Request and passing Client name as body. Thus the Http Header
			are encoded around the client name data.*/
			sbconnreq.append("POST /").append("{"+clientname+"}").append("/ HTTP/1.1\r\n").append(host).append("\r\n").
			append(userAgent).append("\r\n").append(contentType).append("\r\n").append(contentlength).append(clientname.length()).append("\r\n").
			append(date).append(new Date()).append("\r\n");
			
			dos.writeUTF(sbconnreq.toString());

			otherusers = new ArrayList<>(10);
			usertimestamp = new HashMap<>(10);
			JOptionPane.showMessageDialog(null, "You have logged in. You can start Chatting: " + clientname);
			connected = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
