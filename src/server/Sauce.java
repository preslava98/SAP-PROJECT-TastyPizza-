package server;

public class Sauce extends Food {
	
	private int id;
	private String name;
	private String size;
	private String price;
	
	public Sauce(int id, String name, String size, String price) {
		
		super(id, name, size, price);
		this.id = id;
		this.name = name;
		this.size = size;
		this.price = price;
	
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
	
	public String toString()
	
	{
		return "\n{\"id\"" + ":" + "\"" + id + "\"," + "\n" +
				"\"Name\"" + ":" + "\"" + name + "\"," + "\n" +
				"\"Size\"" + ":" + "\"" + size + "\"," + "\n" +
				"\"Price\"" + ":" + "\"" + price + "\"}\n";
	}

}
