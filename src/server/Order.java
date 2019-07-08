package server;

public class Order
{
	private String name;
	private int quantity;
	private int id;
	private Food food;
	
	public Order(String name, int id, int quantity, Food food)
	{
		this.name = name;
		this.id = id;
		this.quantity = quantity;
		this.food = food;
	}
	
	public String toString()
	{
		return "Item: ".concat(name).concat(", Id:" + id).concat(", quantity: ") + quantity;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}
