import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.*;

public class Procesador {
	private static DeterministFiniteAutomate afd; //Automata con las transiciones (matriz de transiciones)
	private static ActionGotoTable agt;
	private static Character car; //Caracter leido 
	public static BufferedReader bf;
	public static FileReader fr;
	public static FileWriter FichToken;
	public static FileWriter FichTablaSimb;
	public static FileWriter FichError;
	private static Map<String,Integer> palRes;
	private static Map<String,Integer> TGlobal = new HashMap<String,Integer>();
	private static ArrayList<String> ListTG = new ArrayList<String>();
	private static Map<String,Integer> TLocal;
	private static boolean zonaDecl = false;
	private static final int EOF=65535;
	private static int conter=0;
	private static int nLinea=1;
	
	


	public static Token ALexico() {
		int estadoActual=0; //Estado actual del automata
		String valor =""; //Almacen de cadenas de numeros
		String cadena=""; //Almacen de cadenas de caracteres
		Token token=null; //Token generado
		String acciones = null;

		while(estadoActual<=6) {
			if(car==EOF) {
				return null;
			}
			if(car == '\n') {
				nLinea++;
			}
			ParMT par = afd.getPar(estadoActual,car);
			
			if(par!=null) { // Estado final o transicion error
			//System.out.println(estadoActual+ "\t" +car+"\t"+par.toString());
			acciones = par.getAcciones();
			estadoActual = par.getEstadoSig();
			}
			else {
				switch(estadoActual) {				
				case 0:
					GenerarErrores(1,""+car);	
					break;
				case 3:
					break;
				case 4:
					GenerarErrores(4,"");
					break;
				case 5:  
					GenerarErrores(5,"");
					break;
				case 6:
					break;
					
				}
				//System.out.println(estadoActual+ "\t" +(int)car);
				
				car=leer();
				return null;
			}
			//SINO SALTA UN ERROR

			switch(acciones) {
			case "A":
				Integer pos = buscarTPR(cadena);
				if (pos != null){
					GenToken(pos,"");
				}
				else if(zonaDecl == true){

				}
				else {
					if(TGlobal.containsKey(cadena)) {
						GenToken(11,TGlobal.get(cadena));
					}
					else {
					pos = conter;
					conter++;
					TGlobal.put(cadena, pos);
					ListTG.add(cadena);
					GenToken(11,pos);
					}
				}
				
				break;
			case "B":
				if(Integer.parseInt(valor)>32767) {
					GenerarErrores(3,"");
				}
				else {
				token = GenToken(12,valor);
				}
				break;
			case "X":
				cadena = cadena + car;
				car = leer();
				break;
			case "S":
				valor = valor + car;
				car = leer();
				break;
			case "C":
				if(cadena.length()>64) {
					GenerarErrores(2,"");
				}
				else
				token = GenToken(13,"\""+cadena+"\"");
				car = leer();
				break;
			case "D":
				token = GenToken(14,"");
				car=leer();
				break;
			case "E":
				token = GenToken(15,"");
				car=leer();
				break;
			case "F":
				token = GenToken(16,"");
				car=leer();
				break;
			case "G":
				token = GenToken(17,"");
				car=leer();
				break;
			case "H":
				token = GenToken(18,"");
				car=leer();
				break;
			case "I":
				token = GenToken(19,"");
				car=leer();
				break;
			case "J":
				token = GenToken(20,"");
				car = leer();
				break;
			case "K":
				token = GenToken(21,"");
				car=leer();
				break;
			case "M":
				token = GenToken(22,"");
				car=leer();
				break;
			case "N":
				token=GenToken(23,"");
				car=leer();
				break;
			case "O":
				token = GenToken(24,"");
				car=leer();
				break;
			case "L":
				car = leer();
				break;
			default:
				GenerarErrores(1,"");
				car = leer();
				break;
			}
		}
		return token;
	}

	public static void AnSintactico (){
		Token token = ALexico();
		String accion;
		Integer estado = 0;
	}

	

	private static Character leer() {
		try {
			return (char)bf.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Token GenToken(Integer id, Object val) {
		if(val==null){
			val = "";
		}
		String tokenTexto= "<"+id+","+val+">";
		try {
			FichToken.write(tokenTexto+ "\n");
		}
		catch(IOException e){

		}
		return new Token(id,val);
	}

	private static void GenerarErrores(int error, String datos) {
		try {
		switch(error) {
		case 1:
			FichError.write("Error AnLexico (1): Linea "+nLinea+" Error en la generacion del token, no se esperaba este caracter "+datos+"\n");
			break;
		case 2:
			FichError.write("Error AnLexico (2): Linea "+nLinea+" Cadena de mas de 64 caracteres\n");
			break;
		case 3:
			FichError.write("Error AnLexico (3): Linea "+nLinea+" Entero fuera de rango, el numero es mayor de 32767\n");
			break;
		case 4:
			FichError.write("Error AnLexico (4): Linea "+nLinea+" El operador %= no se ha construido correctamente\n");
			break;
		case 5:
			FichError.write("Error AnLexico (5): Linea "+nLinea+" Solo se admite el formato de comentarios // -> // Comentario\n");
			break;
		}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	

	private static void rellenarTPR(){
		palRes.put("if",1);
		palRes.put("for",2);
		palRes.put("let",3);
		palRes.put("int",4);
		palRes.put("boolean",5);
		palRes.put("string",6);
		palRes.put("function",7);
		palRes.put("return",8);
		palRes.put("print",9);
		palRes.put("input",10);
	}
	private static Integer buscarTPR(String lexema){
		Integer pos = null;
		if (palRes.containsKey(lexema)){
			pos = palRes.get(lexema);
		}
		return pos;
	}
	
	private static void imprimirTablaSimbolos() {
		try {
			FichTablaSimb.write("Tabla Simbolos Global #1:\n");
			for(int i=0; i<ListTG.size();i++)
			FichTablaSimb.write("* LEXEMA : '"+ListTG.get(i)+"'\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void main (String [] args) {
		palRes = new HashMap<>();
		rellenarTPR();
		afd = new DeterministFiniteAutomate();
		agt = new ActionGotoTable();
		agt.printTable();
		try {
			fr = new FileReader("./data/input1.txt");
			bf = new BufferedReader(fr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			car = (char)bf.read();
			FichToken = new FileWriter(new File("./data/tokens.txt"));
			FichTablaSimb = new FileWriter(new File("./data/TS.txt"));
			FichError = new FileWriter(new File("./data/error.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(car!=EOF){
			ALexico();
		}
		imprimirTablaSimbolos();
		try {
			fr.close();
			bf.close();
			FichToken.close();
			FichTablaSimb.close();
			FichError.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
		


	}
}
