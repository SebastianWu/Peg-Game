import java.io.*;
import java.util.*;

class clause{
    ArrayList<Integer> literals = new ArrayList<>();
}

class Jump{
    int A;  // from A
    int B;  // across B
    int C;  // to C
    int I;  // at time I
    int ID; // integer index
    Jump(int a, int b, int c, int i, int id){
        this.A = a;
        this.B = b;
        this.C = c;
        this.I = i;
        this.ID = id;
    }
}

class Peg{
    int A;  // At position A
    int I;  // At time I
    int ID;     // integer index
    Peg(int a, int i, int id){
        this.A = a;
        this.I = i;
        this.ID = id;
    }
}

public class FrontEnd {
    static int hole_num;    // total hole number
    static int empty_hole_num;      // empty hole number
    static ArrayList<int[]> triples_set = new ArrayList<>();    // arraylist to store peg puzzle specification from input
    static ArrayList<Jump> Jump_atoms = new ArrayList<>();     // store all Jump action
    static ArrayList<Peg> Peg_atoms = new ArrayList<>();    // store all Peg
    static ArrayList<clause> S = new ArrayList<>(); // Set of propositional formulas in CNF
    static int start_state;


    public static void main(String[] args) throws Exception{
        if(args.length ==1){
            String filename = args[0];
            get_puzzle_specification_from(filename);    // get the input from txt file

            System.out.print("The specification of peg game is shown as a set of triples as follow:\n");

            for(int[] t : triples_set){
                System.out.println(t[0]+" "+t[1]+" "+t[2]);
            }

            generate_atoms();   

            preposition_encoding(start_state);  // preprosition Encoding

            write_encoded_preposition_into_txt("FrontEndOutput.txt");
        }else{
            System.out.print("this program should accept exact one argument!\n");
        }

    }
    static void get_puzzle_specification_from(String filename)throws Exception{
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        line = br.readLine();
        String temp[] = line.split(" ");
        hole_num = Integer.parseInt(temp[0]);
        start_state = Integer.parseInt(temp[1]);
        empty_hole_num = 1;
        while ((line = br.readLine()) != null){
            if(!line.equals("")) {
                String triple[] = line.split(" ");
                int t[] = new int[3];
                int index = 0;
                for (String s : triple) {
                    t[index] = Integer.parseInt(s);
                    index++;
                }
                triples_set.add(t);
            }
        }
    }

