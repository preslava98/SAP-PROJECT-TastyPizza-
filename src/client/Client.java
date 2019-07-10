package client;

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;


public class Client
{
	private static DataOutputStream dout;
	private static DataInputStream din;
	private static Scanner input = new Scanner(System.in);
	private static String anwser = "end";
	private static String params[];

	public static void main(String[] args)
	{

		try
		{
			String typeOfAccount = "";
			while (true)
			{
				Socket socket = new Socket("127.0.0.1", 6969);
				din = new DataInputStream(socket.getInputStream());
				dout = new DataOutputStream(socket.getOutputStream());
				boolean loggedIn = false;
				
				boolean hasNotChosen = false;
				String entryOptions = "1. Login\n2. Register\n3. Exit";
				System.out.println(entryOptions);
				String option = input.nextLine();
				dout.writeUTF(option);
				dout.flush();
				anwser = din.readUTF();
				
				System.out.println(anwser);
				if (anwser.equalsIgnoreCase("ERROR: You didn't choose one of the options."))
				{
					hasNotChosen = true;
					System.out.println("\n" + entryOptions);
				} else
					hasNotChosen = false;

				while (hasNotChosen)
				{

					System.out.println(entryOptions);
					option = input.nextLine();
					dout.writeUTF(option);
					dout.flush();
					anwser = din.readUTF();
					System.out.println(anwser);
					if (anwser.equalsIgnoreCase("ERROR: You didn't choose one of the options."))
						hasNotChosen = true;
					else
						hasNotChosen = false;
				}

				if (anwser.equals("Exiting..."))
				{
					System.out.println("Bye");
					socket.close();
					din.close();
					dout.close();
					return;
				}

				if (anwser.equals("Login: Please enter username and password. "))
				{
					dout.writeUTF(input.nextLine());
					dout.flush();
					dout.writeUTF(input.nextLine());
					dout.flush();
					anwser = din.readUTF();
					System.out.println(anwser);
					params = anwser.split(" ");

					while (!params[0].equalsIgnoreCase("Successfully"))
					{
						dout.writeUTF(input.nextLine());
						dout.flush();
						dout.writeUTF(input.nextLine());
						dout.flush();
						anwser = din.readUTF();
						System.out.println(anwser);
						params = anwser.split(" ");
						loggedIn = true;
					}
					typeOfAccount = params[4];
					//System.out.println(typeOfAccount);

				} else if (anwser.equals(
						"Register: Please enter username and password and the type of account you want to create"))
				{
					dout.writeUTF(input.nextLine());
					dout.flush();
					dout.writeUTF(input.nextLine());
					dout.flush();
					dout.writeUTF(input.nextLine());
					dout.flush();

					anwser = din.readUTF();
					System.out.println(anwser);
					params = anwser.split(" ");
					while (!params[0].equalsIgnoreCase("Congratulations!"))
					{
						if (params[0].equalsIgnoreCase("Username"))
						{
							// Entering username password and type of account
							enterNewAccount();
						} else if (params[0].equalsIgnoreCase("Enter"))
						{
							// Entering special password
							dout.writeUTF(input.nextLine());
							dout.flush();

							anwser = din.readUTF();
							System.out.println(anwser);
							params = anwser.split(" ");
						} else if (params[0].equalsIgnoreCase("Please"))
						{
							enterNewAccount();
						} else if (params[0].equalsIgnoreCase("Failed."))
						{
							enterNewAccount();
						}
					}
				
				}
				while (true)
				{
					if (typeOfAccount.startsWith("client!"))
					{
						String choice = input.nextLine();
						dout.writeUTF(choice);
						dout.flush();
						System.out.println(din.readUTF());

						if (choice.equals("2"))
						{
							String orderNum;
							String orderQuantity;

							orderNum = input.nextLine();
							orderQuantity = input.nextLine();
							dout.writeUTF(orderNum);
							dout.flush();
							dout.writeUTF(orderQuantity);
							dout.flush();
							System.out.println(din.readUTF());

							while (!orderNum.equals("0"))
							{
								System.out.println("Continuing...");
								orderNum = input.nextLine();
								orderQuantity = input.nextLine();
								dout.writeUTF(orderNum);
								dout.flush();
								dout.writeUTF(orderQuantity);
								dout.flush();
								System.out.println(din.readUTF());
							}

						}
						if (choice.equals("3"))
						{
							dout.writeUTF(input.nextLine());
							dout.flush();
							System.out.println(din.readUTF());
						}

						if (choice.equals("6"))
							return;
					}

					else if (typeOfAccount.startsWith("admin!"))
					{
						while(true) {
						String choice = input.nextLine();
						dout.writeUTF(choice);
						dout.flush();
						System.out.println(din.readUTF());
						
						if(choice.equals("1"))
						{
							
							int choice2 = Integer.parseInt(input.nextLine());
							dout.writeInt(choice2);
							dout.flush();
							
							switch(choice2) {
							case 1:
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								break;
							case 2:
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								break;
							case 3:
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								break;
							case 4:
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								break;
							}				
							System.out.println(din.readUTF());
							break;
						}
						if(choice.equals("2"))
						{
							String choiceProduct = input.nextLine();
						    dout.writeUTF(choiceProduct);
						    dout.flush();
							
							switch(choiceProduct)
							{
							case "1":
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								break;
								
							case "2":
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								break;
								
							case "3": 
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								break;
								
							case "4":
								System.out.println(din.readUTF());
								dout.writeUTF(input.nextLine());
								dout.flush();
								dout.writeUTF(input.nextLine());
								dout.flush();
								System.out.println(din.readUTF());
								break;
							default:
								System.out.println("Something went wrong.");
								break;
							}
						
					
						}
						if(choice.equals("4"))
						{
							dout.writeUTF(input.nextLine());
							dout.flush();
							
						}
						if(choice.equals("6")) {
							System.exit(0);
						}
				
					}
					}
				}

			}
		} catch (Exception e)
		{
			log4j.info("Something went wrong with one of the previous options. Please try again!"); 
		}

	}

	private static void enterNewAccount()
	{
		try
		{
			dout.writeUTF(input.nextLine());
			dout.flush();
			dout.writeUTF(input.nextLine());
			dout.flush();
			dout.writeUTF(input.nextLine());
			dout.flush();

			anwser = din.readUTF();
			System.out.println(anwser);
			params = anwser.split(" ");
		} catch (Exception e)
		{
			log4j.error("Error entering new account!");
			e.printStackTrace();
		}
	}

	private static final Logger log4j = LogManager.getLogger(Client.class.getName());
}
