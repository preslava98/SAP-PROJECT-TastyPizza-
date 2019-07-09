package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

public class ServerThread implements Runnable
{

	private Socket socket;
	private ArrayList<Accounts> accounts;
	private boolean loggedIn = false;
	private String typeOfAccount = "";
	private String menuOptionsClient = " 1. See Menu\n 2. Create order\n 3. Check orders \n 4. Finish and order \n 5. Log out \n 6. Exit";
	private String menuOptionsAdmin = " 1. Add new pizzas and products\n 2. Edit/Delete products\n 3. See menu \n 4. Process orders "
			+ "\n 5. Log out \n 6. Exit";
	private DataOutputStream dout;
	private DataInputStream din;
	static JSONArray itemsList = new JSONArray();
	private ArrayList<Pizza> pizzaList;
	private List<Sauce> sauceList;
	private List<Order> orderList;
	private List<WholeOrder> pastOrders;
	private List<Food> everyFood;
	private List<Drink> drinkList;
	private List<Dessert> dessertList;
	//static JSONArray itemsList = new JSONArray();

	public ServerThread(Socket socket)
	{
		this.socket = socket;
	}

	public void start()
	{
		run();
	}

	@Override
	public void run()
	{

		everyFood = new ArrayList<Food>();
		pastOrders = new ArrayList<WholeOrder>();
		orderList = new ArrayList<Order>();
		sauceList = new ArrayList<Sauce>();
		pizzaList = new ArrayList<Pizza>();
		dessertList = new ArrayList<Dessert>();
		drinkList = new ArrayList<Drink>();
		

		accounts = new ArrayList<Accounts>();
		accounts.add(new Accounts("1", "1", "client"));
		accounts.add(new Accounts("2", "2", "admin"));
		
		readAccountsClient();
		readPizzasClient();
		readSaucesClient();
		readDrinksClient();
		readDessertsClient();


		try
		{
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());

				String input = din.readUTF();
				if (input.equals("1"))
				{
					dout.writeUTF("Login: Please enter username and password. ");
					dout.flush();
					while (!loggedIn)
					{
						String logAnwser = login();
						if (logAnwser.equalsIgnoreCase("client has logged in"))
						{
							dout.writeUTF("Successfully logged in as client!\nMenu options:\n" + menuOptionsClient);
							dout.flush();
							loggedIn = true;
							typeOfAccount = "client";
						} else if (logAnwser.equalsIgnoreCase("admin has logged in"))
						{
							dout.writeUTF("Successfully logged in as admin!\nMenu options:\n" + menuOptionsAdmin);
							dout.flush();
							loggedIn = true;
							typeOfAccount = "admin";
						} else
						{
							dout.writeUTF(
									"Error. Your name or password did not match. Please enter the correct username and password:");
							dout.flush();
						}
					}
				} else if (input.equals("2"))
				{
					dout.writeUTF(
							"Register: Please enter username and password and the type of account you want to create");
					dout.flush();
					boolean didRegister = register();
					while (!didRegister)
					{
						didRegister = register();

					}

				} else if (input.equals("3"))
				{
					dout.writeUTF("Exiting...");
					dout.flush();
					return;
				} else
				{
					// you didn't choose one of the optinos
					dout.writeUTF("ERROR: You didn't choose one of the options.");
					dout.flush();
				}
			
				if (loggedIn)
				{
					while (true)
					{
						if (!loggedIn)
							break;
						String menuChoice = din.readUTF();
						if (typeOfAccount.equals("client"))
						{
							switch (menuChoice)
							{
							case "1":
								String wholeMenu = seeMenu();
								dout.writeUTF(wholeMenu);
								dout.flush();
								break;
							case "2":
								dout.writeUTF("Enter id and quantity of product. To quit ordering press 0.");
								dout.flush();
								String pizzaId = "";
								boolean found = false;
								boolean shouldLeave = false;
								pizzaId = din.readUTF();
								String pizzaQuantity = din.readUTF();

								if (pizzaId.equals("0"))
								{
									dout.writeUTF(
											"Exiting... You can add anything you want before you finish your order.");
									dout.flush();
									break;
								} else if (Integer.parseInt(pizzaId) >= 1 && Integer.parseInt(pizzaId) <= 100)
								{
									if (pizzaQuantity.equals("0"))
									{
										dout.writeUTF("Can not add element to your cart with 0 quantity!");
										dout.flush();
										break;
									} else
									{
										for (int i = 0; i < everyFood.size(); i++)
										{
											if (everyFood.get(i).getId() == Integer.parseInt(pizzaId))
											{
												orderList.add(
														new Order(everyFood.get(i).getName(), Integer.parseInt(pizzaId),
																Integer.parseInt(pizzaQuantity), everyFood.get(i)));
												dout.writeUTF("Item added to your cart");
												dout.flush();
												found = true;
											}
										}
										if (!found)
										{
											dout.writeUTF(
													"Could not find id of the product you choose, please try again:");
											dout.flush();
										}
										found = false;
									}
								} else
								{
									dout.writeUTF("No such id for product.");
									dout.flush();
								}
								// continue until they enter 0 for id
								while (true)
								{
									pizzaId = din.readUTF();
									pizzaQuantity = din.readUTF();
									try
									{
										shouldLeave = enterProduct(pizzaId, pizzaQuantity);
									} catch (NumberFormatException ee)
									{
										ee.printStackTrace();
										shouldLeave = true;
									}
									if (shouldLeave)
										break;
								}

								break;
							case "3":
								dout.writeUTF("View orders: \n1. Current order\n2. Past orders");
								dout.flush();
								String orderChoice = din.readUTF();
								if (orderChoice.equals("1"))
								{
									String currentOrder = "Your current order:\n";
									for (Order order : orderList)
									{
										currentOrder += order.toString() + "\n";
									}
									dout.writeUTF(currentOrder);
									dout.flush();
								}
								if (orderChoice.equals("2"))
								{
									String allOrders = "Your past orders:\n";

									if (pastOrders.size() > 0)
									{
										for (int i = 0; i < pastOrders.size(); i++)
										{
											allOrders += "Order num: " + (i + 1) + ":\n";
											for (int j = 0; j < pastOrders.get(i).getOrder().size(); j++)
											{
												allOrders += pastOrders.get(i).getOrder().get(j).toString() + "\n";
											}
										}

										dout.writeUTF(allOrders);
										dout.flush();
									} else
									{
										dout.writeUTF("You don't have any past orders.");
										dout.flush();
									}
								}
								break;
							case "4":
								WholeOrder finishedOrder = new WholeOrder();

								for (int i = 0; i < orderList.size(); i++)
								{
									finishedOrder.addOrder(orderList.get(i));
								}
								orderList.clear();
								pastOrders.add(finishedOrder);
								dout.writeUTF("Your order is complete and on its way!");
								dout.flush();
								break;
							case "5":
								dout.writeUTF("Logging out.");
								dout.flush();
								loggedIn = false;
								break;
							case "6":
								dout.writeUTF("Exiting...\nBye");
								dout.flush();
								din.close();
								dout.close();
								socket.close();
								return;
							default:
								dout.writeUTF("You didn't choose one of the options. Please try again.\n"
										.concat(menuOptionsClient));
								dout.flush();
								break;
							}
						} else if (typeOfAccount.equals("admin"))
						{
							while(true) {
							switch (menuChoice)
							{
							case "1":
								boolean menu2 = true;
								while(menu2) {
								dout.writeUTF("Choose what product to add: \n1. Pizza\n2. Drink\n3. Sauce\n4. Dessert\n5. Back ");
								dout.flush();
								int productChoice = din.readInt();
								switch(productChoice)
								{
								case 1:
									String name;
									String size;
									String price;
									String description;
									
									dout.writeUTF("Enter name of pizza: ");
									name = din.readUTF();
									dout.writeUTF("Enter size of pizza: ");
									size = din.readUTF();
									dout.writeUTF("Enter price of pizza: ");
									price = din.readUTF();
									dout.writeUTF("Enter description of pizza: ");
									description = din.readUTF();

									Pizza newPiz = new Pizza(pizzaList.size() + 1, name, size, price, description);
									pizzaList.add(newPiz);
									everyFood.add(newPiz);
									
									
									JSONObject PizzaObject = new JSONObject();
								    PizzaObject.put("pizza", newPiz);
								    
								    writePizzaAdmin(PizzaObject);
								    
									dout.writeUTF("You have added a new pizza! " + newPiz.getName() + ", " + newPiz.getPrice()
											+ ", " + newPiz.getSize() + ", " + newPiz.getDescription());
									dout.flush();
									break;
									
									case 2:
										String nameDrink;
										String sizeDrink;
										String priceDrink;
										
										dout.writeUTF("Enter name of drink: ");
										nameDrink = din.readUTF();
										dout.writeUTF("Enter the size of the drink: ");
										sizeDrink = din.readUTF();
										dout.writeUTF("Enter the price of the drink: ");
										priceDrink = din.readUTF();

										Drink newDrunk = new Drink(drinkList.size() + 1, nameDrink, sizeDrink, priceDrink);
										drinkList.add(newDrunk);
										everyFood.add(newDrunk);
										
										
										JSONObject DrinkObject = new JSONObject();
									    DrinkObject.put("drink", newDrunk);
									    
									    writeDrinkAdmin(DrinkObject);
									    
										dout.writeUTF("You have added a new drink! " + newDrunk.getName() + ", " + newDrunk.getPrice()
												+ ", " + newDrunk.getSize());
										dout.flush();
										break;
										
									case 3:
										String nameSauce;
										String sizeSauce;
										String priceSauce;

										dout.writeUTF("Enter the name of sauce: ");
										nameSauce = din.readUTF();
										dout.writeUTF("Enter the size of sauce: ");
										sizeSauce = din.readUTF();
										dout.writeUTF("Enter the price of sauce: ");
										priceSauce = din.readUTF();

										Sauce sos = new Sauce(sauceList.size() + 1, nameSauce, sizeSauce, priceSauce);
										sauceList.add(sos);
										everyFood.add(sos);
										
										
										JSONObject SauceObject = new JSONObject();
										SauceObject.put("sauce", sos);
									    
									    writeSauceAdmin(SauceObject);
									    
										dout.writeUTF("You have added a new sauce! " + sos.getName() + ", " + sos.getPrice()
												+ ", " + sos.getSize());
										dout.flush();
										break;
									
									case 4:
										String nameDessert;
										String sizeDessert;
										String priceDessert;
										String descrDessert;

										dout.writeUTF("Enter the name of dessert: ");
										nameDessert = din.readUTF();
										dout.writeUTF("Enter the size of dessert: ");
										sizeDessert = din.readUTF();
										dout.writeUTF("Enter the price of dessert: ");
										priceDessert = din.readUTF();
										dout.writeUTF("Enter the description of dessert: ");
										descrDessert=din.readUTF();

										Dessert newDessert = new Dessert(dessertList.size() + 1, nameDessert, sizeDessert, priceDessert, descrDessert);
										dessertList.add(newDessert);
										everyFood.add(newDessert);
										
										
										JSONObject DessertObject = new JSONObject();
										DessertObject.put("dessert", newDessert);
									    
									    writeDessertAdmin(DessertObject);
									    
										dout.writeUTF("You have added a new dessert! " + newDessert.getName() + ", " + newDessert.getPrice()
												+ ", " + newDessert.getSize() + ", " + newDessert.getDescription());
										dout.flush();
										break;
									case 5:
										menu2=false;
										break;
								
								}
								break;
								}
								break;
							case "2":
								dout.writeUTF(
										"To delete a pizza please enter the pizza id and the special admin password to confirm");
								dout.flush();
								
								String pizzaId = din.readUTF();
								String specialPass = din.readUTF();

								for (int i = 0; i < pizzaList.size(); i++)
								{
									if (pizzaList.get(i).getId() == Integer.parseInt(pizzaId))
									{
										if (specialPass.equalsIgnoreCase(Accounts.getSpecialPass()))
										{
											pizzaList.remove(i);
											dout.writeUTF("The pizza you entered has been removed");
											dout.flush();
											break;
										}
									}
								}
								dout.writeUTF(
										"Error could not delete: Either the pizza id you entered or the special password was wrong!");
								dout.flush();
								break;
							case "3":
								String wholeMenu = seeMenu();
								dout.writeUTF(wholeMenu);
								dout.flush();
								break;
							case "4":
								// dout.writeUTF("To delete a part of the order please select the number and add
								// the special password!\n" + );
								dout.flush();

								String idOfOrder = din.readUTF();
								String specialPassword = din.readUTF();
								break;
							case "6":
								System.exit(0);
								break;
							default:
								dout.writeUTF("Error you did not enter one of the options!\n" + menuOptionsAdmin);
								dout.flush();
								break;
							}
						}
						}
					}
				
				}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean enterProduct(String pizzaId, String pizzaQuantity)
	{
		try
		{
			boolean found = false;

			if (pizzaId.equals("0"))
			{
				dout.writeUTF("Exiting... You can add anything you want before you finish your order.");
				dout.flush();
				return true;
			} else if (Integer.parseInt(pizzaId) >= 1 && Integer.parseInt(pizzaId) <= 100)
			{
				if (pizzaQuantity.equals("0"))
				{
					dout.writeUTF("Can not add element to your cart with 0 quantity!");
					dout.flush();
				} else
				{
					for (int i = 0; i < everyFood.size(); i++)
					{
						if (everyFood.get(i).getId() == Integer.parseInt(pizzaId))
						{
							orderList.add(new Order(everyFood.get(i).getName(), Integer.parseInt(pizzaId),
									Integer.parseInt(pizzaQuantity), everyFood.get(i)));
							dout.writeUTF("Item added to your cart");
							dout.flush();
							found = true;
						}
					}
					if (!found)
					{
						dout.writeUTF("Could not find id of the product you choose, please try again:");
						dout.flush();
					}
					found = false;

				}
			} else
			{
				dout.writeUTF("No such id for product.");
				dout.flush();
			}
		} catch (Exception exc)
		{
			exc.printStackTrace();
		}
		return false;
	}

	private String seeMenu()
	{
		String menu = "Our pizza menu:\n\n";
		//for (Pizza pizza : pizzaList)
		//	menu += pizza.toString().concat("\n");
		
		for (int i = 0; i < pizzaList.size(); i++)
		{
			menu += pizzaList.get(i).getId() + ". " + pizzaList.get(i).getName() + ", "
					+ pizzaList.get(i).getSize() + ", " + pizzaList.get(i).getPrice() + ", \n" + pizzaList.get(i).getDescription() + "\n\n";
		}
		

		menu += "\nOur sauces:\n\n";
		for (int i = 0; i < sauceList.size(); i++)
		{
			menu += sauceList.get(i).getId() + ". " + sauceList.get(i).getName() + ", "
					+ sauceList.get(i).getSize() + ", " + sauceList.get(i).getPrice() + "\n";
		}
		
		menu += "\nOur drinks:\n\n";
		for (int i = 0; i < drinkList.size(); i++)
		{
			menu += drinkList.get(i).getId() + ". " + drinkList.get(i).getName() + ", "
					+ drinkList.get(i).getSize() + ", " + drinkList.get(i).getPrice() + "\n";
		}
		
		menu += "\nOur desserts:\n\n";
		for (int i = 0; i < dessertList.size(); i++)
		{
			menu += dessertList.get(i).getId() + ". " + dessertList.get(i).getName() + ", "
					+ dessertList.get(i).getSize() + ", " + dessertList.get(i).getPrice() + ", \n" + dessertList.get(i).getDescription() + "\n\n";
		}

		return menu;
	}

	private boolean register()
	{
		try
		{
			String username = din.readUTF();
			String password = din.readUTF();
			String typeOfAcc = din.readUTF();
			boolean exists = false;

			if (typeOfAcc.equalsIgnoreCase("client"))
			{
				for (int i = 0; i < accounts.size(); i++)
				{
					if (accounts.get(i).getUsername().equals(username))
					{
						exists = true;
					}
				}

				if (exists)
				{
					dout.writeUTF("Username already exists. Please chose unique username");
					dout.flush();
					return false;
				} else
				{
					Accounts newAccount = new Accounts(username, password, typeOfAcc);
					accounts.add(newAccount);
					
					JSONObject AccountObject = new JSONObject();
					AccountObject.put("account", newAccount);
				    
				    writeAccount(AccountObject);
					dout.writeUTF("Congratulations! You have created your new account");
					dout.flush();
					return true;
				}
			} else if (typeOfAcc.equalsIgnoreCase("admin"))
			{
				for (int i = 0; i < accounts.size(); i++)
				{
					if (accounts.get(i).getUsername().equals(username))
					{
						exists = true;
					}
				}

				if (exists)
				{
					dout.writeUTF("Username already exists. Please chose unique username");
					dout.flush();
					return false;
				} else
				{
					dout.writeUTF("Enter special password for admin:");
					dout.flush();
					String specialPassword = din.readUTF();
					if (specialPassword.equalsIgnoreCase(Accounts.getSpecialPass()))
					{
						Accounts newAccount = new Accounts(username, password, typeOfAcc);
						accounts.add(newAccount);
						dout.writeUTF("Congratulations! You have created your new account.\n\n\n" + menuOptionsAdmin);
						dout.flush();
						loggedIn = true;
						return true;
					} else
					{
						dout.writeUTF("Failed. Wrong special password!.");
						dout.flush();
						return false;
					}
				}
			} else
			{
				dout.writeUTF("Please choose client or admin.");
				dout.flush();
				return false;
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private String login()
	{
		try
		{
			String username = din.readUTF();
			String password = din.readUTF();
			String typeOfAcc = "";
			String accountPass = "";

			for (int i = 0; i < accounts.size(); i++)
			{
				if (accounts.get(i).getUsername().equals(username))
				{
					accountPass = accounts.get(i).getPassword();
					typeOfAcc = accounts.get(i).getTypeOfAcc();
				}
			}

			if (typeOfAcc == "")
			{
				return "Error";
			} else
			{
				if (typeOfAcc.equals("client"))
				{
					if (password.equals(accountPass))
						return "client has logged in";
					else
						return "Error";
				} else
				{
					if (password.equals(accountPass))
						return "admin has logged in";
					else
						return "Error";
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return "Error";
	}

	// Files functions

	private Accounts parseItemObjectAccount(JSONObject item)
	{

		JSONObject itemObject = (JSONObject) item.get("account");

		String username = (String) itemObject.get("Username");

		String password = (String) itemObject.get("Password");

		String typeOfAcc = (String) itemObject.get("Type of Account");

		Accounts account = new Accounts(username, password, typeOfAcc);

		return account;

	}
	
	private void readAccountsClient()
	{
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\accounts.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				e.printStackTrace();
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Accounts acc = parseItemObjectAccount((JSONObject) itemsList.get(i));

				accounts.add(acc);
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private Sauce parseItemObjectSauce(JSONObject item)

	{

		JSONObject itemObject = (JSONObject) item.get("sauce");

		String idS = (String) itemObject.get("id");
		int id = Integer.parseInt(idS);

		String name = (String) itemObject.get("Name");

		String size = (String) itemObject.get("Size");

		String price = (String) itemObject.get("Price");

		Sauce sauce = new Sauce(id, name, size, price);

		return sauce;

	}
	
	private void readSaucesClient()
	{
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\sauces.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				e.printStackTrace();
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Sauce food = parseItemObjectSauce((JSONObject) itemsList.get(i));

				sauceList.add(food);
				everyFood.add(food);
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private Drink parseItemObjectDrinks(JSONObject item)
	{

		JSONObject itemObject = (JSONObject) item.get("drink");

		String idS = (String) itemObject.get("id");
		int id = Integer.parseInt(idS);

		String name = (String) itemObject.get("Name");

		String size = (String) itemObject.get("Size");

		String price = (String) itemObject.get("Price");

		Drink drink = new Drink(id, name, size, price);
		return drink;

	}

	private void readDrinksClient()
	{
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\drinks.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				e.printStackTrace();
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Drink food = parseItemObjectDrinks((JSONObject) itemsList.get(i));

				drinkList.add(food);
				everyFood.add(food);
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Pizza parseItemObjectPizza(JSONObject item)
	{

		JSONObject itemObject = (JSONObject) item.get("pizza");

		String idS = (String) itemObject.get("id");
		int id = Integer.parseInt(idS);

		String name = (String) itemObject.get("Name");

		String size = (String) itemObject.get("Size");

		String price = (String) itemObject.get("Price");

		String description = (String) itemObject.get("Description");

		Pizza pizza = new Pizza(id, name, size, price, description);
		return pizza;
	}

	private void readPizzasClient()
	{
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\pizzas.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				e.printStackTrace();
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Pizza testPizza = parseItemObjectPizza((JSONObject) itemsList.get(i));

				pizzaList.add(testPizza);
				everyFood.add(testPizza);
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void writeAccount(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\accounts.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	e.printStackTrace();
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Accounts testAccount = parseItemObjectAccount( (JSONObject) itemsList.get(i));
        	accounts.add(testAccount);

        }
        
        reader.close();

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

	
   
    itemsList.add(Jobj);
  
     

    try (FileWriter file = new FileWriter("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\accounts.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
	

	private void writePizzaAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\pizzas.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	e.printStackTrace();
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Pizza testPizza = parseItemObjectPizza( (JSONObject) itemsList.get(i));
        	pizzaList.add(testPizza);
			everyFood.add(testPizza);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

	
   
    itemsList.add(Jobj);
    
    System.out.println("Pizza successfully added to menu!");
     

    try (FileWriter file = new FileWriter("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\pizzas.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
	
	private void writeDrinkAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\drinks.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	e.printStackTrace();
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Drink testDrink = parseItemObjectDrinks( (JSONObject) itemsList.get(i));
        	drinkList.add(testDrink);
			everyFood.add(testDrink);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

	
   
    itemsList.add(Jobj);
    
    System.out.println("Drink successfully added to menu!");
     

    try (FileWriter file = new FileWriter("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\drinks.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
	
	private void writeSauceAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\sauces.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	e.printStackTrace();
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Sauce testSauce = parseItemObjectSauce( (JSONObject) itemsList.get(i));
        	sauceList.add(testSauce);
			everyFood.add(testSauce);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

	
   
    itemsList.add(Jobj);
    
    System.out.println("Sauce successfully added to menu!");
     

    try (FileWriter file = new FileWriter("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\sauces.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
	
	private void writeDessertAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\desserts.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	e.printStackTrace();
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Dessert testDessert = parseItemObjectDessert( (JSONObject) itemsList.get(i));
        	dessertList.add(testDessert);
			everyFood.add(testDessert);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

	
   
    itemsList.add(Jobj);
    
    System.out.println("Dessert successfully added to menu!");
     

    try (FileWriter file = new FileWriter("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\desserts.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
	
	private Dessert parseItemObjectDessert(JSONObject item)
    {
        JSONObject itemObject = (JSONObject) item.get("dessert");
           
        String idS = (String) itemObject.get("id");
        int id = Integer.parseInt(idS);

        String name = (String) itemObject.get("Name");   
         
        String size = (String) itemObject.get("Size"); 
         
        String price = (String) itemObject.get("Price");  
        
        String description = (String) itemObject.get("Description");  
        
        Dessert dessert = new Dessert(id,name,size,price,description);
        
        return dessert;
        
    }

	private void readDessertsClient()
    {
    	JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader("C:\\Users\\Preslava\\eclipse-workspace\\TastyPizza\\desserts.json"))
        {
        	Object obj = null;
        	try
        	{
        		obj = jsonParser.parse(reader);
        	} catch (org.json.simple.parser.ParseException e)
        	{
        		e.printStackTrace();
        	}
        	
        	
            JSONArray itemsList = (JSONArray) obj;

            for (int i = 0; i < itemsList.size(); i++)
			{
				Dessert dess = parseItemObjectDessert((JSONObject) itemsList.get(i));

				dessertList.add(dess);
				everyFood.add(dess);
			}
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Things to add: When adding new product fix the exception if they enter a string instead of integer for pizzaId
// Fix the logging out - you can log out but then when you log in you can't enter anything
// Add synchronization when adding or deleting new pizzas
// Fix the options after the register happens (currently you can register but it doesn't allow you to continue normally)
// Add the delete option for the admin (idk what u mean)
// Add to the second admin optinon: another parameter which gives you what the client wants you to delete and then do what u want to do.
// Make it multi-threa