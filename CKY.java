import java.io.*;
import java.util.*;
public class CKY{

    private String filename;
    private String word;
    private String startingSymbol;
    private ArrayList<String> terminals;
    private ArrayList<String> nonTerminals;
    private TreeMap<String,ArrayList<String>> grammar;

    public CKY(String filename, String word){
        this.terminals = new ArrayList<>();
        this.nonTerminals = new ArrayList<>();
        this.grammar = new TreeMap<>();
        this.word = word;
        this.filename = filename;
    }

    public void process(){
        this.parseGrammar();
        String[][] ckyTable = this.createCKYTable();
        this.printResult(this.generateCKY(ckyTable));
    }

    public void parseGrammar(){
        Scanner input = this.openFile();
        ArrayList<String> tmp = new ArrayList<>();
        int line = 2;

        startingSymbol = input.next();
        input.nextLine();

        while(input.hasNextLine() && line <= 3){
            tmp.addAll(Arrays.<String>asList(this.toArray(input.nextLine())));
            if(line == 2) { this.terminals.addAll(tmp); }
            if(line == 3) { this.nonTerminals.addAll(tmp); }
            tmp.clear();
            line++;
        }

        while(input.hasNextLine()){
            tmp.addAll(Arrays.<String>asList(this.toArray(input.nextLine())));
            String leftSide = tmp.get(0);
            tmp.remove(0);
            this.grammar.put(leftSide, new ArrayList<>());
            this.grammar.get(leftSide).addAll(tmp);
            tmp.clear();
        }
        input.close();
    }

    public String[][] createCKYTable (){
        int length = word.length();
        String[][] ckyTable = new String[length + 1][];
        ckyTable[0] = new String[length];
        for(int i = 1; i < ckyTable.length; i++){
            ckyTable[i] = new String[length - (i - 1)];
        }
        for(int i = 1; i < ckyTable.length; i++){
            for(int j = 0; j < ckyTable[i].length; j++){
                ckyTable[i][j] = "";
            }
        }
        return ckyTable;
    }

