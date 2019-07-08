package server;

import java.util.ArrayList;

public class WholeOrder
{
	private ArrayList<Order> orders = new ArrayList<Order>();
	
	public WholeOrder()
	{
		super();
	}
	
	public void addOrder(Order newOrder)
	{
		this.orders.add(newOrder);
	}
	
	public ArrayList<Order> getOrder()
	{
		return orders;
	}

}
