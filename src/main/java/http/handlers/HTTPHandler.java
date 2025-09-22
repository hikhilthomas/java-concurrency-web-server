package http.handlers;

import java.util.stream.IntStream;

public interface HTTPHandler<T> {
    int LIMIT = 100_000;

    void handle(T request);

    default int computeTask() {
        return (int) countPrimes(LIMIT);
    }

    private static long countPrimes(int limit) {
        return IntStream.range(0, limit)
            .filter(HTTPHandler::isPrime)
            .count();
    }

    private static boolean isPrime(int num) {
        for (int i = 2; i * i <= num; ++i) {
            if (num % i == 0) return false;
        }
        return true;
    }
}
