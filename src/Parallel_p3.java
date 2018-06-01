import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Cracker implements Runnable {
    public static final byte MIN_CHAR_VALUE = 'a';
    public static final byte MAX_CHAR_VALUE = 'z';

    private final int maxLength;
    private final Result result;
    private final byte minFirstChar;
    private final byte maxFirstChar;
    private final byte[] expectedHash;
    private final MessageDigest md;
    private byte[] buffer;


    /**
     * Each cracker have limited range for first letter of the world.
     * if we have 2 thread then:
     * thread 1 will work with words started from a..m
     * thread 2 will work with words started from n..z
     */
    public Cracker(int chunkNumber, int numberOfChunk, int maxLength, Result result, String expectedHash) {
        this.maxLength = maxLength;
        this.result = result;
        this.expectedHash = toArray(expectedHash);
        int range = MAX_CHAR_VALUE - MIN_CHAR_VALUE + 1;
        int chunkSize = range / numberOfChunk;
        this.minFirstChar = (byte) (MIN_CHAR_VALUE + chunkSize * chunkNumber);
        if (chunkNumber == numberOfChunk - 1) {
            this.maxFirstChar = MAX_CHAR_VALUE;
        } else {
            this.maxFirstChar = (byte) (MIN_CHAR_VALUE + chunkSize * (chunkNumber + 1) - 1);
        }

      System.out.println("Start tread for : " + (char)minFirstChar + " .. " + (char)maxFirstChar);

        buffer = new byte[1];
        initBuffer();

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toArray(String hash) {
        byte[] bytes = new byte[hash.length() / 2];
        for(int i = 0; i< bytes.length; i++){
            bytes[i] = (byte) Integer.parseInt(hash.substring(i*2,i*2 +2), 16);
        }
        return bytes;
    }

    private void initBuffer() {
        buffer[0] = minFirstChar;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i] = MIN_CHAR_VALUE;
        }
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            if (count % 100 == 0 && result.hashFound()) {
                result.addCount(count);
                break;
            }
            byte[] hash = calculateHash();
            count++;
            if (compareHash(hash, expectedHash)) {
                result.addCount(count);
                result.found(new String(buffer));
                break;
            }
            if (increment()) {
                break;
            }
        }
    }

    private boolean compareHash(byte[] hash, byte[] expectedHash) {
        return Arrays.equals(hash, expectedHash);
    }

    private boolean increment() {
        buffer[0]++;
        if (buffer[0] <= maxFirstChar) {
            return false;
        }
        buffer[0] = minFirstChar;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i]++;
            if (buffer[i] <= MAX_CHAR_VALUE) {
                return false;
            }
            buffer[i] = MIN_CHAR_VALUE;
        }
        if (buffer.length < maxLength) {
            buffer = new byte[buffer.length + 1];
            initBuffer();
            return false;
        }
        return true;
    }

    private byte[] calculateHash() {
        md.reset();
        md.update(buffer);
        return md.digest();
    }

    public static String hashToString(byte[] hash) {
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
    private static final int NUMBER_OF_THREADS = 4;


    public Result crack(String hash, int length) {
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        Result result = new Result();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Cracker(i, NUMBER_OF_THREADS, length, result, hash));
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

        Parallel_p3 sc = new Parallel_p3();

        long start = System.nanoTime();
        Result result = sc.crack(hashWord, wordSize);
        long end = System.nanoTime();
        System.out.println("The word is: " + result.getWord());
        System.out.println("Number of words tested: " + result.getCount());
        System.out.println("Processing Time: " + ((end - start) / 1000000) + " Milliseconds");
    }

}
//1F40FC92DA241694750979EE6CF582F2D5D7D28E18335DE05ABC54D0560E0F5302860C652BF08D560252AA5E74210546F369FBBBCE8C12CFC7957B2652FE9A75