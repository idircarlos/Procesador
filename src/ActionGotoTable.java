import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ActionGotoTable {
    
    private Map<Integer, Map<String, String>> tabla; 
    
    public ActionGotoTable(){
        tabla = new HashMap<Integer, Map<String, String>>();
        inicializarTabla();
    }

    private void addEstado(Integer estado, String [] pares){
        Map<String, String> valores = new HashMap<String, String>();
        for (int i = 0; i < pares.length; i = i + 2){
            valores.put(pares[i],pares[i+1]);
        }
        tabla.put(estado, valores);
    }

    public String getAccion(Integer estado, String terminal){
        return tabla.get(estado).get(terminal);
    }

    public void printTable(){
        Iterator<Integer> estados = tabla.keySet().iterator();
        Iterator<String> pares;
        Integer estado;
        String terminal;
        while (estados.hasNext()){
            estado = estados.next();
            Map<String, String> mapa = tabla.get(estado);
            pares = mapa.keySet().iterator();
            System.out.print("Estado " + estado + ":\t");
            while (pares.hasNext()){
                terminal = pares.next();
                System.out.print(terminal + ", " + mapa.get(terminal) + " | ");
            }
            System.out.println();
        }
    }

    private void inicializarTabla() {
        addEstado(0, new String[] {"let","d4","id","d9",
                                    "if","d5","for","d7",
                                    "print","d10","input","d11",
                                    "return","d12","function","d13",
                                    "$","r3","S","1",
                                    "B","2","C","6",
                                    "F","3","F1","8"});
                                    
		/* Estado 1*/
		addEstado(1, new String[] {"$", "acc"}); 

		/* Estado 2*/
		addEstado(2, new String[] {"let","d4","id","d9",
                                    "if","d5","for","d7",
                                    "print","d10","input","d11",
                                    "return","d12","function","d13",
                                    "$","r4","S","14",
                                    "B","2","C","6",
                                    "F","3","F1","8"});

		/* Estado 3*/
		addEstado(3, new String[] {"let","d4","id","d9",
                                    "if","d5","for","d7",
                                    "print","d10","input","d11",
                                    "return","d12","function","d13",
                                    "$","r4","S","15",
                                    "B","2","C","6",
                                    "F","3","F1","8"});

        addEstado(4, new String[] {"int", "d17","string", "d18",
                                    "boolean", "d19", "T", "16"});
        
        addEstado(5, new String[] {"(", "d20"});

        addEstado(6, new String[] {"let", "r6", "id", "r6", 
                                    "if", "r6", "for", "r6",
                                    "}", "r6", "print", "r6",
                                    "input", "r6", "return", "r6",
                                    "function", "r6", "$", "r6"});

        addEstado(7, new String[] {"(", "d21"}); 

        addEstado(8, new String[] {"(", "d23", "F2", "22"});      

        addEstado(9, new String[] {"(", "d24", "=", "d26", 
                                    "%=", "d25"});

        addEstado(10, new String[] {"(", "d27"});

        addEstado(11, new String[] {"(", "d28"});

        addEstado(12, new String[] {"id", "d35", ";", "r39",
                                    "(", "d34", "-", "d33",
                                    "entero", "d36", "cadena", "d37",
                                    "X", "29", "E", "30",
                                    "E1", "31", "E3", "32"});
        
        addEstado(13, new String[] {"id", "d38"});

        addEstado(14, new String[] {"$", "r1"});

        addEstado(15, new String[] {"$", "r2"});

        addEstado(16, new String[] {"id", "d39"});

        addEstado(17, new String[] {"id", "r35", "(", "r35"});

        addEstado(18, new String[] {"id", "r36", "(", "r36"});

        addEstado(19, new String[] {"id", "r37", "(", "r37"});

        addEstado(20, new String[] {"id", "d35", "(", "d34",
                                    "!", "d33", "entero", "d36",
                                    "cadena", "d37", "E", "40",
                                    "E1", "31", "E3", "32"});

        addEstado(21, new String[] {"id", "d42", ";", "r16",
                                    ")", "r16", "D", "41"});
        
        addEstado(22, new String[] {"{", "d44", "F3", "43"});

        addEstado(23, new String[] {")", "r30", "int", "d17",
                                    "string", "d18", "boolean", "d19",
                                    "A", "45", "T", "46"});
        
        addEstado(24, new String[] {"id", "d35", "(", "d34",
                                    ")", "r32", "!", "d33",
                                    "entero", "d36", "cadena", "d37",
                                    "L", "47", "E", "48",
                                    "E1", "31", "E3", "32"});
    
        addEstado(25, new String[] {"id", "r37", "(", "r37"});




              

        














        



































        


        addEstado(50, new String[] {";","d75","<","d54"});

        addEstado(51, new String[] {")","d76","<","d54"});

        addEstado(52, new String[] {")","d77"});

        addEstado(53, new String[] {"let","r15","id","r15",
                                    "if","r15","for","d54",
                                    "}","r15","print","r15",
                                    "input","r15","return","r15",
                                    "function","r15","$","r15"});
        
        addEstado(54, new String[] {"id","d35","(","d34",
                                    "!","d33","entero","d36",
                                    "cadena","d37","E1","78",
                                    "E3","32"});
        
        addEstado(55, new String[] {"id","d35","(","d34",
                                    "!","d33","entero","d36",
                                    "cadena","d37","E3","79"});
                                    
        addEstado(56, new String[] {";","r44",")","r44",
                                    ",","r44","<","r44",
                                    "-","r44"});

        addEstado(57, new String[] {")","d80","<","d54"});

        addEstado(58, new String[] {"id","d35","(","d34",
                                    ")","r32","!","d33",
                                    "entero","d36","cadena","d37",
                                    "L","81","E","48",
                                    "E1","31","E3","32"});
        

    }
}
