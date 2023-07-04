import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Servant implements Runnable {

    private String order;
    private String productsPath;
    AtomicInteger inQueue;

    public Servant(String order, String productsPath, AtomicInteger inQueue) {
        this.order = order;
        this.productsPath = productsPath;
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        //split order name and number of products
        String[] orderSplit = order.split(",");
        String orderId = "o_" + orderSplit[0];
        int productsNumber = Integer.parseInt(orderSplit[1]);

        File productsFile = new File(productsPath);
        Path order_products_out = Paths.get("order_products_out.txt");

        try {
            //read products file
            Scanner sc = new Scanner(productsFile);

            while (sc.hasNextLine() && productsNumber > 0) {
                String product = sc.nextLine();
                String[] productSplit = product.split(",");
                String currentOrder = productSplit[0];

                //if current product is from assigned order, ship it
                if(currentOrder.equals(orderId)) {
                    productsNumber --;
                    String productShipped = product + ",shipped\n";
                    Files.write(order_products_out, productShipped.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        inQueue.decrementAndGet();
    }
}