    static void preposition_encoding(int empty_hole_index){
        // Precondition axioms
        // A => B ^ C ^ ~D
        // ~A V B, ~A V C, ~A V ~D
        for(Jump j : Jump_atoms){
            clause c1 = new clause();
            c1.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.A,j.I)!=0)c1.literals.add(search_from_Peg_Atoms(j.A,j.I));
            clause c2 = new clause();
            c2.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.B,j.I)!=0)c2.literals.add(search_from_Peg_Atoms(j.B,j.I));
            clause c3 = new clause();
            c3.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.C, j.I)!=0)c3.literals.add(-search_from_Peg_Atoms(j.C, j.I));
            S.add(c1);
            S.add(c2);
            S.add(c3);
        }
        // Causal axioms
        // A => ~B ^ ~C ^ D
        // ~A V ~B, ~A V ~C, ~A V D
        for(Jump j : Jump_atoms){
            clause c1 = new clause();
            c1.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.A,j.I+1)!=0)c1.literals.add(-search_from_Peg_Atoms(j.A,j.I+1));
            clause c2 = new clause();
            c2.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.B,j.I+1)!=0)c2.literals.add(-search_from_Peg_Atoms(j.B,j.I+1));
            clause c3 = new clause();
            c3.literals.add(-j.ID);
            if(search_from_Peg_Atoms(j.C, j.I+1)!=0)c3.literals.add(search_from_Peg_Atoms(j.C, j.I+1));
            S.add(c1);
            S.add(c2);
            S.add(c3);
        }
        // Frame axioms Peg(H, I) and ~Peg(H, I+1)
        // A ^ ~B => (C V ... V F)
        // ~A V B V C V ... V F
        for(Peg p : Peg_atoms){
            if( search_from_Peg_Atoms(p.A, p.I+1) > 0) {
                clause c = new clause();
                c.literals.add(-p.ID);
                c.literals.add(search_from_Peg_Atoms(p.A, p.I + 1));
                for (int i : search_H_dis_from_Jump_Atoms(p.A, p.I)) {
                    c.literals.add(i);
                }
                S.add(c);
            }
        }
        // Frame axioms ~Peg(H, I) and Peg(H, I+1)
        // ~A ^ B => (C V ... V F)
        // A V ~B V C V ... V F
        for(Peg p : Peg_atoms){
            if(search_from_Peg_Atoms(p.A, p.I+1)>0){
                clause c = new clause();
                c.literals.add(p.ID);
                c.literals.add(-search_from_Peg_Atoms(p.A, p.I+1));
                for(int i : search_H_app_from_Jump_Atoms(p.A, p.I)){
                    c.literals.add(i);
                }
                S.add(c);
            }
        }
        // One action at a time
        // ~(A ^ B)
        // ~A V ~B
        Set< Set<Integer> > sametime_act_dual_Set = generate_sametime_action_dual();
        for(Set<Integer> d : sametime_act_dual_Set){
            clause c = new clause();
            for(int i : d){
                c.literals.add(-i);
            }
            S.add(c);
        }
        // Start state
        // ~A ^ B ^ C ^ D ^...
        for(int i = 1; i<hole_num+1; i++){
            clause c = new clause();
            if(search_from_Peg_Atoms(i, 1)!=0) {
                if (i==empty_hole_index) {
                    c.literals.add(-search_from_Peg_Atoms(i, 1));
                } else {
                    c.literals.add(search_from_Peg_Atoms(i, 1));
                }
            }
            S.add(c);
        }
        // End State: at least one peg remians at time N-1
        // A V B V...
        clause E = new clause();
        for(int i = 1; i<hole_num+1; i++){
            if(search_from_Peg_Atoms(i, hole_num-1)!=0) {
                E.literals.add(search_from_Peg_Atoms(i, hole_num - 1));
            }
        }
        if(!E.literals.isEmpty()) {
            S.add(E);
        }
        // End State : no two holes have a peg
        // ~(A ^ B)
        // ~A V ~B

        Set< Set<Integer> > pair_of_holes_Set = generate_pair_of_holes_at_end_time();
        for(Set<Integer> pair : pair_of_holes_Set){
            clause c = new clause();
            for(int i : pair){
                c.literals.add(-i);
            }
            S.add(c);
        }

    }

    private static void generate_atoms(){
        int id = 1;
        int max_jump_time_unit = hole_num-empty_hole_num-1;
        for(int[] t : triples_set){
            for(int i = 1; i<max_jump_time_unit+1;i++) {
                Jump j = new Jump(t[0], t[1], t[2], i, id);
                Jump_atoms.add(j);
                id++;
            }
            for(int i = 1; i<max_jump_time_unit+1;i++) {
                Jump j = new Jump(t[2], t[1], t[0], i, id);
                Jump_atoms.add(j);
                id++;
            }
        }
        int max_peg_time_unit = hole_num-empty_hole_num;
        for(int p = 1; p<hole_num+1; p++){
            for(int i = 1; i<max_peg_time_unit+1;i++){
                Peg peg = new Peg(p, i, id);
                Peg_atoms.add(peg);
                id++;
            }
        }
    }

    private static int search_from_Peg_Atoms(int A, int I){
        int Atom_id = 0;
        for(Peg p : Peg_atoms){
            if(p.A == A && p.I == I){
                Atom_id = p.ID;
            }
        }
        return Atom_id;
    }

    private static ArrayList<Integer> search_H_dis_from_Jump_Atoms(int H, int I){   //Peg(H, I) to ~Peg(H, I+1)
        ArrayList<Integer> Atom_id_list = new ArrayList<>();
        for(Jump j : Jump_atoms){
            if(j.A == H && j.I == I){
                Atom_id_list.add(j.ID);
            }
                if(j.B == H && j.I == I){
                Atom_id_list.add(j.ID);
            }
        }
        return Atom_id_list;
    }

    private static ArrayList<Integer> search_H_app_from_Jump_Atoms(int H, int I){   //~Peg(H, I) to Peg(H, I+1)
        ArrayList<Integer> Atom_id_list = new ArrayList<>();
        for(Jump j : Jump_atoms){
            if(j.C == H && j.I == I){
                Atom_id_list.add(j.ID);
            }
        }
        return Atom_id_list;
    }

    static Set< Set<Integer> > generate_sametime_action_dual(){     // One action at a time
        Set< Set<Integer> > sametime_act_dual_Set = new HashSet<>();
        for(Jump j1 : Jump_atoms){
            for(Jump j2 :Jump_atoms){
                if(j1.ID!=j2.ID && j1.I == j2.I){
                    Set<Integer> dual = new HashSet<>();
                    dual.add(j1.ID);
                    dual.add(j2.ID);
                    sametime_act_dual_Set.add(dual);
                }
            }
        }
        return sametime_act_dual_Set;
    }

    static Set< Set<Integer> > generate_pair_of_holes_at_end_time(){    // one peg at end state
        Set< Set<Integer> > pair_of_holes_Set = new HashSet<>();
        for(Peg p1 : Peg_atoms){
            for(Peg p2 : Peg_atoms){
                if(p1.ID!=p2.ID && p1.I == hole_num-empty_hole_num && p2.I == hole_num-empty_hole_num){
                    Set<Integer> pair = new HashSet<>();
                    pair.add(p1.ID);
                    pair.add(p2.ID);
                    pair_of_holes_Set.add(pair);
                }
            }
        }
        return pair_of_holes_Set;
    }

    static void write_encoded_preposition_into_txt(String filename) throws IOException {
        File file = new File(filename);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        for(clause c : S){
            for(int i : c.literals){
                bw.write(i+" ");
            }
            bw.write("\n");
        }
        bw.write("0\n");
        for(Jump j : Jump_atoms){
            bw.write(j.ID+" Jump("+j.A+","+j.B+","+j.C+","+j.I+")\n");
        }
        for(Peg p : Peg_atoms){
            bw.write(p.ID+" Peg("+p.A+","+p.I+")\n");
        }
        bw.close();
        System.out.print("Encoded Preposition was wrote into "+filename+"\n");
    }
}
