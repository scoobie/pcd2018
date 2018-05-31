import java.security.MessageDigest;
import java.util.Scanner;

public class Sequential_crack_p2 {
    private MessageDigest md;
    private byte[] guess;
    private int max_num_chars;
    private byte min_char_value = 'a';
    private byte max_char_value = '{';

    public Sequential_crack_p2() throws Exception {
        md = MessageDigest.getInstance("SHA-512");
        guess = null;
    }


    public String crack(String hash, int length) {
        max_num_chars = length;
        boolean done = false;
        String guess_hash;
        int counter = 1;
        boolean found = false;

        for (int num_chars = 0; num_chars <= max_num_chars && !done; num_chars++) {
            guess = new byte[num_chars];
            for (int x = 0; x < num_chars; x++) {
                guess[x] = min_char_value;
            }

            while (canIncrementGuess() && !done) {
                counter++;
                incrementGuess();
                md.reset();
                md.update(guess);
                guess_hash = hashToString(md.digest());
                if (hash.equals(guess_hash)) {
                    done = true;
                    found = true;
                    break;
                }

            }
        }
        String guessWord = new String(guess);
        if (found) {
            return (guessWord + ":" + counter);
        } else {
            return ("Word not found" + ":" + counter);
        }

    }

    protected boolean canIncrementGuess() {
        boolean canIncrement = false;
        for (int x = 0; x < guess.length; x++) {
            if (guess[x] < max_char_value) canIncrement = true;
        }
        return canIncrement;
    }

    protected void incrementGuess() {
        boolean incremented = false;
        for (int x = (guess.length - 1); x >= 0 && !incremented; x--) {
            if (guess[x] < max_char_value) {
                guess[x]++;
                if (x < (guess.length - 1)) {
                    guess[x + 1] = min_char_value;
                }
                incremented = true;
            }
        }
    }

    private String hashToString(byte[] hash) {
        StringBuilder hex = new StringBuilder(64 * 2);
        for (int i = 0; i < hash.length; i++) {
            hex.append(Integer.toString((hash[i] & 0xf0) >> 4, 16));
            hex.append(Integer.toString(hash[i] & 0x0f, 16));
        }
        return hex.toString().toUpperCase();
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Introduce your hash code:");
        String hashWord = scanner.next();
        System.out.println("what is the maximum length of the word to be found? :");
        int wordSize = scanner.nextInt();
        scanner.close();

        try {
            Sequential_crack_p2 sc = new Sequential_crack_p2();

            String answer;
            long start = System.nanoTime();
            answer = sc.crack(hashWord, wordSize);
            long end = System.nanoTime();
            String[] wordAnswer = answer.split(":");
            System.out.println("The word is: " + wordAnswer[0]);
            System.out.println("Number of words tested: " + wordAnswer[1]);
            System.out.println("Processing Time: " + ((end - start) / 1000000) + " Milliseconds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}