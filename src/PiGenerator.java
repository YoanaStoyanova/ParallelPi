import java.util.concurrent.Callable;

import org.apfloat.Apfloat;
import org.apfloat.Apint;

public class PiGenerator implements Callable<Apfloat> {

	public static final int PRECISION = 10000;
	private static int THREAD_CNT;
	private static Apfloat[] partialSums;
	private int from;
	private int to;

	public static void setThreadCnt(int threadCnt) {
		THREAD_CNT = threadCnt;
		partialSums = new Apfloat[threadCnt];
		for (int i = 0; i < threadCnt; ++i) {
			partialSums[i] = new Apfloat(0, PRECISION);
		}

	}

	public static Apfloat getSum() {
		Apfloat sum = new Apfloat(0, PRECISION);
		for (int i = 0; i < THREAD_CNT; ++i) {
			sum = sum.add(partialSums[i]);
		}
		return sum;
	}

	PiGenerator(int from, int to) {
		this.from = from;
		this.to = to;
	}

	static Apfloat factorialApfloat(int n) {
		Apfloat x = new Apfloat(n, 1000);
		Apfloat res = new Apfloat(1, 1000);
		if (x.equals(0)) {
			return res;
		}

		for (int i = 2; i <= x.intValue(); ++i) {
			res = res.multiply(new Apint(i));

		}
		return res;
	}

	static Apfloat pow(Apfloat x, int power) {
		Apfloat res = new Apfloat(1, 1000);
		if (x.intValue() == 0)
			return res;
		for (int i = 0; i < power; ++i) {
			res = res.multiply(x);
		}
		return res;
	}

	@Override
	public Apfloat call() throws Exception {

		long lStartTime = 0;
		if (!MainClass.QUIET) {
			lStartTime = System.nanoTime();
			System.out.println("Thread " + Thread.currentThread().getId() + " started task.");
		}

		Apfloat partialPi = new Apfloat(0, PRECISION);

		for (int i = from; i < to; ++i) {
			Apfloat part1 = factorialApfloat(4 * i).divide(pow(factorialApfloat(i), 4));
			Apfloat part2 = pow(new Apfloat(396), 4 * i);
			part2.precision(PRECISION);
			Apfloat part3 = new Apfloat((1103 + 26390 * i), PRECISION);
			Apfloat TwoAndThree = part3.divide(part2);
			Apfloat temp = part1.multiply(TwoAndThree);
			partialPi = partialPi.add(temp);
		}

		// end
		if (!MainClass.QUIET) {
			long lEndTime = System.nanoTime();
			long output = lEndTime - lStartTime;
			System.out.printf("Thread %d finished task. Time in secs: %.4f \n", Thread.currentThread().getId(),
					(double) output / 1_000_000_000);
		}

		return partialPi;
		// System.out.println("Thread " + Thread.currentThread().getId() + " start is: "
		// + from + " end: " + to);

	}

}
