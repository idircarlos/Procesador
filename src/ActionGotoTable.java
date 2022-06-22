import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActionGotoTable {
    
    private Map<String, Map<String, String>> tabla; 
    
    public ActionGotoTable(){
        tabla = new LinkedHashMap<String, Map<String, String>>();
        rellenarTablaAccGoTo();
    }

    private void addEstado(String estado, ArrayList<String> pares){
        Map<String, String> valores = new LinkedHashMap<String, String>();
        for (int i = 0; i < pares.size(); i = i + 2){
            valores.put(pares.get(i),pares.get(i+1));
        }
        tabla.put(estado, valores);
    }

    public String getAccion(String estado, String terminal){
        return tabla.get(estado).get(terminal);
    }

    public void printTable(){
        Iterator<String> estados = tabla.keySet().iterator();
        Iterator<String> pares;
        String estado;
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

    private ArrayList<String> crearPares(String [] simbolos, String [] acciones){
        ArrayList <String> pares = new ArrayList<>();
        for (int i = 1; i < acciones.length; i++){
            if (!acciones[i].equals("")){
                if (simbolos[i].equals("coma")){
                    pares.add(",");
                }
                else {
                    pares.add(simbolos[i]);
                }
                pares.add(acciones[i]);
            }
        }
        return pares;
    }

    private void rellenarTablaAccGoTo(){
        BufferedReader bf = null;
		FileReader fr = null;
        try {
			fr = new FileReader("actiongototable.csv");
			bf = new BufferedReader(fr);
            String [] simbolos = bf.readLine().split(",");
            String linea = null;
            String [] fila;
            String estado = null;
            while ((linea = bf.readLine()) != null){
                fila = linea.split(",");
                estado = fila[0];
                addEstado(estado,crearPares(simbolos, fila));
            }
        }catch(Exception e){

        }
    }
}
