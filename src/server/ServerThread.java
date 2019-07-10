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

import org.apache.logging.log4j.Logger; 
import org.apache.logging.log4j.LogManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


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
												String[] splited = everyFood.get(i).getPrice().split("\\s+");
												double priceFood = Double.parseDouble(splited[0]);
												
												orderList.add(
														new Order(everyFood.get(i).getName(), Integer.parseInt(pizzaId),
																Integer.parseInt(pizzaQuantity), priceFood*Integer.parseInt(pizzaQuantity), everyFood.get(i)));
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
								while (true)
								{
									pizzaId = din.readUTF();
									pizzaQuantity = din.readUTF();
									try
									{
										shouldLeave = enterProduct(pizzaId, pizzaQuantity);
									} catch (NumberFormatException e)
									{
										log4j.error("Error exiting from order!");
										shouldLeave = true;
									}
									if (shouldLeave)
										break;
								}

								break;
							case "3":
								dout.writeUTF("View orders: \n1. Current order\n2. Past orders");
								dout.flush();
								double totalPrice = 0;
								String orderChoice = din.readUTF();
								if (orderChoice.equals("1"))
								{
									String currentOrder = "Your current order:\n";
									for (Order order : orderList)
									{
										currentOrder += order.toString() + "\n";
										totalPrice += order.getPrice();
									}
									dout.writeUTF(currentOrder + "\n Total price: " + String.valueOf(String.format("%.2f", totalPrice)) + " BGN");
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
										"Enter what product you want to delete: \n1.Pizza 2.Drink 3.Sauce 4.Dessert");
								dout.flush();
							
								String productDelete = din.readUTF();
							
								
								switch(productDelete)
								{
								case "1":
									dout.writeUTF(
											"To delete a pizza please enter the pizza id and the special admin password to confirm");
									dout.flush();
									
									String pizzaId = din.readUTF();
									int pizzaIdInt = Integer.parseInt(pizzaId);
									String specialPass = din.readUTF();

									for (int i = 0; i < pizzaList.size(); i++)
									{
										if (pizzaList.get(i).getId() == Integer.parseInt(pizzaId))
										{
											if (specialPass.equalsIgnoreCase(Accounts.getSpecialPass()))
											{
												deletePizza(pizzaIdInt);
												pizzaList.remove(i);
												dout.writeUTF("The pizza you entered has been removed");
												dout.flush();
												break;
											}
										}
									}
									break;
								case "2":
									dout.writeUTF(
											"To delete a drink please enter the drink id and the special admin password to confirm");
									dout.flush();
									
									String drinkId = din.readUTF();
									int drinkIdInt = Integer.parseInt(drinkId);
									String specialPassDrink = din.readUTF();

									for (int i = 0; i < drinkList.size(); i++)
									{
										if (drinkList.get(i).getId() == Integer.parseInt(drinkId))
										{
											if (specialPassDrink.equalsIgnoreCase(Accounts.getSpecialPass()))
											{
												deleteDrink(drinkIdInt);
												drinkList.remove(i);
												dout.writeUTF("The drink you entered has been removed");
												dout.flush();
												break;
											}
										}
									}
									break;
								case "3":
									dout.writeUTF(
											"To delete a sauce please enter the sauce id and the special admin password to confirm");
									dout.flush();
									
									String sauceId = din.readUTF();
									int sauceIdInt = Integer.parseInt(sauceId);
									String specialPassSauce = din.readUTF();

									for (int i = 0; i < sauceList.size(); i++)
									{
										if (sauceList.get(i).getId() == Integer.parseInt(sauceId))
										{
											if (specialPassSauce.equalsIgnoreCase(Accounts.getSpecialPass()))
											{
												deleteSauce(sauceIdInt);
												sauceList.remove(i);
												dout.writeUTF("The sauce you entered has been removed");
												dout.flush();
												break;
											}
										}
									}
									break;
								case "4":
									dout.writeUTF(
											"To delete a dessert please enter the sauce id and the special admin password to confirm");
									dout.flush();
									
									String dessertId = din.readUTF();
									int dessertIdInt = Integer.parseInt(dessertId);
									String specialPassDessert = din.readUTF();

									for (int i = 0; i < dessertList.size(); i++)
									{
										if (dessertList.get(i).getId() == Integer.parseInt(dessertId))
										{
											if (specialPassDessert.equalsIgnoreCase(Accounts.getSpecialPass()))
											{
												deleteDessert(dessertIdInt);
												dessertList.remove(i);
												dout.writeUTF("The dessert you entered has been removed");
												dout.flush();
												break;
											}
										}
									}
									break;
								default:
									dout.writeUTF(
											"Error could not delete: Either the pizza id you entered or the special password was wrong!");
									dout.flush();
									break;
								}
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
			log4j.info("Something went wrong with one of the previous options. Please try again!"); 
		}
	}

	private boolean enterProduct(String pizzaId, String pizzaQuantity)
	{
		double currentPrice = 0;
		double totalPrice = 0;
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
							String[] splited = everyFood.get(i).getPrice().split("\\s+");
							double priceFood = Double.parseDouble(splited[0]);
							currentPrice = priceFood*Integer.parseInt(pizzaQuantity);
							orderList.add(
									new Order(everyFood.get(i).getName(), Integer.parseInt(pizzaId),
											Integer.parseInt(pizzaQuantity), currentPrice, everyFood.get(i)));
							
							dout.writeUTF("Item added to your cart.");
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
			log4j.error("Error entering product!"); 
		}
		return false;
	}

	private String seeMenu()
	{
		String menu = "Our pizza menu:\n\n";
		
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
					PassHash passHash = new PassHash();
					Accounts newAccount = new Accounts(username, passHash.PassHash(password), typeOfAcc);
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
						PassHash passHash2 = new PassHash();
						Accounts newAccount = new Accounts(username, passHash2.PassHash(password), typeOfAcc);
						accounts.add(newAccount);
						
						JSONObject AccountObject = new JSONObject();
						AccountObject.put("account", newAccount);
					    
					    writeAccount(AccountObject);
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
			log4j.error("Something went wrong with registration. Try again!"); 
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
				PassHash passCheck = new PassHash();
				if (typeOfAcc.equals("client"))
				{
					if (passCheck.PassHash(password).equals(accountPass))
						return "client has logged in";
					else
						return "Error";
				} else
				{
					if (passCheck.PassHash(password).equals(accountPass))
						return "admin has logged in";
					else
						return "Error";
				}
			}
		} catch (Exception e)
		{
			log4j.error("Something went wrong with LOGIN. Try again!"); 
		}
		return "Error";
	}

	// Files functions
	
	private synchronized void deletePizza(int id)
	{
		JSONParser jsonParser = new JSONParser();
	    
	    try (FileReader reader = new FileReader("pizzas.json"))
	    {
	        Object obj = null;

	        try
	        {
	        	obj = jsonParser.parse(reader);
	        } catch (org.json.simple.parser.ParseException e)
	        {
	        	log4j.error("Error parsing JSON object!"); 
	        }
	       
	        itemsList = (JSONArray) obj;

	        for (int i=0; i < itemsList.size(); i++) {
	        	Pizza testPizza = parseItemObjectPizza( (JSONObject) itemsList.get(i));
	        	pizzaList.add(testPizza);
				everyFood.add(testPizza);
	        }
	        
	        for(int i=0; i < pizzaList.size(); i++)
	        {
	        	if(pizzaList.get(i).getId()==id) {
	        	    itemsList.remove(i);
	        	    }
	        }
	        
	        reader.close();

	    } catch (FileNotFoundException e) {
	    	log4j.error("Error! File NOT FOUND!"); 
	    } catch (IOException e) {
	    	log4j.error("Error reading from file!"); 
	    } catch (Exception e) {
	    	log4j.error("Something went wrong with the file!"); 
	    }

	    try (FileWriter file = new FileWriter("pizzas.json")) {


	        file.write(itemsList.toJSONString());
	        file.flush();
	        file.close();

	    } catch (IOException e) {
	    	log4j.error("Error writing in file!"); 
	    }
	}
	
	private synchronized void deleteDrink(int id)
	{
		JSONParser jsonParser = new JSONParser();
	    
	    try (FileReader reader = new FileReader("drinks.json"))
	    {
	        Object obj = null;

	        try
	        {
	        	obj = jsonParser.parse(reader);
	        } catch (org.json.simple.parser.ParseException e)
	        {
	        	log4j.error("Error parsing JSON object!"); 
	        }
	       
	        itemsList = (JSONArray) obj;

	        for (int i=0; i < itemsList.size(); i++) {
	        	Drink testDrink = parseItemObjectDrinks( (JSONObject) itemsList.get(i));
	        	drinkList.add(testDrink);
				everyFood.add(testDrink);
	        }
	        
	        for(int i=0; i < drinkList.size(); i++)
	        {
	        	if(drinkList.get(i).getId()==id) {
	        	    itemsList.remove(i);   	     
	        	}
	        }
	        
	        reader.close();

	    } catch (FileNotFoundException e) {
	    	log4j.error("Error! File NOT FOUND!"); 
	    } catch (IOException e) {
	    	log4j.error("Error reading from file!"); 
	    } catch (Exception e) {
	    	log4j.error("Something went wrong with the file!"); 
	    }

	    try (FileWriter file = new FileWriter("drinks.json")) {


	        file.write(itemsList.toJSONString());
	        file.flush();
	        file.close();

	    } catch (IOException e) {
	    	log4j.error("Error writing in file!"); 
	    }
	}
	
	private synchronized void deleteSauce(int id)
	{
		JSONParser jsonParser = new JSONParser();
	    
	    try (FileReader reader = new FileReader("sauces.json"))
	    {
	        Object obj = null;

	        try
	        {
	        	obj = jsonParser.parse(reader);
	        } catch (org.json.simple.parser.ParseException e)
	        {
	        	log4j.error("Error parsing JSON object!"); 
	        }
	       
	        itemsList = (JSONArray) obj;

	        for (int i=0; i < itemsList.size(); i++) {
	        	Sauce testSauce = parseItemObjectSauce( (JSONObject) itemsList.get(i));
	        	sauceList.add(testSauce);
				everyFood.add(testSauce);
	        }
	        
	        for(int i=0; i < sauceList.size(); i++)
	        {
	        	if(sauceList.get(i).getId()==id) {
	        	    itemsList.remove(i);       	     
	        	}
	        }
	        
	        reader.close();

	    } catch (FileNotFoundException e) {
	    	log4j.error("Error! File NOT FOUND!"); 
	    } catch (IOException e) {
	    	log4j.error("Error reading from file!"); 
	    } catch (Exception e) {
	    	log4j.error("Something went wrong with the file!"); 
	    }
	
	    try (FileWriter file = new FileWriter("sauces.json")) {


	        file.write(itemsList.toJSONString());
	        file.flush();
	        file.close();

	    } catch (IOException e) {
	    	log4j.error("Error writing in file!"); 
	    }
	}
	
	private synchronized void deleteDessert(int id)
	{
		JSONParser jsonParser = new JSONParser();
	    
	    try (FileReader reader = new FileReader("desserts.json"))
	    {
	        Object obj = null;

	        try
	        {
	        	obj = jsonParser.parse(reader);
	        } catch (org.json.simple.parser.ParseException e)
	        {
	        	log4j.error("Error parsing JSON object!"); 
	        }
	       
	        itemsList = (JSONArray) obj;

	        for (int i=0; i < itemsList.size(); i++) {
	        	Dessert testDessert = parseItemObjectDessert ((JSONObject) itemsList.get(i));
	        	dessertList.add(testDessert);
				everyFood.add(testDessert);
	        }
	        
	        for(int i=0; i < dessertList.size(); i++)
	        {
	        	if(dessertList.get(i).getId()==id) {
	        	    itemsList.remove(i);

	       	     
	        	}
	        }
	        
	        reader.close();

	    } catch (FileNotFoundException e) {
	    	log4j.error("Error! File NOT FOUND!"); 
	    } catch (IOException e) {
	    	log4j.error("Error reading from file!"); 
	    } catch (Exception e) {
	    	log4j.error("Something went wrong with the file!"); 
	    }
	

	    try (FileWriter file = new FileWriter("desserts.json")) {


	        file.write(itemsList.toJSONString());
	        file.flush();
	        file.close();

	    } catch (IOException e) {
	    	log4j.error("Error writing in file!"); 
	    }
	}

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

		try (FileReader reader = new FileReader("accounts.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				log4j.error("Error parsing JSON object!"); 
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Accounts acc = parseItemObjectAccount((JSONObject) itemsList.get(i));

				accounts.add(acc);
			}

		 } catch (FileNotFoundException e) {
		    	log4j.error("Error! File NOT FOUND!"); 
		    } catch (IOException e) {
		    	log4j.error("Error reading from file!"); 
		    } catch (Exception e) {
		    	log4j.error("Something went wrong with the file!"); 
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

		try (FileReader reader = new FileReader("sauces.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				log4j.error("Error parsing JSON object!"); 
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Sauce food = parseItemObjectSauce((JSONObject) itemsList.get(i));

				sauceList.add(food);
				everyFood.add(food);
			}
		 } catch (FileNotFoundException e) {
		    	log4j.error("Error! File NOT FOUND!"); 
		    } catch (IOException e) {
		    	log4j.error("Error reading from file!"); 
		    } catch (Exception e) {
		    	log4j.error("Something went wrong with the file!"); 
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

		try (FileReader reader = new FileReader("drinks.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				log4j.error("Error parsing JSON object!"); 
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Drink food = parseItemObjectDrinks((JSONObject) itemsList.get(i));

				drinkList.add(food);
				everyFood.add(food);
			}

		 } catch (FileNotFoundException e) {
		    	log4j.error("Error! File NOT FOUND!"); 
		    } catch (IOException e) {
		    	log4j.error("Error reading from file!"); 
		    } catch (Exception e) {
		    	log4j.error("Something went wrong with the file!"); 
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

		try (FileReader reader = new FileReader("pizzas.json"))
		{
			Object obj = null;
			try
			{
				obj = jsonParser.parse(reader);
			} catch (org.json.simple.parser.ParseException e)
			{
				log4j.error("Error parsing JSON object!"); 
			}

			JSONArray itemsList = (JSONArray) obj;

			for (int i = 0; i < itemsList.size(); i++)
			{
				Pizza testPizza = parseItemObjectPizza((JSONObject) itemsList.get(i));

				pizzaList.add(testPizza);
				everyFood.add(testPizza);
			}

		 } catch (FileNotFoundException e) {
		    	log4j.error("Error! File NOT FOUND!"); 
		    } catch (IOException e) {
		    	log4j.error("Error reading from file!"); 
		    } catch (Exception e) {
		    	log4j.error("Something went wrong with the file!"); 
		    }
		
	}
	
	private synchronized void writeAccount(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("accounts.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	log4j.error("Error parsing JSON object!"); 
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Accounts testAccount = parseItemObjectAccount( (JSONObject) itemsList.get(i));
        	accounts.add(testAccount);

        }
        
        reader.close();
        
    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (IOException e) {
    	log4j.error("Error reading from file!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }
   
    itemsList.add(Jobj);
  
    try (FileWriter file = new FileWriter("accounts.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
    	log4j.error("Error writing in file!"); 
    }
    }
	

	private synchronized void writePizzaAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("pizzas.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	log4j.error("Error parsing JSON object!"); 
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Pizza testPizza = parseItemObjectPizza( (JSONObject) itemsList.get(i));
        	pizzaList.add(testPizza);
			everyFood.add(testPizza);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (IOException e) {
    	log4j.error("Error reading from file!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }
   
    itemsList.add(Jobj);
    
    System.out.println("Pizza successfully added to menu!");
     

    try (FileWriter file = new FileWriter("pizzas.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
    	log4j.error("Error writing in file!"); 
    }
    }
	
	private synchronized void writeDrinkAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("drinks.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	log4j.error("Error parsing JSON object!"); 
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Drink testDrink = parseItemObjectDrinks( (JSONObject) itemsList.get(i));
        	drinkList.add(testDrink);
			everyFood.add(testDrink);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (IOException e) {
    	log4j.error("Error reading from file!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }
 
    itemsList.add(Jobj);
    
    System.out.println("Drink successfully added to menu!");
     

    try (FileWriter file = new FileWriter("drinks.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }

    }
	
	private synchronized void writeSauceAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("sauces.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	log4j.error("Error parsing JSON object!"); 
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Sauce testSauce = parseItemObjectSauce( (JSONObject) itemsList.get(i));
        	sauceList.add(testSauce);
			everyFood.add(testSauce);
        }
        
        reader.close();

    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (IOException e) {
    	log4j.error("Error reading from file!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }
   
    itemsList.add(Jobj);
    
    System.out.println("Sauce successfully added to menu!");
     

    try (FileWriter file = new FileWriter("sauces.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
    	log4j.error("Error writing in file!"); 
    }
    }
	
	private synchronized void writeDessertAdmin(JSONObject Jobj)
    {
	JSONParser jsonParser = new JSONParser();
    
    try (FileReader reader = new FileReader("desserts.json"))
    {
        Object obj = null;

        try
        {
        	obj = jsonParser.parse(reader);
        } catch (org.json.simple.parser.ParseException e)
        {
        	log4j.error("Error parsing JSON object!"); 
        }
       
        itemsList = (JSONArray) obj;

        for (int i=0; i < itemsList.size(); i++) {
        	Dessert testDessert = parseItemObjectDessert( (JSONObject) itemsList.get(i));
        	dessertList.add(testDessert);
			everyFood.add(testDessert);
        }
        
        reader.close();
        
    } catch (FileNotFoundException e) {
    	log4j.error("Error! File NOT FOUND!"); 
    } catch (IOException e) {
    	log4j.error("Error reading from file!"); 
    } catch (Exception e) {
    	log4j.error("Something went wrong with the file!"); 
    }


	
   
    itemsList.add(Jobj);
    
    System.out.println("Dessert successfully added to menu!");
     

    try (FileWriter file = new FileWriter("desserts.json")) {


        file.write(itemsList.toJSONString());
        file.flush();
        file.close();

    } catch (IOException e) {
    	log4j.error("Error writing in file!"); 
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
        
        try (FileReader reader = new FileReader("desserts.json"))
        {
        	Object obj = null;
        	try
        	{
        		obj = jsonParser.parse(reader);
        	} catch (org.json.simple.parser.ParseException e)
        	{
        		log4j.error("Error parsing JSON object!"); 
        	}
        	
        	
            JSONArray itemsList = (JSONArray) obj;

            for (int i = 0; i < itemsList.size(); i++)
			{
				Dessert dess = parseItemObjectDessert((JSONObject) itemsList.get(i));

				dessertList.add(dess);
				everyFood.add(dess);
			}
 
        } catch (FileNotFoundException e) {
	    	log4j.error("Error! File NOT FOUND!"); 
	    } catch (IOException e) {
	    	log4j.error("Error reading from file!"); 
	    } catch (Exception e) {
	    	log4j.error("Something went wrong with the file!"); 
	    }
	
    }
	
	private static final Logger log4j = LogManager.getLogger(ServerThread.class.getName());
}


// Fix the logging out - you can log out but then when you log in you can't enter anything
// Fix the options after the register happens (currently you can register but it doesn't allow you to continue normally)