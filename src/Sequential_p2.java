import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Sequential_p2 {
    private  int maxLength;
    private  byte[] expectedHash;
    public static final byte MIN_CHAR_VALUE = 'a';
    public static final byte MAX_CHAR_VALUE = 'z';
    private  MessageDigest md;
    private byte[] buffer;
    public int counter=0;
    public boolean found;
    public String guessWord;



    public Sequential_p2() {

    }

    public String crack(String hash, int size){
        this.maxLength=size;
        this.expectedHash=toArray(hash);

        buffer=new byte[1];
        initBuffer();

        try {
            md=MessageDigest.getInstance("SHA-512");

            while(true){
                if (found)
                    break;
                byte[] hashCalc = calculateHash();
                counter++;
                guessWord = new String(buffer);
                if(compareHash(hashCalc,expectedHash)){
                    found=true;
                    ;
                }
                if(increment()){
                    break;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (found) {
            return (guessWord + ":" + counter);
        } else {
            return ("Word not found" + ":" + counter);
        }
   }


    public static String hashToString(byte[] hash) {
        StringBuilder hex = new StringBuilder(64 * 2);
        for (int i = 0; i < hash.length; i++) {
            hex.append(Integer.toString((hash[i] & 0xf0) >> 4, 16));
            hex.append(Integer.toString(hash[i] & 0x0f, 16));
        }
        return hex.toString().toUpperCase();
    }

    private byte[] calculateHash() {
        md.reset();
        md.update(buffer);
        return md.digest();
    }

    private boolean increment() {
        buffer[0]++;
        if (buffer[0] <= MAX_CHAR_VALUE) {
            return false;
        }
        buffer[0] = MIN_CHAR_VALUE;
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

    private boolean compareHash(byte[] hash, byte[] expectedHash) {
        return Arrays.equals(hash, expectedHash);
    }

    private byte[] toArray(String hash) {
        byte[] bytes = new byte[hash.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hash.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private void initBuffer() {
        buffer[0] = MIN_CHAR_VALUE;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i] = MIN_CHAR_VALUE;
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce your hash code:");
        String hashWord = scanner.next();
        System.out.println("what is the maximum length of the word to be found? :");
        int wordSize = scanner.nextInt();
        scanner.close();

        try {
            Sequential_p2 sc = new Sequential_p2();

            String answer;
            long start = System.nanoTime();
            answer = sc.crack(hashWord, wordSize);
            long end = System.nanoTime();
            String[] wordAnswer = answer.split(":");
            System.out.println("The word is: " + wordAnswer[0]);
            System.out.println("Number of words tested: " + wordAnswer[1]);
            System.out.println("Processing Time: " + ((end - start) / 1e9) + " Seconds");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}