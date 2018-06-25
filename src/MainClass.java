import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApfloatRuntimeException;

public class MainClass {

	static int THREAD_CNT = 16;
	static int upTo = 2451;
	static int CHUNK_SIZE = 10;
	static String outputFile = "./result.txt";
	static boolean QUIET = false;

	public static void setArguments(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			System.out.println("Arg " + i + " is " + args[i]);
			switch (args[i]) {
			case "-p":
				upTo = Integer.parseInt(args[++i]);
				break;
			case "-t":
				THREAD_CNT = Integer.parseInt(args[++i]);
				break;
			case "-o":
				outputFile = args[++i];
				break;
			case "-q":
				QUIET = true;
				break;
			default: // Invalid argument
				break;
			}
		}

		System.out.println("Thread count: " + THREAD_CNT);
		System.out.println("upTo: " + upTo);
		System.out.println("outputFile: " + outputFile);
		System.out.println("quiet: " + QUIET);
	}

	public static void main(String[] args)
			throws ApfloatRuntimeException, InterruptedException, ExecutionException, IOException {

		long execStart = System.nanoTime();
		setArguments(args);
		ExecutorService pool = Executors.newFixedThreadPool(THREAD_CNT);
		List<Future<Apfloat>> list = new ArrayList<Future<Apfloat>>();

		PiGenerator.setThreadCnt(THREAD_CNT);
		
		int i;
		for (  i = CHUNK_SIZE; i < upTo; i += CHUNK_SIZE) {
			Future<Apfloat> future = pool.submit(new PiGenerator(i - CHUNK_SIZE, i ));
			list.add(future);
		}
		if(i > upTo) {
			list.add(pool.submit(new PiGenerator(i - CHUNK_SIZE, upTo)));
		}
		
		pool.shutdown();

		Apfloat sum = new Apfloat(0, PiGenerator.PRECISION);
		for (Future<Apfloat> fut : list) {
			sum = sum.add(fut.get());
		}

		Apfloat sqrt2 = ApfloatMath.sqrt(new Apfloat(2, 1000));
		Apfloat sqrt8 = (sqrt2.divide(new Apfloat(9801, 1000)).multiply(new Apfloat(2, 1000)));
		Apfloat oneOverPi = sum.multiply(sqrt8);
		Apfloat PiReal = new Apfloat(1, 1000).divide(oneOverPi);
		System.out.println(PiReal.toString(true));

		try (FileWriter fos = new FileWriter(new File(outputFile))) {
			fos.write(PiReal.toString());
			// fos.close(); There is no more need for this line since you had created the
			// instance of "fos" inside the try. And this will automatically close the
			// OutputStream
		}
		long execEnd = System.nanoTime();
		long totalTime = execEnd - execStart;
		if (!QUIET)
			System.out.printf("Total execution time in secs: %.4f \n", (double) totalTime / 1_000_000_000);

	}

}
