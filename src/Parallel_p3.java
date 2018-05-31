import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Cracker implements Runnable {
    private final int maxLength;
    private final Result result;
    private final byte minFirstChar;
    private final byte maxFirstChar;
    private final String expectedHash;
    private final MessageDigest md;
    private byte[] buffer;
    private int count = 0;


    /**
     * Each cracker have limited range for first letter of the world.
     * if we have 2 thread then:
     *  thread 1 will work with words started from a..m
     *  thread 2 will work with words started from n..z
     */
    public Cracker(int chunkNumber, int maxLength, Result result, String expectedHash) {
        this.maxLength = maxLength;
        this.result = result;
        this.expectedHash = expectedHash;
        int range = Parallel_p3.MAX_CHAR_VALUE - Parallel_p3.MIN_CHAR_VALUE + 1;
        int chunkSize = range / Parallel_p3.NUMBER_OF_THREADS;
        this.minFirstChar = (byte) (Parallel_p3.MIN_CHAR_VALUE + chunkSize * chunkNumber);
        if (chunkNumber == Parallel_p3.NUMBER_OF_THREADS - 1) {
            this.maxFirstChar = Parallel_p3.MAX_CHAR_VALUE;
        } else {
            this.maxFirstChar = (byte) (Parallel_p3.MIN_CHAR_VALUE + chunkSize * (chunkNumber + 1) - 1);
        }

//            System.out.println("Start tread for : " + (char)minFirstChar + " .. " + (char)maxFirstChar);

        buffer = new byte[1];
        initBuffer();

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void initBuffer() {
        buffer[0] = minFirstChar;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i] = Parallel_p3.MIN_CHAR_VALUE;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (result.hashFound()) {
                result.addCount(count);
                break;
            }
            String hash = calculateHash();
            count++;
            if (hash.equals(expectedHash)) {
                result.addCount(count);
                result.found(new String(buffer));
                break;
            }
            if (increment()) {
                break;
            }
        }
    }

    private boolean increment() {
        buffer[0]++;
        if (buffer[0] <= maxFirstChar) {
            return false;
        }
        buffer[0] = minFirstChar;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i]++;
            if (buffer[i] <= Parallel_p3.MAX_CHAR_VALUE) {
                return false;
            }
            buffer[i] = Parallel_p3.MIN_CHAR_VALUE;
        }
        if (buffer.length < maxLength) {
            buffer = new byte[buffer.length + 1];
            initBuffer();
            return false;
        }
        return true;
    }

    private String calculateHash() {
        md.reset();
        md.update(buffer);
        return hashToString(md.digest());
    }

    private String hashToString(byte[] hash) {
        StringBuilder hex = new StringBuilder(64 * 2);
        for (int i = 0; i < hash.length; i++) {
            hex.append(Integer.toString((hash[i] & 0xf0) >> 4, 16));
            hex.append(Integer.toString(hash[i] & 0x0f, 16));
        }
        return hex.toString().toUpperCase();
    }
}

class Result {
    private volatile String word = null;
    private int totalCount = 0;

    public void found(String word) {
        this.word = word;
    }

    public boolean hashFound() {
        return word != null;
    }

    public synchronized void addCount(int count) {
        totalCount += count;
    }

    public String getWord() {
        return word;
    }

    public synchronized int getCount() {
        return totalCount;
    }
}


public class Parallel_p3 {
    public static final int NUMBER_OF_THREADS = 8;

    public static final byte MIN_CHAR_VALUE = 'a';
    public static final byte MAX_CHAR_VALUE = 'z';


    public Result crack(String hash, int length) {
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        Result result = new Result();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Cracker(i, length, result, hash));
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce your hash code:");
        String hashWord = scanner.next();
        System.out.println("what is the maximum length of the word to be found? :");
        int wordSize = scanner.nextInt();
        scanner.close();

        try {
            Parallel_p3 sc = new Parallel_p3();

            Result answer;
            long start = System.nanoTime();
            answer = sc.crack(hashWord, wordSize);
            long end = System.nanoTime();
            System.out.println("The word is: " + answer.getWord());
            System.out.println("Number of words tested: " + answer.getCount());
            System.out.println("Processing Time: " + ((end - start) / 1000000) + " Milliseconds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
//1F40FC92DA241694750979EE6CF582F2D5D7D28E18335DE05ABC54D0560E0F5302860C652BF08D560252AA5E74210546F369FBBBCE8C12CFC7957B2652FE9A75