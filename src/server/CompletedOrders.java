package server;

public class CompletedOrders
{

	String completedOrder;
	boolean completed=false;
	
	public CompletedOrders(String completedOrder, boolean completed)
	{
		this.completedOrder = completedOrder;
		this.completed = completed;
	}

	public String getCompletedOrder()
	{
		return completedOrder;
	}

	public boolean isCompleted()
	{
		return completed;
	}
	
	public String toString()
	{
		return "Order: " + completedOrder;
	}

	public void setCompletedOrder(String completedOrder)
	{
		this.completedOrder = completedOrder;
	}

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}
	
	
}
