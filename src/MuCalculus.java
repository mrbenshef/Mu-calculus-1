/**
 * Created by s1309096 on 06/03/17.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MuCalculus {
    private static int tautology = 0;
    private static List<Integer> resetIndexes = new ArrayList<>();
    private static int nodeIndex =0;
    private static List<String> nodes = new ArrayList<>();
    private static int valid = 0;
    private static int validity = 1;

                    /* RULE1: P or not P -> valid */
    public static boolean Rule1(Node form) {
        int p = 0;
        int np = 0;
        for(formula f : form.getKey()){
            if(f.toString().startsWith("P"))
                p = 1;
            if(f.toString().startsWith("NP"))
                np = 1;
        }
        if(p == 1 && np ==1)
            return true;
        return false;
    }


                    /* Rule2: F or not F -> form */
    public static boolean Rule2(Node form) {
        for(formula f : form.getKey())
            if(f.toString().startsWith("N"))
                for(formula g : form.getKey())
                    if(g.toString().equals(f.toString().substring(1)))
                        return true;
        return false;
    }


                    /* Copy elements of a list into another */
    public static List<formula> copy(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f : form.getKey()){
            formula copy = deepCopy(f);
            newList.add(copy);
        }
        return newList;
    }


                        /* deep copy */
    public static formula deepCopy(formula input){
        formula copy = new formula(input.toString());
        copy.nameNumber = input.nameNumber;
        copy.names = input.names;
        return copy;
    }

                    /* Create new formula, but retain the names */
    public static formula create(formula f, String form) {
        formula f1 = new formula(form);
        f1.names = f.names;
        f1. nameNumber = f.nameNumber;
        return f1;
    }


                    /* Rule3: F or G --> F, G */
    public static Node Rule3(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f: form.getKey())
            if(f.toString().startsWith("or")) {
                    /* first split the formulas properly */
                int parantheses = 0;
                int commaIndex;
                for(commaIndex = 0; commaIndex<f.toString().length(); commaIndex++){
                    if(f.toString().charAt(commaIndex) == '(')
                        parantheses++;
                    else if(f.toString().charAt(commaIndex) == ')')
                        parantheses--;
                    else if(f.toString().charAt(commaIndex) == ',' && parantheses ==1)
                        break;
                }
                    /* Then expand OR into two formulas */
                formula f1 = create(f, f.toString().substring(3, commaIndex -1));
                formula f2 = create(f, f.toString().substring(commaIndex + 2, f.toString().length() - parantheses));
                newList.remove(f);
                newList.add(f1);
                newList.add(f2);
                break;
            }
        return new Node(newList);
    }


                    /* Rule4: F and G --> two trees */
    public static boolean Rule4(Node form, List<formula> abbrev, BinaryTree bTree) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f: newList)
            if(f.toString().startsWith("and")) {
                int parantheses = 0;
                int commaIndex =0;
                for(commaIndex =0; commaIndex<f.toString().length(); commaIndex++){
                    if(f.toString().charAt(commaIndex) == '(')
                        parantheses++;
                    else if(f.toString().charAt(commaIndex) == ')')
                        parantheses--;
                    else if(f.toString().charAt(commaIndex) == ',' && parantheses ==1)
                        break;
                }
                    /* Then expand OR into two formulas */
                formula f1 = create(f, f.toString().substring(4, commaIndex -1));
                formula f2 = create(f, f.toString().substring(commaIndex + 2, f.toString().length() - parantheses));

                    /* Remove the old formula from the list */
                newList.remove(f);

                    /* Prepare the trees */
                List<formula> tree1 = new ArrayList<>();
                List<formula> tree2 = new ArrayList<>();
                for(formula f3 : newList) {
                    tree1.add(create(f3, f3.toString()));
                    tree2.add(create(f3, f3.toString()));
                }

                tree1.add(f1);
                tree2.add(f2);

                Node leftNode = new Node(tree1);
                leftNode.setParents(form);

                Node rightNode = new Node(tree2);
                rightNode.setParents(form);
                    /* Recursively check the trees */
                System.out.print("\nTree1 : \n");
                isValid(leftNode, abbrev, bTree);


                System.out.print("\nTree2 : \n");
                isValid(rightNode, abbrev, bTree);
                return true;
            }
        return false;
    }


                    /* Rule5: Modal Rule */
    // TODO: to apply this rule on the most outermost formula starting with [a]
    public static Node Rule5(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f : form.getKey())
            if(f.toString().startsWith("[a]")) {
                newList.clear();
                formula f1 = create(f, f.toString().substring(3));
                newList.add(f1);
                for (formula g : form.getKey())
                    if (g.toString().startsWith("<a>")) {
                        formula g1 = create(g, g.toString().substring(3));

                        newList.add(g1);
                    }
                break;
            }
            return new Node(newList);
    }


                    /* Rule6: Unfolding least fixed point */
    public static Node Rule6(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f: form.getKey())
            if(f.toString().startsWith("m")) {
                formula f1 = create(f, f.toString().substring(3));
                newList.remove(f);
                newList.add(f1);
                break;
            }
        return new Node(newList);
    }


                    /* add new name when unfolding greatest fixed point */
    public static formula addName(formula f) {
        formula f1 = create(f, f.toString());
        f1.form = f.toString().substring(3);
        if(f.names == null) {
            f1.nameNumber = 1;
            f1.names = Character.toString(f.toString().toLowerCase().charAt(1)) + f1.nameNumber;
        }
        else {
            f1.nameNumber = f.names.length()/2 + 1;
            f1.names = f1.names + f.toString().toLowerCase().charAt(1) + f1.nameNumber;
        }
        return f1;
    }

                    /* Rule7: Unfolding greatest fixed point */
    public static Node Rule7(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f : form.getKey())
            if(f.toString().startsWith("n")) {
                formula f1 = addName(f);
                newList.remove(f);
                newList.add(f1);
                break;
            }
        return new Node(newList);
    }


                    /* Structural Rule 1: Thin rule */
    public static Node structuralRule1(Node form) {
        List<formula> newList = new ArrayList<>(form.getKey());
        for(formula f : form.getKey())
            for(formula g : form.getKey())
                if(f.toString().equals(g.toString()) && f != g) {
                    if(f.names.startsWith(g.names)) {
                        newList.remove(g);
                        return new Node(newList);
                    }
                    else if(g.names.startsWith(f.names)) {
                        newList.remove(f);
                        return new Node(newList);
                    }
                }
        return new Node(newList);
    }

                    /* Find prefix to reset on */
    public static String prefix(String str1, String str2, Node form) {
        int i = 0;
        int flag  = 1;
        if(str1 == null || str2 == null)
            return null;
        while(flag == 1 && i < str1.length()-1 && i < str2.length()) {
            i = i + 2;
            if(str1.startsWith(str2.substring(0,i)))
                for(formula f : form.getKey()) {
                    if(f.names != null)
                        if(f.names.length()>= i && f.names.startsWith(str2.substring(0,i)) && f.names.length() == i)
                            flag = 2;
                }
            else
                flag = 0;
            if(flag == 1)
                break;
            if(flag == 2)
                flag = 1;

        }
        if(i > 0 && i <  str1.length()-1 && i<str2.length() -1)
            return str1.substring(0, i);
        return null;
    }



    /* Perform reset on all formulas that contain prefix*/
    public static boolean doReset(List<formula> form, String prefix) {
        if(prefix == null)
            return false;
        for(formula f: form) {
            if(f.names != null)
                if(f.names.startsWith(prefix) && f.names.length() > prefix.length())
                    f.names = prefix;
        }
        return true;
    }

    public static List<String> parents = new ArrayList<>();
                    /* Structural Rule 2: Reset rule */
    public static boolean structuralRule2(Node form) {
        List<formula> newList = form.getKey();
        if(form.getKey().size() == 1) {
            if (newList.get(0).names != null && newList.get(0).names.length() > 2) {
                parents = form.getParents();
                form.getKey().get(0).names = form.getKey().get(0).names.substring(0, 2);
                return true;
            }
        }
        for(formula f : form.getKey())
            for(formula g : form.getKey()) {
                String prefix = prefix(f.names, g.names, form);
                if(prefix != null) {
                    parents = form.getParents();
                    doReset(form.getKey(), prefix);
                    return true;
                }
            }
        return false;
    }


                    /* Find the name of the fixed point to assign as abbreviation */
    public static char findFirstVariable(String str) {
        String variables = "TWXYZ";
        for(int i =0; i< str.length(); i++) {
            if(variables.indexOf(str.charAt(i)) != -1)
                return str.charAt(i);
        }
        return 'A';
    }


