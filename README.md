# BasicMulti-threading
Simple application to demonstrate multi-threading use in Java</br>
This application is made to demonstrate proficiency and ability to deploy a multi-threaded solution. Specifically, this is a producer-consumer relationship.</br>
</br>
The intent is to simulate a relationship between a "producer", which might be a storefront that produces orders and a "consumer", the warehouse; which will "take in" the orders (consume) to fulfill them.</br>
</br>
Producer code generates orders and sends them to the consumer in first-in-first-out order Again, this is deployed in asynchronous fashion. The producer and consumer are working in non-linear time. While the performance gain is negative or negligible given the small amount of data in this set, given hundreds of thousands to millions of operations there would be a significant gain in performance.</br>
</br>
This is a self-contained, one-file application.
