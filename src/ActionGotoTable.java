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
        addEstado(0, new String[] {"let", "d4", "if", "d5",
									   "alert", "d7", "input", "d8",
									   "id", "d9", "return", "d10",
									   "function", "d12", "$", "r4",
									   "A", "1", "B", "2", "F", "3",
									   "S", "6", "F1", "11"});

		/* Estado 1*/
		addEstado(1, new String[] {"$", "a"});
		
		/* Estado 2*/
		addEstado(2, new String[] {"let", "d4", "if", "d5",
									   "alert", "d7", "input", "d8",
									   "id", "d9", "return", "d10",
									   "function", "d12", "$", "r4",
									   "A", "13", "B", "2", "F", "3",
									   "S", "6", "F1", "11"});

		/* Estado 3*/
		addEstado(3, new String[] {"let", "d4", "if", "d5",
									   "alert", "d7", "input", "d8",
									   "id", "d9", "return", "d10",
									   "function", "d12", "$", "r4",
									   "A", "14", "B", "2", "F", "3",
									   "S", "6", "F1", "11"});
    }
}
