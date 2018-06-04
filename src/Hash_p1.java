import java.nio.charset.StandardCharsets;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;

public class Hash_p1 {

    public static String stringHexHash( String texto) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] hash = digest.digest( texto.getBytes( StandardCharsets.ISO_8859_1));
        return javax.xml.bind.DatatypeConverter.printHexBinary(hash);
    };

    public static void lineSep(){
        System.out.println("---------------------------------------------------------------" +
                "------------------------------------------------------------------------");
    }

    public static void main(String[] args) throws InterruptedException, NoSuchAlgorithmException {
        String[] palavra= {"zoom","zigzag","zirconium"};

        for(int i=0;i<palavra.length;i++) {
            String hash=stringHexHash(palavra[i]);
            lineSep();
            System.out.println("palavra: "+palavra[i]+"\n"+"Hash: "+hash+"\t");
        };
        lineSep();
    }
}
