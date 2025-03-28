import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/*
* This application is made to demonstrate proficiency and ability to deploy a multi-threaded solution.
* Specifically, this is a producer-consumer relationship.
*
* The intent is to simulate a relationship between a "producer", which might be a storefront that produces orders,
* and a "consumer", the warehouse; which will "take in" the orders (consume) to fulfill them.
*
* Producer code generates orders and sends them to the consumer in first-in-first-out order.
* Again, this is deployed in asynchronous fashion. The producer and consumer are working in non-linear time.
* While the performance gain is negative or negligible given the small amount of data in this set,
* given hundreds of thousands to millions of operations, there would be a significant gain in performance.
* */

//pojo to handle order details
record Order(int orderID, String orderType, int orderQuantity)
{
	public Order(Order o)
	{
		this(o.orderID, o.orderType, o.orderQuantity);
	}
}

//consumer class
class Warehouse
{
	//for example-sake there are only 3 types of shoes to pick from
	public static final List<String> productList = new ArrayList<>(List.of("hiking", "sneakers", "running"));
	private static List<Order> orderList;

	//arbitrary number to simulate the warehouse being at capacity
	//ie can't take any more orders until some are filled
	private static final int CAPACITY = 3;

	public Warehouse()
	{
		//this must be a linked list, or some collection that helps track insertion order
		//since a requirement is FIFO
		orderList = new LinkedList<Order>();
	}

	//called by producer
	public synchronized void receiveOrder(Order newOrder)
	{
		//while the warehouse is at capacity for orders
		while(orderList.size() >= CAPACITY)
		{
			try
			{
				//respond that the warehouse is at capacity
				System.out.println(Thread.currentThread().getName() + " is at capacity, waiting");
				wait();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		//at this point in the code, warehouse has capacity to fulfill an order, wake up all waiting threads
		notifyAll();
		if(orderList.size() < CAPACITY)
		{
			orderList.add(newOrder);
			System.out.println("Producer added " + newOrder + " - size: " + orderList.size());
			try {
				Thread.sleep(200); //simulation of time it takes to fulfill an order
				//in reality we might be doing some calculations here, or waiting on data from another source
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		else
		{
			System.out.println("orderList is full");
		}
	}

	//called by consumer
	public synchronized void fulfillOrder()
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
			Order o = new Order(orderList.remove(0));
			System.out.println(Thread.currentThread().getName() + " starting " + o);
			int sleepTime = 200 + (o.orderQuantity() * 30); //simulation of a real world application needing time
			//for calculations, awaiting data or something else
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
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

public class Main
{
	private static int orderID = 0; //order incrementer
	private static final int orderCount = 10;//number of orders, arbitrarily chosen but can be any non-zero
	private static final int orderPerThread = 5;//number of orders to fulfill per thread, arbitrarily chosen
	public static void main(String[] args)
	{
		Warehouse shoehouse = new Warehouse();

		Runnable producerRunnable = () ->
		{
			//Streams are lazy and have no guarantee to generate in a certain order
			//however we are just generating randomly and at this point, order does not yet matter
			List<Order> orderGens = Stream.generate(Main::generateRandomOrder)
				.limit(orderCount)
				.toList();
			for(Order o : orderGens)
			{
				//pass the order to the consumer
				shoehouse.receiveOrder(o);
			}
		};

		Runnable consumerRunnable = () ->
		{
			for(int i = 0; i < orderPerThread; i++)
			{
				//consumer will start fulfilling orders
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

	//helper method to easily generate orders
	//will pick a random shoe type and quantity between 0 and 100, not including 0
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
