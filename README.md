Parallel Command Processor

The project consists of three classes: a main class with the main method called CommandProcessor, and a class for each level of threads. Level 1 threads are named Boss, and level 2 threads are named Servant.

In the main method, the output files are cleared to ensure correct generation during program execution. The command file is read in bytes, which is explained in the README_BONUS file. Two thread pools are created, one for each type of thread, with a maximum of P threads. P level 1 Boss threads are started.

Each level 1 thread receives a start position, a read portion size, the command file, an ID, the total number of threads, the path to the product file, an executor for level 2 threads, and an atomic variable to check if the execution of level 2 threads has finished.

Before reading the commands and starting the level 2 threads, the portion size is updated so that each thread reads complete lines. A buffer is created to check if the assigned portion starts with "o_". If not, the start position and portion size are updated until a line starting with "o_" is found. Then, a similar check is done for the end of the portion. The content immediately following the assigned portion is read, and if it is not "o_", the portion size is updated to end at the end of a line. Finally, the content of the portion is read with the updated size and divided into lines.

For each line, if there are products in the command, a level 2 Servant thread is created to process the command. The current level 1 Boss thread waits for the products in the command to be delivered (using the future.get function), and then marks the command as delivered in the orders_out file.

Each level 2 thread receives a command, the path to the product file, and an atomic variable that is decremented when the execution is finished.

First, the command is split into ID-command and the number of products in the command. While there are still products in the command, each line is read from the file. If a product belonging to the thread's command is found, it is marked as delivered in the order_products_out file. After delivering all the products in the command, the level 2 thread terminates and decrements the atomic variable.