//                    /* Find abbreviations of formulas */
//    public static List<formula> abbreviations(File input) throws IOException {
//        List<formula> abbrev = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
//            String line;
//            int k = 0;
//            while ((line = br.readLine()) != null) {
//                for (int i = line.length() - 1; i >= 0; i--) {
//                    k = line.length();
//                    if ((line.charAt(i) == 'm' || line.charAt(i) == 'n') && line.charAt(i+1) != 'd') {
//                        formula f = new formula(line.substring(i, k));
//                        f.abbrev = Character.toString(findFirstVariable(line.substring(i, k)));
//                        line = line.substring(0, i) + f.abbrev;
//                        abbrev.add(f);
//                        k = i;
//                    }
//                }
//            }
//        }
//        Collections.reverse(abbrev);
//        return abbrev;
//    }
    //TODO: shrink the formula
    public static List<formula>abbreviations(File input) throws IOException {
        List<formula> abbrev = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line; int j;
            while ((line = br.readLine()) != null) {
                for( int i = 0; i < line.length(); i++)
                    if(line.charAt(i) == 'm' || line.charAt(i) == 'n') {
                        int parantheses = 0;
                        for (j = i + 1; j < line.length(); j++) {
                            if (line.charAt(j) == '(')
                                parantheses++;
                            if(line.charAt(j) == ')')
                                parantheses--;
                            if(line.charAt(j) == ' ' && parantheses == 0)
                                break;
                            if(parantheses == -1)
                                break;
                        }
                        formula f = new formula(line.substring(i, j));
                        f.abbrev = Character.toString(line.charAt(i + 1));
                        abbrev.add(f);
                    }
                }
            }
        return abbrev;
    }


                    /* Index ob the abbreviation */
    public static int abbrevIndex(List<formula> form, String x) {
        int index  = -1;
        int i =-1;
        for(formula f :form) {
            i++;
            if (f.toString().equals(x))
                return i;
        }
        return index;
    }



                    /* Apply Rules */
    public static Node applyRules(Node formulas, List<formula> abbrev, BinaryTree bTree) {
        for(formula f: formulas.getKey())
            if(f.toString().startsWith("("))
                f.form = f.toString().substring(1, f.toString().length() -1);
        if(Rule1(formulas) || Rule2(formulas)) {
            tautology = 1;
            return null;
        }

        Node form = structuralRule1(formulas);
        if(!form.getKey().equals(formulas.getKey())) {
            bTree.add(formulas, form, "left");
            form.setParents(formulas);
            return form;
        }
        if(structuralRule2(formulas))
        {
            resetIndexes.add(nodeIndex);
            bTree.add(formulas, formulas, "left");
            form.addParrent("reset");
            System.out.print("reset\n");
            for(String s : parents)
                form.addParrent(s);
            return form;
        }

        form = Rule3(formulas);
        if(!form.getKey().equals(formulas.getKey())) {
            bTree.add(formulas, form, "left");
            form.setParents(formulas);
            return form;
        }

        if(Rule4(formulas, abbrev, bTree))
            return null;

        form = Rule7(formulas);
        if(!form.getKey().equals(formulas.getKey())) {
            bTree.add(formulas, form, "left");
            form.setParents(formulas);
            return form;
        }

        form = Rule6(formulas);
        if(!form.getKey().equals(formulas.getKey())) {
            bTree.add(formulas, form, "left");
            form.setParents(formulas);
            return form;
        }

        List<formula> abb = new ArrayList<>(formulas.getKey());
        for (formula f : abbrev)
            if (abbrevIndex(formulas.getKey(), f.abbrev) != -1) {
                f.names = formulas.getKey().get(abbrevIndex(formulas.getKey(), f.abbrev)).names;
                abb.remove(formulas.getKey().get(abbrevIndex(formulas.getKey(), f.abbrev)));
                abb.add(f);
                bTree.add(formulas, form, "left");
                form.setParents(formulas);
                Node abbreviation = new Node(abb);
                abbreviation.setParents(form);
                return abbreviation;
            }

        form = Rule5(formulas);
        if(!form.getKey().equals(formulas.getKey())) {
            bTree.add(formulas, form, "left");
            form.setParents(formulas);
            return form;
        }
        validity = 0;
        return null;
    }


                /* Sort formulas alphabetically and make a string out of every node */
    public static String getNode(Node form) {
        String s = "";
        List<String> list = new ArrayList<>();
        for (formula f : form.getKey())
            list.add(f.toString() + f.names);
        Collections.sort(list);
        for(String str:list)
            s = s + str;
        return s;
    }


                /* Print node */
    public static void printNode(List<formula> form) {
        if(form != null)
            for (formula f : form)
                System.out.print(f.toString() + "    " + f.names + "           ,      ");
        System.out.print('\n');
    }


                /* Validate the formula */
    public static boolean isValid(Node formulas, List<formula> abbrev, BinaryTree bTree) {
        printNode(formulas.getKey());
        int index = 0;
        do {
            nodes = formulas.getParents();
            nodeIndex ++;
            if(formulas.getParents().contains(getNode(formulas)))
                break;
            index = formulas.getParents().indexOf(getNode(formulas));
            formulas = applyRules(formulas, abbrev, bTree);
            if(formulas != null) {
                printNode(formulas.getKey());
            }
            else {
                //validity = 0;
                return false;
            }
        } while(!formulas.getParents().contains(getNode(formulas)) && tautology == 0);
        if(formulas != null)
            if(formulas.getParents().indexOf("reset")> index -1)
                valid = 1;
        if(valid ==1 || tautology ==1) {
            valid = 0;
            tautology = 0;
        }
        else {
            validity = 0;
        }
        return true;
    }


                /* main */
    public static void main(String[] args) throws IOException {
        List<formula> formulas = new ArrayList<>();
        List<formula> abbrev = new ArrayList<>();
        List<String> nodes = new ArrayList<>();

        abbrev = abbreviations(new File(args[0]));
        System.out.print(abbrev);
        formulas.add(abbrev.get(0));

//        formula f1  = new formula("nZ.or([a]Z , W)");
//        f1.abbrev = "Z";
//
//        formula f2 = new formula("mW.and([a]W , NP)");
//        f2.abbrev = "W";
//        abbrev.add(f1); abbrev.add(f2);
//        formula f3 = new formula("nX.and(<a>X , Y)");
//        f3.abbrev = "X";
//        formula f4 = new formula("mY.or(<a>Y , P)");
//        f4.abbrev = "Y";
//        formulas.add(f3); formulas.add(f1);
//        abbrev.add(f3); abbrev.add(f4);

//        formula f1  = new formula("nZ.[a]or(Z , P)");
//        f1.abbrev = "Z";
//        formula f2 = new formula("mX.<a>and(X , NP)");
//        f2.abbrev = "X";
//        abbrev.add(f1);abbrev.add(f2);
//        formula f = new formula("and(nZ.[a]or(Z , P) , mX.<a>and(X , NP))");
//        formulas.add(f);
        Node root = new Node(formulas);
        isValid(root, abbrev, new BinaryTree(formulas));
        if(validity == 1)
            System.out.print("Formula is valid");
        else
            System.out.print(("Formula is not valid"));


    }





}
