import java.io.*;
import java.util.*;

public class BackEnd {
    static HashMap<Integer, String> int_to_literal = new HashMap<>();
    static Set<Integer> atoms = new HashSet<>(); //set of propositional atoms
    static HashMap<Integer, Boolean> Value_Map = new HashMap<>();

    public static void main(String[] args) throws Exception{
        if(args.length ==1){
            String filename = args[0];
            get_Value_Map_from(filename);
            generate_Path();
        }else{
            System.out.print("this program should accept exact one argument!\n");
        }
    }

    static void get_Value_Map_from(String filename) throws IOException {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if(!line.equals("0")){
                String temp[] = line.split(" ");
                if(temp[1].equals("True")){
                    Value_Map.put(Integer.parseInt(temp[0]), true);
                }
                if(temp[1].equals("False")){
                    Value_Map.put(Integer.parseInt(temp[0]), false);
                }
            }else{
                break;
            }
        }
        while ((line = br.readLine()) != null){
            String temp[] = line.split(" ");
            //System.out.print(temp[0]+" "+temp[1]+"\n");
            atoms.add(Integer.parseInt(temp[0]));
            int_to_literal.put(Integer.parseInt(temp[0]),temp[1]);
        }
    }

    static void generate_Path(){
        if(Value_Map.isEmpty()){
            System.out.print("No Solution!\n");
        }else {
            ArrayList<Jump> Jump_action = new ArrayList<>();
            ArrayList<Peg> Peg_list = new ArrayList<>();
            int Jump_time = 0;
            for (int k : Value_Map.keySet()) {
                String literal = int_to_literal.get(k);
                //System.out.println(k + " " + literal+ " " + Value_Map.get(k));
                if (Value_Map.get(k) && literal.contains("Jump")) {     // get Jump action which is true
                    //System.out.println(k + " " + literal+ " " + Value_Map.get(k));
                    literal = literal.replace("Jump(", "");
                    literal = literal.replace(")", "");
                    String temp[] = literal.split(",");
                    if (Integer.parseInt(temp[3]) > Jump_time) {
                        Jump_time = Integer.parseInt(temp[3]);
                    }
                    Jump j = new Jump(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]), Integer.parseInt(temp[3]), k);
                    Jump_action.add(j);
                }
                if (Value_Map.get(k) && literal.contains("Peg")) {      // get Peg which is true
                    //System.out.println(k + " " + literal+ " " + Value_Map.get(k));
                    literal = literal.replace("Peg(", "");
                    literal = literal.replace(")", "");
                    String temp[] = literal.split(",");
                    Peg p = new Peg(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), k);
                    Peg_list.add(p);
                }
            }
            for (int i = 1; i < Jump_time + 1; i++) {
                System.out.print("At time " + i + "\n\t");
                for (Peg p : Peg_list) {
                    if (p.I == i) {
                        System.out.print("Hole " + p.A + " ");
                    }
                }
                System.out.print("has peg\n");
                for (Jump j : Jump_action) {
                    if (j.I == i) {
                        System.out.print("\tJump from hole " + j.A + " across hole " + j.B + " to hole " + j.C + "\n");
                    }
                }
            }
            System.out.print("at time " + (Jump_time + 1) + "\n");
            for (Peg p : Peg_list) {
                if (p.I == Jump_time + 1) {
                    System.out.print("\tHole" + p.A + " has peg\n");
                }
            }
            System.out.print("Accomplished!\n");
        }

    }
}
