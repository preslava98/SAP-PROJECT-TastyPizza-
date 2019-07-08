package server;

public class Dessert extends Food{

	private int id;
	private String name;
	private String size;
	private String price;
	private String description;
	
	public Dessert(int id, String name, String size, String price, String description) {
		
		super(id, name, size, price);
		this.id = id;
		this.name = name;
		this.size = size;
		this.price = price;
		this.description = description;
	
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSize() {
		return size;
	}

	public String getPrice() {
		return price;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String toString()
	

	{
		return "\n{\"id\"" + ":" + "\"" + id + "\"," + "\n" +
				"\"Name\"" + ":" + "\"" + name + "\"," + "\n" +
				"\"Size\"" + ":" + "\"" + size + "\"," + "\n" +
				"\"Price\"" + ":" + "\"" + price + "\"," + "\n" +
				"\"Description\"" + ":" + "\"" + description + "\"}\n";
	}
}
