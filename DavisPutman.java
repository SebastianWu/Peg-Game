import java.io.*;
import java.util.*;

enum PropValue{TRUE, FALSE, UNBOUND}

public class DavisPutman {

    static HashMap<Integer, String> int_to_literal = new HashMap<>();   // hash map of index and literals
    static ArrayList<clause> clauses = new ArrayList<>(); // Set of propositional formulas in CNF
    static Set<Integer> atoms = new HashSet<>(); //set of propositional atoms
    static clause Nil = new clause();   // Empty clause \psi

    public static void main(String[] args) throws Exception {
        if(args.length ==1){ 
            String filename = args[0];
            get_clauses_from(filename);
            /*
            for(clause c : clauses){
                for(int i : c.literals){
                    System.out.print(i+" ");
                }
                System.out.println();
            }
            for(int i : atoms){
                System.out.print(i+" ");
            }
            */

            HashMap<Integer, PropValue> V = dp();
            for(int v : V.keySet()){
                System.out.print(v + " " + int_to_literal.get(v));
                if (V.get(v) == PropValue.TRUE) {
                    System.out.print(" true\n");
                }
                if (V.get(v) == PropValue.FALSE) {
                    System.out.print(" false\n");
                }
            }
            write_Value_Map_into_txt(V,"DavisPutmanOutput.txt");
        }else{
            System.out.print("this program should accept exact one argument!\n");
        }
    }

    static HashMap<Integer, PropValue> dp(){
        Set<Integer> ATOMS = new HashSet<>(atoms);
        ArrayList<clause> S = new ArrayList<>(clauses);
        HashMap<Integer, PropValue> V = new HashMap<>();
        for(int A : ATOMS){
            V.put(A, PropValue.UNBOUND);    // initailize all propersition value with UNBOUND
        }
        return dp1(ATOMS, S, V);
    }

    static HashMap<Integer, PropValue> dp1(Set<Integer> ATOMS, ArrayList<clause> S, HashMap<Integer, PropValue> V){
        while(true){ //Loop as long as there are easy cases to cherry pick 
            if(S.isEmpty()){    //BASE OF THE RECURSION: SUCCESS OR FAILURE
                for(int A : ATOMS){
                    if(V.get(A) == PropValue.UNBOUND){
                        V.put(A, PropValue.TRUE);
                    }
                }
                return V;
            }else if(S.contains(Nil)){
                //System.out.print("Nil\n");
                return new HashMap<>();
            }else if(get_pure_literal(ATOMS,S)!=0){ //EASY CASES: PURE LITERAL ELIMINATION AND FORCED ASSIGNMENT 
                int L = get_pure_literal(ATOMS,S);      //Pure literal elimination 
                V = obviousAssign(L, V);
                Iterator<clause> itr = S.iterator();
                while(itr.hasNext()){
                    if(itr.next().literals.contains(L)){
                        itr.remove();
                    }
                }
            }else if(get_singleton_clause(S)!=0){   //Forced assignment 
                int L = get_singleton_clause(S);
                V = obviousAssign(L, V);
                S = propagate(atom(L), S, V);
            }else{  //No easy cases found 
                break;
            }
        }

        // HARD CASE: PICK SOME ATOM AND TRY EACH ASSIGNMENT IN TURN 
        int A = 0;

        for(int i : ATOMS){
            if(V.get(i)==PropValue.UNBOUND){
                A = i;
                break;
            }
        }

        HashMap<Integer,PropValue> V1 = new HashMap<>(); // deep copy to save V
        for(int k : V.keySet()){
            if(V.get(k)==PropValue.UNBOUND){
                V1.put(new Integer(k), PropValue.UNBOUND);
            }
            if(V.get(k)==PropValue.TRUE){
                V1.put(new Integer(k),PropValue.TRUE);
            }
            if(V.get(k)==PropValue.FALSE){
                V1.put(new Integer(k),PropValue.FALSE);
            }
        }

        V1.put(A, PropValue.TRUE);
        //System.out.print(A+" true\n");

        ArrayList<clause> S1 = new ArrayList<>();   // deep copy to save S
        for(clause c : S){
            clause c1 = new clause();
            for(int i : c.literals){
                c1.literals.add(new Integer(i));
            }
            S1.add(c1);
        }

        S1 = propagate(A, S1, V1);
        HashMap<Integer, PropValue> VNEW = dp1(ATOMS, S1, V1);
        if(!VNEW.isEmpty()){
            return VNEW;
        }else{
            V.put(A, PropValue.FALSE);
            //System.out.print(A+" false\n");
            S1 = propagate(A, S, V);
            return(dp1(ATOMS, S1, V));
        }

    }