    public String[][] generateCKY(String[][] ckyTable){
        //Paso 1: llenamos la tabla
        for(int i = 0; i < ckyTable[0].length; i++){
            ckyTable[0][i] = this.manageWord(word, i);
        }
        //Paso 2: obtenemos las producciones para los terminales
        for(int i = 0; i < ckyTable[1].length; i++){
            String[] validCombinations = this.checkIfProduces(new String[] {ckyTable[0][i]});
            ckyTable[1][i] = this.toString(validCombinations);
        }
        if(word.length() <= 1) { return ckyTable; }
        //Paso 3: Obtenemos las producciones para las subcadenas de longitud 2
        for(int i = 0; i < ckyTable[2].length; i++){
            String[] downwards = this.toArray(ckyTable[1][i]);
            String[] diagonal = this.toArray(ckyTable[1][i+1]);
            String[] validCombinations = checkIfProduces(this.getAllCombinations(downwards, diagonal));
            ckyTable[2][i] = this.toString(validCombinations);
        }
        if(word.length() <= 2){ return ckyTable; }
        //Paso 3: Obtenemos las producciones para las subcadenas de longitud n
        TreeSet<String> currentValues = new TreeSet<String>();

        for(int i = 3; i < ckyTable.length; i++){
            for(int j = 0; j < ckyTable[i].length; j++){
                for(int compareFrom = 1; compareFrom < i; compareFrom++){
                    String[] downwards = ckyTable[compareFrom][j].split("\\s");
                    String[] diagonal = ckyTable[i-compareFrom][j+compareFrom].split("\\s");
                    String[] combinations = this.getAllCombinations(downwards, diagonal);
                    String[] validCombinations = this.checkIfProduces(combinations);
                    if(ckyTable[i][j].isEmpty()){
                        ckyTable[i][j] = this.toString(validCombinations);
                    }else{
                        String[] oldValues = this.toArray(ckyTable[i][j]);
                        ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(oldValues));
                        newValues.addAll(Arrays.asList(validCombinations));
                        currentValues.addAll(newValues);
                        ckyTable[i][j] = this.toString(currentValues.toArray(new String[currentValues.size()]));
                    }
                }
                currentValues.clear();
            }
        }
        return ckyTable;
    }
    
    public String manageWord(String word, int position){
        return Character.toString(word.charAt(position));
    }

    public String[] checkIfProduces(String[] toCheck){
        ArrayList<String> storage = new ArrayList<>();
        for(String s : this.grammar.keySet()){
            for(String current : toCheck){
                if(this.grammar.get(s).contains(current)){
                    storage.add(s);
                }
            }
        }
        if(storage.isEmpty()){ 
            return new String[] {}; 
        }
        return storage.toArray(new String[storage.size()]);
    }

    public String[] getAllCombinations(String[] from, String[] to){
        int length = from.length * to.length;
        int counter = 0;
        String[] combinations = new String[length];
        if(length == 0){ return combinations; };
        for(int i = 0; i < from.length; i++){
            for(int j = 0; j < to.length; j++){
                combinations[counter] = from[i] + to[j];
                counter++;
            }
        }
        return combinations;
    }

    public void printResult (String[][] ckyTable){
        System.out.println("Word: " + this.word);
        System.out.println("\nG = (" + this.terminals.toString().replace("[", "{").replace("]", "}") 
                          + ", " + this.nonTerminals.toString().replace("[", "{").replace("]", "}")
                          + ", " + this.startingSymbol + ")\n\nWith Productions P as:");
        for(String s: grammar.keySet()){
            System.out.println(s + " -> " + this.grammar.get(s).toString().replaceAll("[\\[\\]\\,]", "").replaceAll("\\s", " | "));
        }
        System.out.println("\nApplying CKY-Algorithm:\n");
        this.drawTable(ckyTable);
    }

    public void drawTable(String[][] ckyTable){
        int l = this.findLongestString(ckyTable) + 2;
        String formatString = "| %-" + l + "s ";
        String s = "";
        StringBuilder sb = new StringBuilder();
        //Construimos la tabla
        sb.append("+");
        for(int x = 0; x <= l + 2; x++){
            if(x == l + 2){ 
                sb.append("+");
            }else{
                sb.append("-");
            }
        }
        String low = sb.toString();
        sb.delete(0, 1);
        String lowRight = sb.toString();
        //Imprimimos la tabla
        for(int i = 0; i < ckyTable.length; i++){
            for(int j = 0; j <= ckyTable[i].length; j++){
                System.out.print((j == 0) ? low : (i <= 1 && j == ckyTable[i].length - 1) ? "" : lowRight);
            }
            System.out.println();
            for(int j = 0; j < ckyTable[i].length; j++){
                s = (ckyTable[i][j].isEmpty()) ? "-" : ckyTable[i][j];
                System.out.format(formatString, s.replaceAll("\\s", ","));
                if(j == ckyTable[i].length - 1) { System.out.print("|"); }
            }
            System.out.println();
        }
        System.out.println(low+"\n");
        //Paso 4: Revisamos si está el símbolo inicial,para evaluar si la cadena puede ser generada por la gramática
        if(ckyTable[ckyTable.length-1][ckyTable[ckyTable.length-1].length-1].contains(this.startingSymbol)){
            System.out.println("The word \"" + this.word + "\" is an element of the CFG G and can be derived from it.");
        }else{
            System.out.println("The word \"" + this.word + "\" is not an element of the CFG G and can not be derived from it.");
        }
    }

    public int findLongestString(String[][] ckyTable){
        int x = 0;
        for(String[] s : ckyTable){
            for(String d : s){
                if(d.length() > x){ x = d.length(); }
            }
        }
        return x;
    }

    public Scanner openFile(){
        try{
            return new Scanner(new File(this.filename));
        }catch(FileNotFoundException e){
            System.out.println("Error: Can't find or open the file: " + this.filename + ".");
            System.exit(1);
            return null;
        }
    }

    public String[] toArray(String input){
        return input.split("\\s");
    }

    public String toString(String[] input){
        return Arrays.toString(input).replaceAll("[\\[\\]\\,]", "");
    }
}