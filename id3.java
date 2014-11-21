import java.io.*;
import java.util.*;

public class id3 {
        int numAtributos;              
        String []nomAtributos;        
        private int atributoClase;
        Vector []domains;

        class DataPoint {
                public int []attributes;

                public DataPoint(int numatributos) {
                        attributes = new int[numatributos];
                }
        };

        class TreeNode {
                public double entropia;                  
                public Vector data;                     
                public int decompositionAttribute;      
                public int decompositionValue;          
                public TreeNode []children;             
                public TreeNode parent;                 

                public TreeNode() {
                        data = new Vector();
                }
        };

        TreeNode root = new TreeNode();
        
        public int getSymbolValue(int atributo, String simbolo) {
                int index = domains[atributo].indexOf(simbolo);
                if (index < 0) {
                        domains[atributo].addElement(simbolo);
                        return domains[atributo].size() -1;
                }
                return index;
        }

        
        public int []getAllValues(Vector data, int atributo) {
                Vector values = new Vector();
                int num = data.size();
                for (int i=0; i< num; i++) {
                        DataPoint point = (DataPoint)data.elementAt(i);
                        String simbolo = (String)domains[atributo].elementAt(point.attributes[atributo] );
                        int index = values.indexOf(simbolo);
                        if (index < 0) {
                                values.addElement(simbolo);
                        }
                }

                int []array = new int[values.size()];
                for (int i=0; i< array.length; i++) {
                        String simbolo = (String)values.elementAt(i);
                        array[i] = domains[atributo].indexOf(simbolo);
                }
                values = null;
                return array;
        }


        public Vector getSubset(Vector data, int atributo, int value) {
                Vector subset = new Vector();

                int num = data.size();
                for (int i=0; i< num; i++) {
                        DataPoint point = (DataPoint)data.elementAt(i);
                        if (point.attributes[atributo] == value) subset.addElement(point);
                }
                return subset;
        }

        
        public double calcularEntropia(Vector data) {

                int numdata = data.size();
                if (numdata == 0) return 0;

                int atributo = atributoClase;
                int numvalores = domains[atributo].size();
                double sum = 0;
                for (int i=0; i< numvalores; i++) {
                        int count=0;
                        for (int j=0; j< numdata; j++) {
                                DataPoint point = (DataPoint)data.elementAt(j);
                                if (point.attributes[atributo] == i) count++;
                        }
                        double probability = 1.*count/numdata;
                        if (count > 0) sum += -probability*Math.log(probability);
                }
                return sum;
        }

