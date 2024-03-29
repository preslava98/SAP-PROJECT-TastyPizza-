package server;

public class Food
{
	private int id;
	private String name;
	private String size;
	private String price;
	
	public String toString()
	{
		return id + ". "+ name + ", " + size + ", " + price;
	}
	
	public Food(int id, String name, String size, String price)
	{
		this.id = id;
		this.name = name;
		this.size = size;
		this.price = price;
	}
	public int getId()
	{
		return id;
	}
	public String getName()
	{
		return name;
	}
	public String getSize()
	{
		return size;
	}
	public String getPrice()
	{
		return price;
	}
}
