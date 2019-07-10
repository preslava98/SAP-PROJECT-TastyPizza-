package server;

public class Order
{
	private String name;
	private int quantity;
	private int id;
	private double price;
	private Food food;
	
	public Order(String name, int id, int quantity, double price, Food food)
	{
		this.name = name;
		this.id = id;
		this.quantity = quantity;
		this.price=price;
		this.food = food;
	}
	
	public String toString()
	{
		return "Item: " + name + ", Id: " + id + "., Quantity: " + quantity + 
				", Price: " + price;
	}
	
	public String getName()
	{
		return name;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public int getId()
	{
		return id;
	}

	public double getPrice()
	{
		return price;
	}

	public Food getFood()
	{
		return food;
	}
	
	

}
