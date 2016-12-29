
//: concurrency/ExchangerDemo.java
/* Added by Eclipse.py */
import java.util.concurrent.*;
import java.util.List;
import net.mindview.util.*;

class ExchangerProducer<T> implements Runnable {
	private Generator<T> generator;
	private Exchanger<List<T>> exchanger;
	private List<T> holder;

	//constructor
	ExchangerProducer(Exchanger<List<T>> exchg, Generator<T> gen, List<T> holder) {
		exchanger = exchg;
		generator = gen;
		this.holder = holder;
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
				for (int i = 0; i < ExchangerDemo.size; i++){
					holder.add(generator.next());
				}		
					
				// Exchange full for empty:
				holder = exchanger.exchange(holder);
				//调用exchange之后，会等待consumer处理之后返回的holder
				System.out.println("holder size in producer: " +holder.size());
			}
		} catch (InterruptedException e) {
			// OK to terminate this way.
		}
	}
}

class ExchangerConsumer<T> implements Runnable {
	private Exchanger<List<T>> exchanger;
	private List<T> holder;
	private volatile T value;

	//constructor
	ExchangerConsumer(Exchanger<List<T>> ex, List<T> holder) {
		exchanger = ex;
		this.holder = holder;
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {			
				holder = exchanger.exchange(holder);//从producer传入consumer
				System.out.println("holder size in consumer: " +holder.size());
				for (T x : holder) {
					value = x; // Fetch out value
					System.out.println("current value: " + value);
					holder.remove(x); // OK for CopyOnWriteArrayList
				}
			}
		} catch (InterruptedException e) {
			// OK to terminate this way.
		}
		System.out.println("Final value: " + value);
	}
}

public class ExchangerDemo {
	static int size = 10;
	static int delay = 5; // Seconds

	public static void main(String[] args) throws Exception {
		if (args.length > 0)
			size = new Integer(args[0]);
		if (args.length > 1)
			delay = new Integer(args[1]);
		
		ExecutorService exec = Executors.newCachedThreadPool();
		Exchanger<List<Fat>> xc = new Exchanger<List<Fat>>();
		List<Fat> producerList = new CopyOnWriteArrayList<Fat>(), consumerList = new CopyOnWriteArrayList<Fat>();
		
		exec.execute(new ExchangerProducer<Fat>(xc, BasicGenerator.create(Fat.class), producerList));
		exec.execute(new ExchangerConsumer<Fat>(xc, consumerList));
		TimeUnit.SECONDS.sleep(delay);
		exec.shutdownNow();
	}
} /*
	 * Output: (Sample) Final value: Fat id: 29999
	 */// :~
