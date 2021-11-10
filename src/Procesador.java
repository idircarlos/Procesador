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
	public static FileWriter FichParse;
	private static Map<String,Integer> palRes;
	private static Map<String,Integer> TGlobal = new HashMap<String,Integer>();
	private static ArrayList<String> ListTG = new ArrayList<String>();
	private static String[] consecuentes = new String[51];
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
				return new Token(EOF,"$");
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
					token = GenToken(pos,"");
				}
				else if(zonaDecl == true){

				}
				else {
					if(TGlobal.containsKey(cadena)) {
						token = GenToken(11,TGlobal.get(cadena));
					}
					else {
					pos = conter;
					conter++;
					TGlobal.put(cadena, pos);
					ListTG.add(cadena);
					token = GenToken(11,pos);
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

	public static void ASintactico (){
		Token token = ALexico();
		String simbolo = obtenerLexema(token);
		String accion;
		char operacion=' ';
		String estado = "0";
		String antecedente; //Simbolos en el antecedente (cadena de simbolos)
		int k = 1; //Simbolos en el consecuente (numero)
		String consecuente; //Simbolos en el consecuente (cadena de simbolos)
		String regla; //Regla utilizada (se vuelca en el fichero parse)
		
		Stack<String> pilaAsc = new Stack<String>();
		pilaAsc.push(estado);
		int iter = 1;
		while (true){
			System.out.println("-------------------ITER " + iter + "--------------------");
			System.out.println( "1\t" +pilaAsc);
			System.out.println("Esatdo\t" +estado);
			System.out.println("Simbolo\t" +simbolo);
			accion = agt.getAccion(estado, simbolo);
			System.out.println("ACCION\t" +accion);
			if(accion!=null){
				operacion = accion.charAt(0); // Miramos si es desplazamiento ('d') regla ('r') aceptado ('acc') o error (' ')
			}
			else {
				System.out.println("No aceptado");
				System.exit(1);
			}
			if(operacion == 'd'){
				estado = accion.substring(1,accion.length());
				System.out.println("2\t" + pilaAsc);
				pilaAsc.push(simbolo);
				System.out.println("3\t" + pilaAsc);
				pilaAsc.push(estado);
				System.out.println("4\t" + pilaAsc);
				token = ALexico();
				System.out.println("token\t"+token);
				simbolo = obtenerLexema(token);
			}
			else if (operacion == 'r'){
				regla = accion.substring(1, accion.length());
				String[] linea = consecuentes[Integer.parseInt(regla)].split(":");
				System.out.println(Arrays.toString(linea));
				k = Integer.parseInt(linea[1]);
				antecedente = linea[0];
				System.out.println("regla " +regla);
				System.out.println("Antecedente  "+antecedente);
				System.out.println("k " + k);
				try{
					FichParse.write(regla+" ");
				}
				catch (IOException e){

				}
				for (int i = 1; i <= 2*k; i++){
					pilaAsc.pop();
				}
				estado = agt.getAccion(pilaAsc.lastElement(), antecedente);
				System.out.println("5\t" + pilaAsc);
				pilaAsc.push(antecedente);
				System.out.println("6\t" + pilaAsc);
				pilaAsc.push(estado);
				System.out.println("7\t" + pilaAsc);
			}
			else if(operacion == 'a'){
				System.out.println("Aceptado");
				return;
			}
			iter++;
			

		}
	}

	public static String obtenerLexema (Token token){
		String lexema = "";
		if (token == null) return "";
		Integer codigoToken = token.getId();
		switch(codigoToken){
			case 1:
			lexema = "if";
			break;
			case 2:
			lexema = "for";
			break;
			case 3:
			lexema = "let";
			break;
			case 4:
			lexema = "int";
			break;
			case 5:
			lexema = "boolean";
			break;
			case 6:
			lexema = "string";
			break;
			case 7:
			lexema = "function";
			break;
			case 8:
			lexema = "return";
			break;
			case 9:
			lexema = "print";
			break;
			case 10:
			lexema = "input";
			break;
			case 11:
			lexema = "id";
			break;
			case 12:
			lexema = "entero";
			break;
			case 13:
			lexema = "cadena";
			break;
			case 14:
			lexema = "=";
			break;
			case 15:
			lexema = "%=";
			break;
			case 16:
			lexema = "(";
			break;
			case 17:
			lexema = ")";
			break;
			case 18:
			lexema = "{";
			break;
			case 19:
			lexema = "}";
			break;
			case 20:
			lexema = ",";
			break;
			case 21:
			lexema = ";";
			break;
			case 22:
			lexema = "-";
			break;
			case 23:
			lexema = "!";
			break;
			case 24:
			lexema = "<";
			break;
			default:
			lexema = "$";
			break;

		}
		return lexema;
	}

	public static void rellenarConsecuentes (){

		consecuentes[0] = "S1:1"; consecuentes[1] = "S:2"; consecuentes[2] = "S:2"; consecuentes[3] = "S:0";
		consecuentes[4] = "A:3"; consecuentes[5] = "A:0"; consecuentes[6] = "B:5"; consecuentes[7] = "B:5";
		consecuentes[8] = "B:1"; consecuentes[9] = "B:11"; consecuentes[10] = "B1:2"; consecuentes[11] = "B1:0";
		consecuentes[12] = "C:5"; consecuentes[13] = "C:4"; consecuentes[14] = "C:4"; consecuentes[15] = "C:5";
		consecuentes[16] = "C:5"; consecuentes[17] = "C:3"; consecuentes[18] = "D:0"; consecuentes[19] = "D:3";
		consecuentes[20] = "D:3"; consecuentes[21] = "F:3"; consecuentes[22] = "F1:3"; consecuentes[23] = "F2:3";
		consecuentes[24] = "F3:3"; consecuentes[25] = "G:2"; consecuentes[26] = "G:0"; consecuentes[27] = "H:1";
		consecuentes[28] = "H:0"; consecuentes[29] = "K:4"; consecuentes[30] = "K:0"; consecuentes[31] = "L:2";
		consecuentes[32] = "L:0"; consecuentes[33] = "Q:3"; consecuentes[34] = "Q:0"; consecuentes[35] = "T:1";
		consecuentes[36] = "T:1"; consecuentes[37] = "T:1"; consecuentes[38] = "X:1"; consecuentes[39] = "X:0";
		consecuentes[40] = "E:3"; consecuentes[41] = "E:1"; consecuentes[42] = "E1:3"; consecuentes[43] = "E1:1";
		consecuentes[44] = "E3:2"; consecuentes[45] = "E3:3"; consecuentes[46] = "E3:1"; consecuentes[47] = "E3:4";
		consecuentes[48] = "E3:1"; consecuentes[49] = "E3:1";
		
		/*
		consecuentes[1] = "S1:1"; consecuentes[2] = "S:2"; consecuentes[3] = "S:2"; consecuentes[4] = "S:0";
		consecuentes[5] = "A:3"; consecuentes[6] = "A:0"; consecuentes[7] = "B:5"; consecuentes[8] = "B:5";
		consecuentes[9] = "B:1"; consecuentes[10] = "B:11"; consecuentes[11] = "B1:2"; consecuentes[12] = "B1:0";
		consecuentes[13] = "C:5"; consecuentes[14] = "C:4"; consecuentes[15] = "C:4"; consecuentes[16] = "C:5";
		consecuentes[17] = "C:5"; consecuentes[18] = "C:3"; consecuentes[19] = "D:0"; consecuentes[20] = "D:4";
		consecuentes[21] = "D:4"; consecuentes[22] = "F:3"; consecuentes[23] = "F1:3"; consecuentes[24] = "F2:3";
		consecuentes[25] = "F3:3"; consecuentes[26] = "G:2"; consecuentes[27] = "G:0"; consecuentes[28] = "H:1";
		consecuentes[29] = "H:0"; consecuentes[30] = "K:4"; consecuentes[31] = "K:0"; consecuentes[32] = "L:2";
		consecuentes[33] = "L:0"; consecuentes[34] = "Q:3"; consecuentes[35] = "Q:0"; consecuentes[36] = "T:1";
		consecuentes[37] = "T:1"; consecuentes[38] = "T:1"; consecuentes[39] = "X:1"; consecuentes[40] = "X:0";
		consecuentes[41] = "E:3"; consecuentes[42] = "E:1"; consecuentes[43] = "E1:3"; consecuentes[44] = "E1:1";
		consecuentes[45] = "E3:2"; consecuentes[46] = "E3:3"; consecuentes[47] = "E3:1"; consecuentes[48] = "E3:4";
		consecuentes[49] = "E3:1"; consecuentes[50] = "E3:1";
		*/
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
		rellenarConsecuentes();
		agt.printTable();
		try {
			fr = new FileReader("./data/inputAS.txt");
			bf = new BufferedReader(fr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			car = (char)bf.read();
			FichToken = new FileWriter(new File("./data/tokens.txt"));
			FichTablaSimb = new FileWriter(new File("./data/TS.txt"));
			FichError = new FileWriter(new File("./data/error.txt"));
			FichParse = new FileWriter(new File("./data/parse.txt"));
			FichParse.write("Ascendente ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ASintactico();
		imprimirTablaSimbolos();
		try {
			fr.close();
			bf.close();
			FichToken.close();
			FichTablaSimb.close();
			FichError.close();
			FichParse.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
		


	}
}
