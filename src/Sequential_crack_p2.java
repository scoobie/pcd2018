import java.security.MessageDigest;
import java.util.Scanner;

public class Sequential_crack_p2 {
    MessageDigest md;
    char[] guess;
    int max_num_chars;
    char min_char_value=97;    // starting  in a character
    char max_char_value=122;   // finishing in z character

    public Sequential_crack_p2() throws Exception
    {
        md = MessageDigest.getInstance("SHA-512");
        guess = null;
    }


    public String crack(String hash,int length) throws Exception {
        max_num_chars=length;
        boolean done = false;
        String guess_hash;
        int counter=0;

        for(int num_chars = 0; num_chars < max_num_chars && !done; num_chars++)
        {
            guess = new char[num_chars];
            for(int x = 0; x < num_chars; x++)
            {
                guess[x] = min_char_value;
            }

            while(canIncrementGuess() && !done)
            {
                incrementGuess();
                counter++;
                md.reset();
                md.update(new String(guess).getBytes());
                guess_hash = hashToString(md.digest());
                if(hash.equals(guess_hash))
                {
                    done = true;
                }
            }
        }
        String guessWord=new String(guess);
        return (guessWord+" "+counter);
    }

    protected boolean canIncrementGuess()
    {
        boolean canIncrement = false;

        for(int x=0; x < guess.length; x++)
        {
            if(guess[x] < max_char_value) canIncrement = true;
        }
        return canIncrement;
    }

    protected void incrementGuess()
    {
        boolean incremented = false;

        for(int x = (guess.length - 1);x >= 0 && !incremented; x--)
        {
           if(guess[x] < max_char_value)
            {
                guess[x]++;
                if(x < (guess.length-1))
                {
                    guess[x+1] = min_char_value;
                }
                incremented = true;
            }
        }
    }

    protected String hashToString(byte[] hash)
    {
        StringBuffer hex=new StringBuffer();
        for (int i=0;i<hash.length;i++)
            hex.append(Integer.toString((hash[i]&0xff)+0x100,16).substring(1));
        return hex.toString().toUpperCase();
    }

    public static void main(String[] args ){

        Scanner scanner= new Scanner(System.in);
        System.out.println("Introduzir HASH :");
        String hashWord=scanner.next();
        System.out.println("Qual é o comprimento máximo da palava a descobrir? :");
        int wordSize=scanner.nextByte();
        scanner.close();

        try {
            Sequential_crack_p2 sc= new Sequential_crack_p2();

            String answer;
            long start=System.nanoTime();
            answer=sc.crack(hashWord,wordSize);
            long end=System.nanoTime();
            String [] wordAnswer=answer.split(" ");
            System.out.println("The word is: "+ wordAnswer[0]);
            System.out.println("Number of words tested: "+wordAnswer[1]);
            System.out.println("Processing Time: "+((end-start)/1000000) +" Milliseconds");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
