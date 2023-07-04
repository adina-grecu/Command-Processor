import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Boss implements Runnable{
        private FileChannel ordersChannel;
        private long start;
        private int size;
        private int id;

        private int P;

        private String productsPath;
        private ExecutorService lvl2executor;
        AtomicInteger inQueue;

        public Boss(long start, int size, FileChannel ordersChannel, int id, int P, String productsPath, ExecutorService lvl2executor, AtomicInteger inQueue) {
            this.start = start;
            this.size = size;
            this.ordersChannel = ordersChannel;
            this.id = id;
            this.P = P;
            this.productsPath = productsPath;
            this.lvl2executor = lvl2executor;
            this.inQueue = inQueue;
        }

        @Override
        public void run() {
            try {
                //check if thread will read from start of new line
                if(start != 0) {
                    ByteBuffer checkStartBuffer = ByteBuffer.allocate(2);
                    ordersChannel.read(checkStartBuffer, start);
                    String checkStart = new String(checkStartBuffer.array());

                    //if not, skip until new line
                    while(!checkStart.equals("o_")) {
                        start++;
                        size --;
                        checkStartBuffer.clear();
                        ordersChannel.read(checkStartBuffer, start);
                        checkStart = new String(checkStartBuffer.array());
                    }
                }

                //check if thread reads until end of line
                if( id != P) {
                    ByteBuffer checkEndBuffer = ByteBuffer.allocate(2);
                    ordersChannel.read(checkEndBuffer, start + size);
                    String checkEnd = new String(checkEndBuffer.array());

                    //if not, skip until new line
                    while (!checkEnd.equals("o_")) {
                        size++;
                        checkEndBuffer.clear();
                        ordersChannel.read(checkEndBuffer, start + size);
                        checkEnd = new String(checkEndBuffer.array());
                    }
                }

                //read from file with buffer size updated
                ByteBuffer buffer = ByteBuffer.allocate(size);
                ordersChannel.read(buffer, start);
                String content = new String(buffer.array());

                //divide content by lines
                String[] lines = content.split("o_");
                lines = Arrays.copyOfRange(lines, 1, lines.length);

                Path orders_out = Paths.get("orders_out.txt");

                //start a level 2 thread for every order
                for(int j = 0; j < lines.length; j++) {
                    String line = lines[j].trim();

                    if(!line.endsWith(",0")) {
                        inQueue.incrementAndGet();
                        Future<?> f = lvl2executor.submit(new Servant(lines[j].trim(), productsPath, inQueue));
                        f.get();

                        //after all products from order are shipped, mark order as shipped
                        String orderShippedName= "o_" + lines[j].trim() + ",shipped\n";
                        Files.write(orders_out, orderShippedName.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                    }
                }

                //check if there are still level 2 threads running
                int left = inQueue.get();
                if(left == 0) {
                    lvl2executor.shutdown();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
