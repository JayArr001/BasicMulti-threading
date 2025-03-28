package Challenge_331_SynchronizationChallenge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

record Order(int orderID, String orderType, int orderQuantity)
{
	public Order(Order o)
	{
		this(o.orderID, o.orderType, o.orderQuantity);
	}
}

class Warehouse
{
	public static final List<String> productList = new ArrayList<>(List.of("hiking", "sneakers", "running"));
	private static List<Order> orderList;
	private static final int CAPACITY = 3;

	public Warehouse()
	{
		orderList = new LinkedList<Order>();
	}

	public synchronized void receiveOrder(Order newOrder) //called by producer
	{
		while(orderList.size() >= CAPACITY) //while the warehouse is at capacity for orders
		{
			try
			{
				System.out.println(Thread.currentThread().getName() + " is at capacity, waiting");
				wait();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		notifyAll();
		if(orderList.size() < CAPACITY)
		{
			orderList.add(newOrder);
			System.out.println("Producer added " + newOrder + " - size: " + orderList.size());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		else
		{
			System.out.println("orderList is full");
		}
	}

	public synchronized void fulfillOrder() //called by consumer
	{
		while(orderList.size() < 1) //list is empty and we are waiting for new orders
		{
			try
			{
				System.out.println(Thread.currentThread().getName() + " sees no orders, waiting");
				wait();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		notifyAll();
		//do stuff like pop the first element since we need to go in FIFO order
		if(orderList.size() > 0)
		{
			Order o = new Order(orderList.remove(0)); //remove is pop();
			System.out.println(Thread.currentThread().getName() + " starting " + o);
			int sleepTime = 200 + (o.orderQuantity() * 30);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println(Thread.currentThread().getName() + " fulfilled orderID " + o.orderID() + " in " + sleepTime + "ms");
		}
		else
		{
			System.out.println("orderList is empty");
		}
	}
}

public class aMain
{
	private static int orderID = 0;
	private static final int orderCount = 10;//number of orders, given from assignment
	private static final int orderPerThread = 5;//number of orders to fulfill per thread, given from assignment
	public static void main(String[] args)
	{
		Warehouse shoehouse = new Warehouse();

		Runnable producerRunnable = () ->
		{
			List<Order> orderGens = Stream.generate(aMain::generateRandomOrder)
				.limit(orderCount)
				.toList();
			for(Order o : orderGens)
			{
				shoehouse.receiveOrder(o);
			}
		};

		Runnable consumerRunnable = () ->
		{
			for(int i = 0; i < orderPerThread; i++)
			{
				shoehouse.fulfillOrder();
			}
		};

		Thread producerThread = new Thread(producerRunnable);
		Thread consumerThread1 = new Thread(consumerRunnable);
		Thread consumerThread2 = new Thread(consumerRunnable);

		producerThread.setName("producer");
		consumerThread1.setName("consumer1");
		consumerThread2.setName("consumer2");
		producerThread.start();
		consumerThread1.start();
		consumerThread2.start();
	}

	public static Order generateRandomOrder()
	{
		orderID++;
		Random random = new Random();
		int randomShoeType = random.nextInt(0, Warehouse.productList.size());
		String randomShoe = Warehouse.productList.get(randomShoeType);
		int newQty = random.nextInt(0, 100);
		return new Order(orderID, randomShoe, newQty);
	}
}