        public boolean yaUsadoDescomponer(TreeNode node, int atributo) {
                if (node.children != null) {
                        if (node.decompositionAttribute == atributo )
                                return true;
                }
                if (node.parent == null) return false;
                return yaUsadoDescomponer(node.parent, atributo);
        }

        
        public void descomponerNodo(TreeNode node) {

                double mejorEntropia=0;
                boolean selected=false;
                int atributoSeleccionado=0;

                int numdata = node.data.size();
                int numAtributosEnt = numAtributos-1;
                node.entropia = calcularEntropia(node.data);
                if (node.entropia == 0) return;
                
                for (int i=0; i< numAtributosEnt; i++) {

                    if ( atributoClase == i ) {
                        continue;
                        }
                        int numvalues = domains[i].size();
                        if ( yaUsadoDescomponer(node, i) ) continue;
                        double averageentropy = 0;
                        for (int j=0; j< numvalues; j++) {
                                Vector subset = getSubset(node.data, i, j);
                                if (subset.size() == 0) continue;
                                double subentropy = calcularEntropia(subset);
                                averageentropy += subentropy * subset.size();  // Weighted sum
                        }
                        
                        averageentropy = averageentropy / numdata;   // Taking the weighted average
                        if (selected == false) {
                          selected = true;
                          mejorEntropia = averageentropy;
                          atributoSeleccionado = i;
                        } else {
                          if (averageentropy < mejorEntropia) {
                            selected = true;
                            mejorEntropia = averageentropy;
                            atributoSeleccionado = i;
                          }
                        }
                }

                if (selected == false) return;
                // Now divide the dataset using the selected attribute
                int numvalues = domains[atributoSeleccionado].size();
                node.decompositionAttribute = atributoSeleccionado;
                node.children = new TreeNode [numvalues];
                for (int j=0; j< numvalues; j++) {
                  node.children[j] = new TreeNode();
                  node.children[j].parent = node;
                  node.children[j].data = getSubset(node.data, atributoSeleccionado, j);
                  node.children[j].decompositionValue = j;
                }
                
                for (int j=0; j< numvalues; j++) {
                  descomponerNodo(node.children[j]);
                }
                
                node.data = null;               
        }

        
        public int leerDatos(String filename)  throws Exception {

                FileInputStream in = null;

                try {
                        File inputFile = new File(filename);
                        in = new FileInputStream(inputFile);
                } catch ( Exception e) {
                        System.err.println( "No es posible abrir el archivo: " + filename + "\n" + e);
                        return 0;
                }

                BufferedReader bin = new BufferedReader(new InputStreamReader(in) );

                String input;
                while(true) {
                        input = bin.readLine();
                        if (input == null) {
                                System.err.println( "El archivo esta vacio: " + filename + "\n");
                                return 0;
                        }
                        if (input.startsWith("//")) continue;
                        if (input.equals("")) continue;
                        break;
                }

                StringTokenizer tokenizer = new StringTokenizer(input);
                numAtributos = tokenizer.countTokens();
                if (numAtributos <= 1) {
                        System.err.println( "Leer linea: " + input);
                        System.err.println( "No se pueden obtener los nombres de los atributos en la linea");
                        System.err.println( "Se espera al menos un atributo de entrada y uno de salida");
                        return 0;
                }

                domains = new Vector[numAtributos];
                for (int i=0; i < numAtributos; i++) domains[i] = new Vector();
                nomAtributos = new String[numAtributos];

                for (int i=0; i < numAtributos; i++) {
                        nomAtributos[i]  = tokenizer.nextToken();
                }

                while(true) {
                        input = bin.readLine();
                        if (input == null) break;
                        if (input.startsWith("//")) continue;
                        if (input.equals("")) continue;

                        tokenizer = new StringTokenizer(input);
                        int numtokens = tokenizer.countTokens();
                        if (numtokens != numAtributos) {
                                System.err.println( "Leer " + root.data.size() + " datos");
                                System.err.println( "Ultima linea leida: " + input);
                                System.err.println( "Esperando " + numAtributos  + " atributos");
                                return 0;
                        }

                        DataPoint point = new DataPoint(numAtributos);
                        for (int i=0; i < numAtributos; i++) {
                                point.attributes[i]  = getSymbolValue(i, tokenizer.nextToken() );
                        }
                        root.data.addElement(point);
                }

                bin.close();

                return 1;
        }       
   
        
        public void imprimirArbol(TreeNode node, String tab) {

                int outputattr = atributoClase;

                if (node.children == null) {
                        int []values = getAllValues(node.data, outputattr );
                        if (values.length == 1) {
                                System.out.println(tab + "\t" + nomAtributos[outputattr] + " = \"" + domains[outputattr].elementAt(values[0]) + "\";");
                                return;
                        }
                        System.out.print(tab + "\t" + nomAtributos[outputattr] + " = {");
                        for (int i=0; i < values.length; i++) {
                                System.out.print("\"" + domains[outputattr].elementAt(values[i]) + "\" ");
                                if ( i != values.length-1 ) System.out.print( " , " );
                        }
                        System.out.println( " };");
                        return;
                }

                int numvalues = node.children.length;
                for (int i=0; i < numvalues; i++) {
                  System.out.println(tab + "if( " + nomAtributos[node.decompositionAttribute] + " == \"" +
                          domains[node.decompositionAttribute].elementAt(i) + "\") {" );
                  imprimirArbol(node.children[i], tab + "\t");
                  if (i != numvalues-1) System.out.print(tab +  "} else ");
                  else System.out.println(tab +  "}");
                }
        }

        
        public void createDecisionTree() {
                descomponerNodo(root);
                imprimirArbol(root, "");
        }

      
        public static void main(String[] args) throws Exception {

                id3 me = new id3();
                
        Scanner in = new Scanner(System.in);

        System.out.print("Ruta del archivo: ");
        String str = in.nextLine();

        System.out.print("Atributo Clase(#Columna): ");
        me.atributoClase = in.nextInt();

        int status = me.leerDatos( str );
        if (status <= 0) {
            return;
        }
                me.createDecisionTree();
        }       
}
