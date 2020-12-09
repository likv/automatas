public class Main {
    public static void main(String[] pps) {
        String filename = pps[0];
        String word = pps[1];
        CKY alg = new CKY(filename, word);
        alg.process();
    }
}