    static int get_pure_literal(Set<Integer> ATOMS, ArrayList<clause> S){   // get pure literal
        int pure_literal = 0;
        for(int A : ATOMS){
            int contain_pos_flag = 0;
            int contain_neg_flag = 0;
            for(clause c : S){
                if(c.literals.contains(A)){
                    contain_pos_flag = 1;
                }
                if(c.literals.contains(-A)){
                    contain_neg_flag = 1;
                }
            }
            if(contain_pos_flag ==1 && contain_neg_flag == 0){  // only contain positive literal
                pure_literal = A;
                break;
            }
            if(contain_neg_flag ==1 && contain_pos_flag == 0){  // only contain negative literal
                pure_literal = -A;
                break;
            }
        }
        return pure_literal;
    }

    static int get_singleton_clause(ArrayList<clause> S){ // get singleton clause
        int singleton_clause = 0;
        for(clause c : S){
            if(c.literals.size() == 1){
                singleton_clause = c.literals.get(0);
            }
        }
        return singleton_clause;
    }

    static HashMap<Integer, PropValue> obviousAssign(int L, HashMap<Integer, PropValue> V){
        if(L > 0){
            V.put(L, PropValue.TRUE);
            //System.out.print(atom(L)+ "true\n");
        }else if (L < 0){
            V.put(-L, PropValue.FALSE);
            //System.out.print(atom(L)+ "false\n");
        }
        return V;
    }

    static ArrayList<clause> propagate(int A, ArrayList<clause> S, HashMap<Integer, PropValue> V){
        Iterator<clause> itr_c = S.iterator();
        while(itr_c.hasNext()){  
            ArrayList<Integer> temp = itr_c.next().literals;
            if(temp.contains(A)&& V.get(A)==PropValue.TRUE){    // A is true and contain positive A
                itr_c.remove();
            }
            if(temp.contains(-A)&& V.get(A)==PropValue.FALSE){      // A is false and contain negative A
                itr_c.remove();
            }
        }

        itr_c = S.iterator();
        while(itr_c.hasNext()){
            ArrayList<Integer> temp = itr_c.next().literals;
            if(temp.contains(A)&&V.get(A)==PropValue.FALSE){        // A is false and contain positive A
                if(temp.size() == 1){
                    itr_c.remove();
                    S.add(Nil);
                    //System.out.print("nil");
                    break;
                }else {
                    Iterator<Integer> itr_i = temp.iterator();
                    while (itr_i.hasNext()) {
                        if (itr_i.next() == A) {
                            itr_i.remove();
                        }
                    }
                }
            }
            if(temp.contains(-A)&&V.get(A)==PropValue.TRUE){        // A is true and contain negative A
                if(temp.size() == 1){
                    itr_c.remove();
                    S.add(Nil);
                    //System.out.print("nil");
                    break;
                }else {
                    Iterator<Integer> itr_i = temp.iterator();
                    while (itr_i.hasNext()) {
                        if (itr_i.next() == -A) {
                            itr_i.remove();
                        }
                    }
                }
            }
        }
        return S;
    }

    static int atom(int L){
        if(L > 0){
            return L;
        }else{
            return -L;
        }
    }

    static void get_clauses_from(String filename) throws Exception {        // get input form txt file
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            if(!line.equals("0")){
                String temp[] = line.split(" ");
                clause c = new clause();
                for (String s : temp) {
                    c.literals.add(Integer.parseInt(s));
                    //System.out.print(s+" ");
                }
                //System.out.println();
                clauses.add(c);
            }else{
                break;
            }
        }
        while ((line = br.readLine()) != null){
            if(!line.equals("")) {
                String temp[] = line.split(" ");
                //System.out.print(temp[0]+" "+temp[1]+"\n");
                atoms.add(Integer.parseInt(temp[0]));
                int_to_literal.put(Integer.parseInt(temp[0]), temp[1]);
            }
        }
    }

    static void write_Value_Map_into_txt(HashMap<Integer, PropValue> V, String filename) throws IOException {
        /*
        for(int v : V.keySet()){
            System.out.print(v+" "+ int_to_literal.get(v));
            if(V.get(v)==PropValue.TRUE){
                System.out.print(" true\n");
            }
            if(V.get(v)==PropValue.FALSE){
                System.out.print(" false\n");
            }
        }*/
        File file = new File(filename);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for(int v : V.keySet()){
            if(V.get(v)==PropValue.TRUE) {
                bw.write(v + " True\n");
            }
            if(V.get(v)==PropValue.FALSE){
                bw.write(v + " False\n");
            }
        }
        bw.write("0\n");
        for(int i : int_to_literal.keySet()){
            bw.write(i+" "+int_to_literal.get(i)+"\n");
        }
        bw.close();
        System.out.print("Value Map was wrote into "+filename+"\n");
    }

}
