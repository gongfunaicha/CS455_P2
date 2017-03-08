CS455 Homework 2 - Programming Component
SCALABLE SERVER DESIGN: USING THREAD POOLS TO MANAGE ACTIVE NETWORK CONNECTIONS

Compilation:
Please use "make" command to compile the whole project.
If you want to remove the .class files produced by the compilation, please use command "make clean".

Executing project:
Registry:
	java cs455.scaling.server.Server <portnum> <thread-pool-size>
Messaging Node:
	java cs455.scaling.client.Client <server-host> <server-port> <message-rate>

Files:
cs455/
	scaling/
		client/
			Client.java: Main class for client side. Performs command line check, connects to server, starts the statistics collector thread and sender thread, and uses NIO to perform reading.
		clientThread/
			ClientSenderThread.java: Constantly generate and send data over channel at given speed.
			ClientStatisticsCollector.java: Collects sending and receiving statistics and prints out statistics every 10 seconds.
		server/
			Server.java: Main class for server side. Performs command line check, binds to one local port, start statistics collector thread and threadpool manager thread, and uses NIO to deal with accept, read, and write.
		serverThread/
			ServerStatisticsCollector.java: Collects throughput and active connection statistics and prints out statistics every 5 seconds.
		task/
			Task.java: Task class, uses char task to differentiate read, hash, and write task.
		threadpool/
			ThreadpoolManager.java: Threadpool manager class, responsible for assigning tasks to standby threads.
			WorkerThread.java: Worker thread class, responsible for performing read, hash, and write task.
		util/
			queue/
				TaskQueue.java: Task queue class wraps a task queue linkedlist inside and ensures safe concurrent action on task queue.
				WorkerQueue.java: Worker queue class wraps a worker queue linkedlist inside and ensures safe concurrent action on worker queue.
			Attachment.java: Designed to be used as channel attachment in server, contains data buffer, digest buffer, and two flags - inUse and alreadyRewrite.
			DigestUtil.java: Returns SHA1 digest of the byte array input represented in 40 bytes hex string.
			HashStorage.java: Stores the unacknowledged hashes of the client side.
			TimeStamp.java: Utility class that prints the given message with timestamp.
			
			
Chen Wang
3/7/2017