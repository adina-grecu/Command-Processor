import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.toIntExact;

public class CommandProcessor {
    public static void main(String[] args) throws Exception {
        //number of threads
        int P = Integer.parseInt(args[1]);
        long start = 0;
        AtomicInteger inQueue = new AtomicInteger(0);

        //delete out files before start of program
        File orders_out = new File("orders_out.txt");
        File order_products_out = new File("order_products_out.txt");
        orders_out.delete();
        order_products_out.delete();

        //orders folder
        File folder = new File(args[0]);
        File[] files = folder.listFiles();

        //orders and products files
        FileInputStream orderFile = new FileInputStream(files[1]);
        String productsPath = files[0].getPath();
        FileChannel ordersChannel = orderFile.getChannel();

        //divide filesize equally to P threads
        long fileSize = ordersChannel.size();
        long chunkSize = fileSize / P;

        //start thread pools for P threads of levels 1 & 2
        ExecutorService lvl1executor = Executors.newFixedThreadPool(P);
        ExecutorService lvl2executor = Executors.newFixedThreadPool(P);

        //start P threads of level 1
        for (int i = 0; i < P - 1; i++) {
            lvl1executor.submit(new Boss(start, toIntExact(chunkSize), ordersChannel, i + 1, P, productsPath, lvl2executor, inQueue));
            fileSize -= chunkSize;
            start += chunkSize;
        }

        lvl1executor.submit(new Boss(start, toIntExact(fileSize), ordersChannel, P, P, productsPath, lvl2executor, inQueue));

        lvl1executor.shutdown();
    }
}
