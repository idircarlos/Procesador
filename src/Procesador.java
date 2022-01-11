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
	private static BufferedReader bf;
	private static FileReader fr;
	private static FileWriter FichToken;
	private static FileWriter FichTablaSimb;
	private static FileWriter FichError;
	private static FileWriter FichParse;
	private static Map<String,Integer> palRes;
	private static boolean GlobalActiva = true;
	private static Map<String, Map<String, Object>> TGlobal = new HashMap<String, Map<String, Object>>();
	private static Map<String, Map<String, Object>> TLocal = new HashMap<String, Map<String, Object>>();
	private static Map<Integer, String> ListTG = new HashMap<Integer, String>();
	private static Map<Integer, String> ListTL = new HashMap<Integer, String>();
	private static String[] consecuentes = new String[51];
	private static boolean zonaDecl = false;
	private static final int EOF=65535;
	private static int posTG=0;
	private static int nLinea=1;
	private static Stack<Map<String,Object>> pilaAtributos = new Stack<Map<String,Object>>();
	private static int despG = 0;
	private static int despL = 0;
	private static int nEtiqueta = 1;
	private static ArrayList<ParFunc> bufferTS = new ArrayList<ParFunc>();
	private static String funcActual = null;
	private static boolean hayError = false;
	private static int contador = 0;
	
	public static Token ALexico() {
		int estadoActual=0; //Estado actual del automata
		String valor =""; //Almacen de cadenas de numeros
		String cadena=""; //Almacen de cadenas de caracteres
		Token token=null; //Token generado
		String acciones = null;

		while(estadoActual<=6 && !hayError) {
			// para usar cualquier caracter en un comentario
			if (estadoActual == 6){
				while(car != '\n' && car != EOF){car = leer();}
				estadoActual = 0;
		   	}
			if(car==EOF) {
				return new Token(EOF,"$");
			}
			if(car == '\n') {
				nLinea++;
			}
			System.out.println("FUNCIÓN");
			System.out.println(car);
			
			ParMT par = afd.getPar(estadoActual,car);	//
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
				boolean existente;
				Map<String,Object> atributos = new HashMap<String,Object>();
				if (pos != null){
					token = GenToken(pos,"");
				}
				else{
					System.out.println("variable: " + contador);
					pos = posTG;
					if(zonaDecl) { //Zona declaracion activa 
						 //Mira si existe en una tabla o no
						System.out.println("GlobalActiva y zonadecl activa: " + GlobalActiva);
						if(GlobalActiva){ //Si esta la global activa 
							existente = TGlobal.containsKey(cadena);
						}
						else { //Esta la local activa
							existente = TLocal.containsKey(cadena);
						}
						if (existente){ //Esto significa que existe la variable en dicho ambito ya declarada
							GenerarErrores(6, "Ya existe el identificador '"+cadena+"' en este ámbito\n"); // identificador ya declarado previamente
						}
						else{ //Sino es que no existe y hay que crearla (Lexico solo introduce el lexema)
							atributos.put("lexema", cadena);
							atributos.put("tipo", "");
							atributos.put("desp", "");
							atributos.put("numParam", "");
							atributos.put("tipoParam",new ArrayList<String>());
							atributos.put("tipoRet","");
							atributos.put("etiq", "");
							atributos.put("pos", posTG);
							if(GlobalActiva){ //Si esta la Global Activa entonces se vuelca en la global
								TGlobal.put(cadena, atributos); //Se ponen los atributos (solo el lexema) en la tabla global
								ListTG.put(posTG, cadena); //Se almacena la posicion en la TS en una lista auxiliar
								posTG++;
							}
							else{
								TLocal.put(cadena, atributos);
								ListTL.put(posTG, cadena);
								posTG++;
							}
						}
					}
					else{ //No esta la zona de declaracion activada
						System.out.println("GlobalActiva y zonadecl desactivada: " + GlobalActiva);
						if(GlobalActiva){ // Se busca en la global
						    //	System.out.println("CADENA: " + cadena);
							//System.out.println(TGlobal);
							existente = TGlobal.containsKey(cadena);
							//System.out.println("TGAT   La variable "+ cadena + " existe en la tabla global -> "+ existente);
							if(existente){
								//System.out.println("estamos dentro");
								
								if (TGlobal.get(cadena).get("pos") != null && !TGlobal.get(cadena).get("pos").equals("") ){
									pos = Integer.parseInt(""+TGlobal.get(cadena).get("pos"));
								}
								else {
									pos = null;
								}
								//System.out.println("pos" + pos);
							} 
						} 
						else{ //Se busca en local y luego en la global si es que no existia en la local
							existente = TLocal.containsKey(cadena);
							System.out.println("TLocal: " + TLocal);
							//System.out.println("TLAT   La variable "+ cadena + " existe en la tabla local -> "+ existente);
							if(!existente){
								existente = TGlobal.containsKey(cadena);
								//System.out.println("TLAT   La variable "+ cadena + " existe en la tabla global -> "+ existente);
								if(existente){
									pos = (Integer)TGlobal.get(cadena).get("pos"); //Existe en la global
								}
							}
							else{ //Existe en la local (existente = true) para la local
								//System.out.println(TLocal);
								//System.out.print("A VER SI ES NULL ");
								pos = (Integer)TLocal.get(cadena).get("pos"); //Existe en la local
								//System.out.println("number " + pos);
								//System.out.println(TGlobal);
								//System.out.println(TLocal);
							}
						}
						if(!existente){ //No existe el identificador y no estamos en zona de declaracion por lo que es un entero
						//Hay que anadirlo a la global
							atributos.put("lexema", cadena);
							atributos.put("tipo", "ent");
							atributos.put("desp", despG);
							atributos.put("numParam", "");
							atributos.put("tipoParam",new ArrayList<String>());
							atributos.put("tipoRet","");
							atributos.put("etiq", "");
							atributos.put("pos", pos);
							ListTG.put(posTG,cadena);
							despG++;
							posTG++;
							TGlobal.put(cadena, atributos);
							
						}
					}
					//System.out.println("ESTAMOS AQUI   "+pos);
					token = GenToken(11, pos);
					contador++;
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
		String regla; //Regla utilizada (se vuelca en el fichero parse)
		boolean encontrado;
		
		Stack<String> pilaAsc = new Stack<String>();
		pilaAtributos.push(new HashMap<String,Object>());
		pilaAsc.push(estado);
		while (!hayError){
			//System.out.println("-------------------ITER " + iter + "--------------------");
			//System.out.println( "1\t" +pilaAsc);
			//System.out.println("Esatdo\t" +estado);
			//System.out.println("Simbolo\t" +simbolo);
			accion = agt.getAccion(estado, simbolo);
			System.out.println("ACCION\t" +accion);
			if(accion!=null){
				operacion = accion.charAt(0); // Miramos si es desplazamiento ('d') regla ('r') aceptado ('acc') o error (' ')
			}
			else {
				//System.out.println("No aceptado");
				//System.out.println("Error");
				ErrorSintactico(estado, simbolo);
				hayError = true;
				break;
			}
			if(operacion == 'd'){
				estado = accion.substring(1,accion.length());
				//System.out.println("2\t" + pilaAsc);
				pilaAsc.push(simbolo);
				Map<String,Object> atributos = new HashMap<String,Object>();
				if (simbolo.equals("id")){
					//System.out.println(token);
					Integer id = (Integer)token.getVal();
					//System.out.println("NECESITAMOS QUE AQUI NO DE NULL " + id);
					//System.out.println(GlobalActiva);
					if (!GlobalActiva){ //Si no esta activa la tabla
						encontrado = TLocal.containsKey(ListTL.get(id));
						if (encontrado){
							//System.out.println("dentro");
							atributos = TLocal.get(ListTL.get(id));
							//System.out.println("COSAS");
							//System.out.println(ListTL);
							//System.out.println(TLocal);
							//System.out.println(atributos);
						}
						else if(TGlobal.containsKey(ListTG.get(id))){
							//System.out.println("medio");
							atributos = TGlobal.get(ListTG.get(id));
						}
						else {
							//System.out.println("fuera");
							atributos.put("lexema",ListTL.get(id));
						}
					}
					else if (TGlobal.containsKey(ListTG.get(id))){
						//System.out.println("CHIVATON1");
						atributos = TGlobal.get(ListTG.get(id));
						//System.out.println(atributos);
					}
					else {
						//System.out.println("CHIVATON2");
						atributos.put("lexema", ListTG.get(id));
					}
				}
				else {
					//System.out.println("CHIVATON3");
					atributos.put("lexema", simbolo);
				}
				pilaAtributos.push(atributos);
				//System.out.println("3\t" + pilaAsc);
				pilaAsc.push(estado);
				//System.out.println("4\t" + pilaAsc);
				pilaAtributos.push(new HashMap<String,Object>());
				if (simbolo.equals("function") || simbolo.equals("let")){
					ASemantico("-2");
				}
				else if (simbolo.equals(";") || simbolo.equals("=")){
					ASemantico("-1");
				}
				token = ALexico();
				//System.out.println("token\t"+token);
				simbolo = obtenerLexema(token);
			}
			else if (operacion == 'r'){
				regla = accion.substring(1, accion.length());
				String[] linea = consecuentes[Integer.parseInt(regla)].split(":");
				//System.out.println(Arrays.toString(linea));
				k = Integer.parseInt(linea[1]);
				antecedente = linea[0];
				//System.out.println("regla " +regla);
				//System.out.println("Antecedente "+antecedente);
				//System.out.println("k " + k);
				try{
					FichParse.write(regla+" ");
				}
				catch (IOException e){

				}
				for (int i = 1; i <= 2*k; i++){
					pilaAsc.pop();
				}
				estado = agt.getAccion(pilaAsc.lastElement(), antecedente);
				//System.out.println("5\t" + pilaAsc);
				pilaAsc.push(antecedente);
				//System.out.println("6\t" + pilaAsc);
				pilaAsc.push(estado);
				//System.out.println("7\t" + pilaAsc);
				ASemantico(regla);
				
			}
			else if(operacion == 'a'){
				//System.out.println("Aceptado");
				//imprimirPilaSimbolos();
				//System.out.println();
				//System.out.println(TGlobal);
				//System.out.println();
				//System.out.println(TLocal);
				return;
			}
			else {
				//System.out.println("Error");
				ErrorSintactico(estado, simbolo);
			}
		}
	}
	/**
	 * 0: lexema
	 * 1: tipo
	 * 2: desp
	 * 3: numParam
	 * 4: tipoParam (ARRAY)
	 * 5: tipoRet
	 * 6: etiq
	 * 	 
	 **/
	@SuppressWarnings("unchecked")
	public static void ASemantico (String regla){
		
		if(regla.equals("-2")) {
			zonaDecl = true;
			return;
		}
		
		else if(regla.equals("-1")) {
			zonaDecl = false;
			return;
		}
		
		
		int cima = pilaAtributos.size()-1;

		String[] linea = consecuentes[Integer.parseInt(regla)].split(":");
		int k = Integer.parseInt(linea[1]);
		String antecedente = linea[0];
		
		Map<String,Object> atributos = new HashMap<String,Object>();
		Map<String,Object> atributosIDS = new HashMap<String,Object>();
		String [] atrs = {"lexema","tipo","desp","tipoParam","tipoRet","etiq","pos","numParam"};
		for (int i = 0; i < atrs.length; i++){
			if (atrs[i].equals("tipoParam")){
				atributos.put(atrs[i], new ArrayList<String>());
				atributosIDS.put(atrs[i], new ArrayList<String>());
			}
			else if (atrs[i].equals("numParam")){
				atributos.put(atrs[i], 0);
				atributosIDS.put(atrs[i], 0);
			}
			else {
				atributos.put(atrs[i], "");
				atributosIDS.put(atrs[i], "");
			}
		}
				

		atributos.put("lexema", antecedente);
		String id;
		
		int ancho;
		//imprimirPilaSimbolos();
		switch(Integer.parseInt(regla)){
			case 0: //NO PUEDE TENER ERRORES 
				TGlobal.clear(); //Borrar contenido de Tabla Global
			break;

		    case 1:
				if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_vacio")){
					atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				} 
				else if(pilaAtributos.get(cima-3).get("tipo").equals("tipo_error") ||
						pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
								atributos.put("tipo", "tipo_error");
								//No error
				}
				else{
					atributos.put("tipo", "tipo_ok");
				}
				break;

			case 2:
				if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_vacio")){
					atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				}
				else if(pilaAtributos.get(cima-3).get("tipo").equals("tipo_error") ||
						pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
								atributos.put("tipo", "tipo_error");
								//No error
				}
				else{
					atributos.put("tipo", "tipo_ok");
				}
				break;
			
			case 3:  //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			
			case 4:
				Map<String,Object> map = new HashMap<>();
				id = (String) pilaAtributos.get(cima-3).get("lexema");
				String tipoVariable = (String) pilaAtributos.get(cima-5).get("tipo");
				map.put("tipo",tipoVariable);
				map.put("desp", despL);	
				map.put("tipoParam", new ArrayList<String>());
				map.put("pos", pilaAtributos.get(cima-3).get("pos"));
				map.put("lexema", id);
				map.put("numParam", 0);
				TLocal.put(id, map);
				despL = despL + (Integer)pilaAtributos.get(cima-5).get("desp");
				//System.out.println("CONTENIDO" + pilaAtributos.get(cima-1).get("tipoParam"));
				ArrayList<String> auxiliar = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (auxiliar.size() == 0){
					//System.out.println("inicializo a 1");
					atributos.put("numParam", 1);
					ArrayList<String> auxiliar1 = new ArrayList<String>();
					auxiliar1.add((String) pilaAtributos.get(cima-5).get("tipo"));
					atributos.put("tipoParam", auxiliar1);
				}
				else {
					int numParam = Integer.valueOf(""+pilaAtributos.get(cima-1).get("numParam")) + 1;
					//System.out.println("NUM PARAM = " + numParam);
					atributos.put("numParam", numParam);
					ArrayList<String> auxiliar1 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
					auxiliar1.add((String) pilaAtributos.get(cima-5).get("tipo"));
					atributos.put("tipoParam", auxiliar1);
				}
				break;

			case 5: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			case 6:
				ancho = (Integer) pilaAtributos.get(cima-7).get("desp");
				id = (String) pilaAtributos.get(cima-5).get("lexema");

				atributosIDS.put("lexema", id);
				atributosIDS.put("tipo", (String) pilaAtributos.get(cima-7).get("tipo"));
				atributosIDS.put("pos", pilaAtributos.get(cima-5).get("pos"));
				if(GlobalActiva){
					atributosIDS.put("desp", despG);
					//System.out.println("NECESITAMOS QUE NO DE NULL " + id);
					//imprimirPilaSimbolos();
					TGlobal.put(id, atributosIDS);
					despG = despG + ancho;
				}
				else{
					atributosIDS.put("desp", despL);
					TLocal.put(id, atributosIDS);
					despL = despL + ancho;
				}

				pilaAtributos.set(cima-5, atributosIDS);
				//imprimirPilaSimbolos();
				if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_vacio") || 
					pilaAtributos.get(cima-5).get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
						atributos.put("tipo", "tipo_ok");
				}
				else{
					atributos.put("tipo", "tipo_error");
					GenerarErrores(21, "Se esperaba un tipo de dato '" + pilaAtributos.get(cima-5).get("tipo") + "' pero se encontró '" + pilaAtributos.get(cima-3).get("tipo") + "'\n");
					
					//ERROR Tipos distintos a ambos lados de la asignacion
				}
				
				break;
			case 7:
				if(pilaAtributos.get(cima-5).get("tipo").equals("log") && pilaAtributos.get(cima-1).get("tipo").equals("tipo_ok")){
					atributos.put("tipo", "tipo_ok");
				}
				else{
					atributos.put("tipo", "tipo_error");
					if(!pilaAtributos.get(cima-5).get("tipo").equals("log")){
						if(pilaAtributos.get(cima-5).get("tipo").equals("tipo_error"))
						GenerarErrores(22, "Se esperaba una expresión lógica pero se encontró una expresión errónea \n");
						//22
						//LA CONDICION NO ERA LOGICA
						else{
							GenerarErrores(22, "Se esperaba una expresión lógica pero se encontró "+pilaAtributos.get(cima-5).get("tipo")+"\n");
						}
					} 
					if(pilaAtributos.get(cima-1).get("tipo").equals("tipo_vacio")){
						//23
						//EL CUERPO DE UN IF NO PUEDE SER VACIO	
						GenerarErrores(23, "El cuerpo de un if no puede ser vacio\n");
					}
					else if(!pilaAtributos.get(cima-1).get("tipo").equals("tipo_ok")){
						GenerarErrores(40, "El cuerpo del if es incorrecto\n");
					}
				}
				
				break;
			case 8: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;
			case 9:
				if(!pilaAtributos.get(cima-3).get("tipo").equals("tipo_error") &&
					!pilaAtributos.get(cima-9).get("tipo").equals("tipo_error")&&
					pilaAtributos.get(cima-13).get("tipo").equals("log") &&
					pilaAtributos.get(cima-17).get("tipo").equals("tipo_ok")){
						atributos.put("tipo", "tipo_ok");
					}
				else{
					atributos.put("tipo", "tipo_error");
					//HACER ERRORES CON ESPECIFCIDAD
					if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")){
						GenerarErrores(24,"El cuerpo del bucle for es incorrecto\n");
					}
					if (pilaAtributos.get(cima-9).get("tipo").equals("tipo_error")){
						GenerarErrores(25,"Fallo en el tercer campo del bucle for: Se esperaba una asigancion correcta\n");
					}
					if (!pilaAtributos.get(cima-13).get("tipo").equals("log")){
						GenerarErrores(26,"Fallo en el segundo campo del bucle for: Se esperaba tipo logico\n");
					}
					if (!pilaAtributos.get(cima-17).get("tipo").equals("tipo_ok")){
						GenerarErrores(27,"Fallo en el primer campo del bucle for: Se esperaba una asigancion correcta\n");
					}
				}
				
				break;
			case 10: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 11: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			case 12:
				id = (String) pilaAtributos.get(cima-9).get("lexema");
				if(!GlobalActiva){
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				pilaAtributos.set(cima-9, atributosIDS);
				//System.out.println("imprimir atributos");
				//imprimirPilaSimbolos();
				if(atributosIDS.get("tipo").equals("func")){
					//String nombreFuncion = (String) pilaAtributos.get(cima-9).get("lexema");
					if(pilaAtributos.get(cima-9).get("tipo").equals("func")){
						Integer numeroParams = (Integer)atributosIDS.get("numParam");
						ArrayList<String> tipoParametros = (ArrayList<String>) atributosIDS.get("tipoParam");
						ArrayList<String> tipoParametrosL = (ArrayList<String>) pilaAtributos.get(cima-5).get("tipoParam");
						//System.out.println(TGlobal);
						if(pilaAtributos.get(cima-9).get("numParam").equals(numeroParams) &&
								tipoParametrosL.equals(tipoParametros)){
								//	System.out.println("NO ERROR");
							atributos.put("tipo", "tipo_ok");
						}
						else if(tipoParametros.size()==0 && tipoParametrosL.size()!=0){
							//System.out.println("PRIMER ERROR");
							atributos.put("tipo", "tipo_error");
							GenerarErrores(30, id);
							
						}
						else if (tipoParametros.size() == tipoParametrosL.size() && !tipoParametros.equals(tipoParametrosL)){
							atributos.put("tipo", "tipo_error");
							Collections.reverse(tipoParametros);
							Collections.reverse(tipoParametrosL);
							GenerarErrores(36, "Los parámetros que se esperaban para la función " + id + " son: " + tipoParametros + " pero se encontraron: " + tipoParametrosL + "\n");
							
						}
						else{
						//	System.out.println("SEGUNDO ERROR");
							atributos.put("tipo", "tipo_error");
							GenerarErrores(31, "El número de parametros de la funcion " + id + " es incorrecto, se esperaban " + pilaAtributos.get(cima-9).get("numParam") + "\n");
							
							//ERROR 6 La llamada a funcion es incorrecta (parametros de llamada incorrectos)
						}
					}
					else{
					//	System.out.println("TERCER ERROR");
						atributos.put("tipo", "tipo_error");
						GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
						
						//ERROR 6 La llamada a funcion es incorrecta
					}
					
				}
				else if (!pilaAtributos.get(cima-9).get("tipo").equals("func")){
					//System.out.println("CUARTO ERROR");
					imprimirPilaSimbolos();
					atributos.put("tipo", "tipo_error");
					GenerarErrores(51, "La función " + id + " no esta declarada\n");
					
					//La funcion no está declarada
				}
				
				break;
			case 13:
				id = (String) pilaAtributos.get(cima-7).get("lexema");
				
				if(!GlobalActiva){
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-7).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-7).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				atributos = new HashMap<>(atributosIDS);
				pilaAtributos.set(cima-7, atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					//gestor de errores
						GenerarErrores(28, "Se esperaba un tipo de dato entero  pero se encontró '" + atributosIDS.get("tipo") + "'\n");
						
				}
				
				break;

			case 14:
				id = (String) pilaAtributos.get(cima-7).get("lexema");
				//System.out.println(TLocal.containsKey(id));
				//System.out.println(GlobalActiva);
				//System.out.println(TGlobal.containsKey(id));
				if(!GlobalActiva){
					
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					//	System.out.println(atributosIDS);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-7).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-7).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
					System.out.println(id);
					System.out.println("atr: " + atributosIDS);
				} 
				pilaAtributos.set(cima-7, atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					GenerarErrores(29, "Se esperaba un tipo de dato '" +atributosIDS.get("tipo") + "' pero se encontró un tipo erróneo\n");
					
					//gestor de errores
				}
				//atributos.put("tipo", atributosIDS.get("tipo"));
				//System.out.println(pilaAtributos);
				//System.out.println(atributosIDS);
				
				break;
				
			case 15:
				if (pilaAtributos.get(cima-5).get("tipo").equals("ent") ||
					pilaAtributos.get(cima-5).get("tipo").equals("cad"))
					atributos.put("tipo", "tipo_ok");
				else {
					atributos.put("tipo", "tipo_error");
					if (pilaAtributos.get(cima-5).get("tipo").equals("log"))
						GenerarErrores(32, "Se esperaba una expresion de tipo 'ent' o 'cad' pero se encontró: '" + pilaAtributos.get(cima-5).get("tipo") + "'\n");
					else {
						GenerarErrores(32, "Se esperaba una expresion de tipo 'ent' o 'cad'\n");
					}
					
					
			}
			 	
				break;

			case 16:
				id = (String) pilaAtributos.get(cima-5).get("lexema");
				
				if(!GlobalActiva){
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("tipoParam", new ArrayList<String>());
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("tipoParam", new ArrayList<String>());
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				} 
				pilaAtributos.set(cima-5, atributosIDS);
				
				if(atributosIDS.get("tipo").equals("ent") || 
				atributosIDS.get("tipo").equals("cad") ){

					atributos.put("tipo", "tipo_ok");
					
				}else{
					atributos.put("tipo", "tipo_error");
					GenerarErrores(33,"Se esperaba un identificador pero se encontró: '" + id + "'\n");
					
				}
				
				break;

			case 17:
				if(!GlobalActiva){
				//	System.out.println("final " + funcActual);
					String tipoRet = (String) TGlobal.get(funcActual).get("tipoRet");
					if(pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")){
						atributos.put("tipo", "tipo_error");
						atributos.put("tipoRet", "tipo_error");
						if(tipoRet.equals("tipo_vacio")){
							GenerarErrores(41,"La función " + funcActual + " devuelve un tipo, no se esperaba retorno\n");

						}else{
							GenerarErrores(41,"La función " + funcActual + " devuelve un tipo erróneo, se esperaba '"+ tipoRet + "'\n");
						}
						
					}
					else if(pilaAtributos.get(cima-3).get("tipo").equals(tipoRet)){
						atributos.put("tipo", "tipo_ok");
						atributos.put("tipoRet", tipoRet);
					}
					else{
						atributos.put("tipo", "tipo_error");
						atributos.put("tipoRet", "tipo_error");
					//	System.out.println(TGlobal.get(funcActual));
						if(tipoRet.equals("tipo_vacio")){
						//gestor de errores
							GenerarErrores(34,"La función " + funcActual + " no devuelve nada, pero se encontró un tipo '" + pilaAtributos.get(cima-3).get("tipo") +"'\n");
						}
						else {
							GenerarErrores(35, "La función " + funcActual + " debe devolver '" +tipoRet +  "' pero se encontró '" + pilaAtributos.get(cima-3).get("tipo") + "'\n");
						}
						//Hacer gestor de errores enviando funActual y tipoRet
					}
				}else{
					atributos.put("tipo", "tipo_ok");
					atributos.put("tipoRet", pilaAtributos.get(cima-3).get("tipo"));
					
				}
				
				break;
				
			case 18://NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;

			case 19:
				id = (String) pilaAtributos.get(cima-5).get("lexema");
				
				if(!GlobalActiva){
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-5).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-5).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				//atributos = new HashMap<>(atributosIDS);
				pilaAtributos.set(cima-5, atributosIDS);
			//	System.out.println("atributos");
			//	System.out.println(atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-1).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					GenerarErrores(28, "Se esperaba un tipo de dato entero  pero se encontró '" + atributosIDS.get("tipo") + "'\n");
					//gestor de errores
				}
				
				break;
			case 20:
				id = (String) pilaAtributos.get(cima-5).get("lexema");
				
				if(!GlobalActiva){
					if(TLocal.containsKey(id)){
						atributosIDS = TLocal.get(id);
					}
					else if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-5).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				}
				else{
					if(TGlobal.containsKey(id)){
						atributosIDS = TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("pos", pilaAtributos.get(cima-5).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
				} 
				//atributos = new HashMap<>(atributosIDS);
				pilaAtributos.set(cima-5, atributosIDS);
			//	System.out.println("atributos");
			//	System.out.println(atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-1).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
				GenerarErrores(29, "Se esperaba un tipo de dato '" +atributosIDS.get("tipo") + "' pero se encontró un tipo erróneo\n");
					//gestor de errores
				}
				
				break;
			case 21: //NO PUEDE TENER ERRORES
				atributos.put("tipo", (String) pilaAtributos.get(cima-1).get("tipo"));
			//	if (!pilaAtributos.get(cima-1).get("tipo").equals("tipo_ok")){
				//	GenerarErrores(41, "Error en el cuerpo de la funcion " + funcActual + "\n");
			//	}
				//BUFFER
				bufferTS.add(new ParFunc(funcActual, new HashMap<String,Map<String,Object>>(TLocal)));
				System.out.println("case 21 " + TLocal);
				TLocal.clear();
				//System.out.println("cambio a null");
				funcActual = null;
				System.out.println("global activa true");
				GlobalActiva = true;
				break;

			case 22: //NO PUEDE TENER ERRORES
				System.out.println("global activa false");
				GlobalActiva = false;
				//System.out.println("antes " + funcActual);
				funcActual = (String) pilaAtributos.get(cima-3).get("lexema");
				//System.out.println("despues " + funcActual);
				despL = 0;
				Map<String,Object> atribAux = TGlobal.get((String) pilaAtributos.get(cima-3).get("lexema"));
			//	System.out.println();
				//imprimirPilaSimbolos();
				//atribAux.put("lexema", funcActual);
				atribAux.put("tipo", "func");
				atribAux.put("tipoParam", new ArrayList<>());
				atribAux.put("tipoRet", (String) pilaAtributos.get(cima-1).get("tipo"));
				atribAux.put("etiq", generarEtiqueta());
				TGlobal.put(funcActual, atribAux);
				//atribAux.put("tipoRet",(String) pilaAtributos.get(cima-1).get("tipo")); 	// PROBABLEMENTE QUITAR NO CAMBIA
				//atribAux.put("info", pilaAtributos.get(cima-3).get("lexema"));				// PROBABLEMENTE QUITAR
				break;

			case 23: //NO PUEDE TENER ERRORES
				//atributos.put("lexema", funcActual);
				Map<String,Object> aux = TGlobal.get(funcActual);
				aux.put("numParam", pilaAtributos.get(cima-3).get("numParam"));
				aux.put("tipoParam", pilaAtributos.get(cima-3).get("tipoParam"));
				TGlobal.put(funcActual, aux);
			//	System.out.println(pilaAtributos.get(cima-3));
				atributos.put("numParam", (Integer)pilaAtributos.get(cima-3).get("numParam"));
				atributos.put("tipoParam", (ArrayList<String>) pilaAtributos.get(cima-3).get("tipoParam"));
				zonaDecl = false;
				break;
			
			case 24: //NO PUEDE TENER ERRORES
				atributos.put("tipo", (String) pilaAtributos.get(cima-3).get("tipo"));
				atributos.put("tipoRet", (String) pilaAtributos.get(cima-3).get("tipoRet"));
				break;

			case 25: //NO PUEDE TENER ERRORES
                if(pilaAtributos.get(cima-3).get("tipo").equals("tipo_error") ||
					pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
						atributos.put("tipo", "tipo_error");
		
				} 
					else {
						atributos.put("tipo",pilaAtributos.get(cima-3).get("tipo")); 
						atributos.put("tipoRet",pilaAtributos.get(cima-3).get("tipoRet"));
					}
					GlobalActiva = true;
					break;
					
			case 26: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;

			case 27: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 28: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			
			case 29: //NO PUEDE TENER ERRORES
				Map<String,Object> map1 = new HashMap<>();
				id = (String) pilaAtributos.get(cima-3).get("lexema");
				String tipoVariable1 = (String) pilaAtributos.get(cima-5).get("tipo");
				map1.put("tipo",tipoVariable1);
				map1.put("desp", despL);
				map1.put("tipoParam", new ArrayList<>());
				map1.put("pos", pilaAtributos.get(cima-3).get("pos"));
				map1.put("lexema", id);
				map1.put("numParam", 0);
				TLocal.put(id, map1);
				despL = despL + (Integer)pilaAtributos.get(cima-5).get("desp");
				ArrayList<String> auxiliar2 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (auxiliar2.size() == 0){
					atributos.put("numParam", 1);
					//System.out.println("inicializo a 1 aqui");
					ArrayList<String> auxiliar1 = new ArrayList<String>();
					auxiliar1.add((String) pilaAtributos.get(cima-5).get("tipo"));
					atributos.put("tipoParam", auxiliar1);
				}
				else {
					int numParam = Integer.valueOf(""+pilaAtributos.get(cima-1).get("numParam")) + 1;
					atributos.put("numParam", numParam);
					ArrayList<String> auxiliar1 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
					auxiliar1.add((String) pilaAtributos.get(cima-5).get("tipo"));
					atributos.put("tipoParam", auxiliar1);
				}
				break;
				
			case 30: //NO PUEDE TENER ERRORES
				atributos.put("tipoParam", new ArrayList<String>());
				break;

			case 31: //NO PUEDE TENER ERRORES
				ArrayList<String> params1 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (params1.size() == 0){
					ArrayList<String> auxParams = new ArrayList<String>();
					auxParams.add((String)pilaAtributos.get(cima-3).get("tipo"));
					atributos.put("tipoParam", auxParams);
					atributos.put("numParam", 1);
				}
				else {
					int numParametros = Integer.valueOf(""+pilaAtributos.get(cima-1).get("numParam")) + 1;
					//System.out.println("NUM PARAM = " + numParametros);
					atributos.put("numParam", numParametros);
					ArrayList<String> parametros = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
					parametros.add((String) pilaAtributos.get(cima-3).get("tipo"));
					atributos.put("tipoParam", parametros);
				}
				break;

			case 32: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			
			case 33: //NO PUEDE TENER ERRORES
				ArrayList<String> params2 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (params2.size() == 0){
					ArrayList<String> auxParams = new ArrayList<String>();
					auxParams.add((String)pilaAtributos.get(cima-3).get("tipo"));
					atributos.put("tipoParam", auxParams);
					atributos.put("numParam", 1);
				}
				else {
					int numParametros = Integer.valueOf(""+pilaAtributos.get(cima-1).get("numParam")) + 1;
					atributos.put("numParam", numParametros);
					ArrayList<String> parametros = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
					parametros.add((String) pilaAtributos.get(cima-3).get("tipo"));
					atributos.put("tipoParam", parametros);
				}
				break;
			case 34: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;
			case 35: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "ent");
				atributos.put("desp", 1);
				break;
			case 36: //NO PUEDE TENER ERRORES
			   	atributos.put("tipo", "cad");
				atributos.put("desp", 64);
				break;
			case 37: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "log");
				atributos.put("desp", 1);
				break;
 
			case 38: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 39: //NO PUEDE TENER ERRORES
				atributos.put("tipo", "tipo_vacio");
				break;

			case 40:
				if(pilaAtributos.get(cima-1).get("tipo").equals("ent") && 
				pilaAtributos.get(cima-5).get("tipo").equals("ent")){
					atributos.put("tipo", "log");
				}else{
					atributos.put("tipo", "tipo_error");
					GenerarErrores(44, "Los dos tipos deben ser enteros pero son: '"+ pilaAtributos.get(cima-1).get("tipo")+ "' y '" +pilaAtributos.get(cima-5).get("tipo") +"'\n");
					//gestor de errores 
				}
				
				break;

			case 41: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				//System.out.println("regla41");
				//imprimirPilaSimbolos();
				//System.out.println("regla41");				
				break;

			case 42:
			//	System.out.println(TGlobal);
			 	if(pilaAtributos.get(cima-1).get("tipo").equals("ent") && 
				pilaAtributos.get(cima-5).get("tipo").equals("ent")){
					atributos.put("tipo", "ent");
				} else{
					atributos.put("tipo", "tipo_error");
						GenerarErrores(44, "Los dos tipos deben ser enteros pero son: '"+ pilaAtributos.get(cima-1).get("tipo")+ "' y '" +pilaAtributos.get(cima-5).get("tipo") +"'\n");
					//gestor de errores 
				}
				
				break;
				
			case 43: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 44:
				//System.out.println("pila simb");
			//	imprimirPilaSimbolos();
				if(pilaAtributos.get(cima-1).get("tipo").equals("log")){
					atributos.put("tipo", "log");
				}else{
					atributos.put("tipo", "tipo_error");
					if(pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
						GenerarErrores(45, "El operador ! solo se puede usar con expresiones de tipo lógico, la expresión es errónea\n");
					}
					else{
					GenerarErrores(45, "El operador ! solo se puede usar con expresiones de tipo lógico pero se encontró una expresión de tipo '" + pilaAtributos.get(cima-1).get("tipo") + "'\n");
					}
					//gestor de errores
				}
				
				break;

			case 45: //NO PUEDE TENER ERRORES
				atributos.put("tipo",pilaAtributos.get(cima-3).get("tipo"));
				break;
			
			case 46: //NO PUEDE TENER ERRORES
			//	System.out.println("*****");
			//	imprimirPilaSimbolos();
			//	System.out.println("*****");
				id = (String) pilaAtributos.get(cima-1).get("lexema");
				//System.out.println("AQIO");
				//imprimirPilaSimbolos();
			//	System.out.println(id+ " SI DA NULL ESTA MAL");
			//	System.out.println(TGlobal);
			//	System.out.println("local " + TLocal);
				boolean encontrado;
				if (!GlobalActiva){	// si no estamos en la global
					encontrado = TLocal.containsKey(id);
					//System.out.println(encontrado+" PRUEBA " + id);
					if(encontrado){
						atributosIDS = TLocal.get(id);
					}else{
						encontrado = TGlobal.containsKey(id);
						if(encontrado){
							atributosIDS = TGlobal.get(id);
						}
						else{
							atributosIDS.put("lexema", id);
				//			System.out.println("LO PONGO A ENTERO");
							atributosIDS.put("tipo", "ent");
							atributosIDS.put("desp", despG);
							atributosIDS.put("tipoParam", new ArrayList<String>());
							atributosIDS.put("pos", pilaAtributos.get(cima-1).get("pos"));
							TGlobal.put(id, atributosIDS);
							despG++;
						}
					}
				}
				else {
					encontrado = TGlobal.containsKey(id);
					if(encontrado){
					//	System.out.println("ESTAMOS AQUI TIO: " + TGlobal);
						atributosIDS=TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
					//	System.out.println("LO PONGO A ENTERO");
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("tipoParam", new ArrayList<String>());
						atributosIDS.put("pos", pilaAtributos.get(cima-1).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
					
				}
				atributos = new HashMap<>(atributosIDS);
				//System.out.println("imprimo pila");
				//imprimirPilaSimbolos();
				pilaAtributos.set(cima-1, atributosIDS);
				//imprimirPilaSimbolos();
				break;
			
			case 47:
				id = (String) pilaAtributos.get(cima-7).get("lexema");
				Integer numParametros = (Integer)pilaAtributos.get(cima-3).get("numParam");
				ArrayList<String> tipoParametros = (ArrayList<String>)pilaAtributos.get(cima-3).get("tipoParam");
				System.out.println(id);
				if (GlobalActiva){
					if (TGlobal.get(id).get("numParam").equals(numParametros) && 
						TGlobal.get(id).get("tipoParam").equals(tipoParametros) ){
							atributos.put("tipo", pilaAtributos.get(cima-7).get("tipoRet"));
							atributos.put("numParam", numParametros);
							atributos.put("tipoParam", tipoParametros);
					}
					else {
						atributos.put("tipo", "tipo_error");
						GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
						
					}
				}
				else {
				//	System.out.println("LA GLOBAL ES  " + id);
				//	System.out.println(numParametros + " , " + tipoParametros);
				//	System.out.println(TGlobal);
					if (TGlobal.get(id).get("numParam").equals(numParametros) && 
						TGlobal.get(id).get("tipoParam").equals(tipoParametros) ){
							atributos.put("tipo", pilaAtributos.get(cima-7).get("tipoRet"));
							atributos.put("numParam", numParametros);
							atributos.put("tipoParam", tipoParametros);
					}
					else {
						atributos.put("tipo", "tipo_error");
						GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
						
					}
				}
				
				break;

			case 48: //NO PUEDE TENER ERRORES
				atributos.put("tipo","ent");
				break;
				
			case 49: //NO PUEDE TENER ERRORES
				atributos.put("tipo","cad");
				break;
		}

		for (int i = 0; i < 2*k; i++){
			pilaAtributos.pop();
		}

		pilaAtributos.push(atributos);
		pilaAtributos.push(new HashMap<String,Object>());
		

		
	}
	private static String generarEtiqueta() {
		nEtiqueta++;
		return "EtiqFuncion" + (nEtiqueta - 1);
	}

	private static void imprimirPilaSimbolos(){
		int j = 1;
		for (int i=pilaAtributos.size()-2; i>=0;i = i - 2){
			System.out.print("cima-"+j +": LEXEMA = "+ pilaAtributos.get(i).get("lexema")+"\t");
			System.out.print("TIPO = "+ pilaAtributos.get(i).get("tipo")+"\t");
			System.out.print("DESP = "+ pilaAtributos.get(i).get("desp")+"\t");
			System.out.print("NUMPARAM = "+ pilaAtributos.get(i).get("numParam")+"\t");
			System.out.print("TIPOPARAM = "+ pilaAtributos.get(i).get("tipoParam")+"\t");
			System.out.println("TIPORET = "+ pilaAtributos.get(i).get("tipoRet")+"\t");
			//System.out.println("INFO = "+ pilaAtributos.get(i).get("info")+"\t");
			j = j + 2;
		}
	}


	private static void ErrorSintactico(String estadoCadena, String simbolo){

		int estado = Integer.valueOf(estadoCadena);
		if (estado == 5 || estado == 7 || estado == 8 || estado == 10 || estado == 11){
			GenerarErrores(14,simbolo); // Se esperaba '(' pero se encontró -> token
		}
		else if (estado == 45 || estado == 47 || estado == 52 || estado == 81 || estado == 90 || estado == 99 || estado == 100 || estado == 103){
			GenerarErrores(7,simbolo); // Se esperaba ')' pero se encontró -> token
		}
		else if (estado == 22 || estado == 69 || estado == 102){
			GenerarErrores(8,simbolo); // Se esperaba '{' pero se encontró -> token
		}
		else if (estado == 67 || estado == 89 || estado == 105){
			GenerarErrores(9,simbolo); // Se esperaba '}' pero se encontró -> token
		}
		else if (estado == 29 || estado == 41 || estado == 61 || estado == 76 || estado == 77){
			GenerarErrores(10,simbolo); // Se esperaba ';' pero se encontró -> token
		}
		else if (estado == 1){
			GenerarErrores(11,simbolo); // Se esperaba final de fichero pero se encontro -> token
		}
		else if (estado == 4 || estado == 91){
			GenerarErrores(12,simbolo); // Se esperaba un tipo de dato (int, string boolean) pero se encontro -> token
		}
		else if (estado == 13 || estado == 16 || estado == 28 || estado == 46 || estado == 98){
			GenerarErrores(13,simbolo); // Se esperaba un id pero se encontro -> token
		}
		else {
			GenerarErrores(20,simbolo);
		}
	}

	private static String obtenerLexema (Token token){
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

	private static void rellenarConsecuentes (){
		
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
					FichError.write("Error léxico (1): Linea "+nLinea+" Error en la generacion del token, no se esperaba este caracter "+datos+"\n");
					hayError = true;
					break;
				case 2:
					FichError.write("Error léxico (2): Linea "+nLinea+" Cadena de mas de 64 caracteres\n");
					hayError = true;
					break;
				case 3:
					FichError.write("Error léxico (3): Linea "+nLinea+" Entero fuera de rango, el numero es mayor de 32767\n");
					hayError = true;
					break;
				case 4:
					FichError.write("Error léxico (4): Linea "+nLinea+" El operador %= no se ha construido correctamente\n");
					hayError = true;
					break;
				case 5:
					FichError.write("Error léxico (5): Linea "+nLinea+" Solo se admite el formato de comentarios // -> // Comentario\n");
					hayError = true;
					break;
				case 6:
					FichError.write("Error semántico (6): Linea "+nLinea+" "+datos);
					hayError = true;
					break;
				
				case 7:
					FichError.write("Error sintáctico (7): Linea "+nLinea+" Se esperaba ) pero se encontró "+datos);
					break;
				case 8:
					FichError.write("Error sintáctico (8): Linea "+nLinea+" Se esperaba { pero se encontró "+datos);
					break;
				case 9:
					FichError.write("Error sintáctico (9): Linea "+nLinea+" Se esperaba } pero se encontró "+datos);
					break;
				case 10:
					FichError.write("Error sintáctico (10): Linea "+nLinea+" Se esperaba ; pero se encontró "+datos);
					break;
				case 11:
					FichError.write("Error sintáctico (11): Linea "+nLinea+" Se esperaba final de fichero pero se encontró "+datos);
					break;
				case 12:
					FichError.write("Error sintáctico (12): Linea "+nLinea+" Se esperaba un tipo de dato (int, boolean, string)\n");
					break;
				case 13:
					FichError.write("Error sintáctico (13): Linea "+nLinea+" Se esperaba un id pero se encontró "+datos);
					break;
				case 14:
					FichError.write("Error sintáctico (14): Linea "+nLinea+" Se esperaba ( pero se encontró "+datos);
					break;
				case 20:
					FichError.write("Error sintáctico (20): Linea "+nLinea+" Token no esperado, se encontró '"+datos+"'\n");
					break;
				case 21:
					FichError.write("Error semántico (21): Linea "+nLinea+ " " + datos);
					break;
				case 22:
					FichError.write("Error semántico (22): Linea "+nLinea+ " " + datos);
					break;
				case 23:
					FichError.write("Error semántico (23): Linea "+nLinea+ " " + datos);
					break;
				case 24:
					FichError.write("Error semántico (24): Linea "+nLinea+ " " + datos);
					break;
				case 25:
					FichError.write("Error semántico (25): Linea "+nLinea+ " " + datos);
					break;
				case 26:
					FichError.write("Error semántico (26): Linea "+nLinea+ " " + datos);
					break;
				case 27:
					FichError.write("Error semántico (27): Linea "+nLinea+ " " + datos);
					break;
				case 28:
					FichError.write("Error semántico (28): Linea "+nLinea+ " " + datos);
					break;
				case 29:
					FichError.write("Error semántico (29): Linea "+nLinea+ " " + datos);
					break;
				case 30:
					FichError.write("Error semántico (30): Linea "+nLinea+" Llamada a a función "+datos+" no recibe parámetros");
					break;
				case 31:
					FichError.write("Error semántico (31): Linea "+nLinea+" "+datos);
					break;
				case 32:
					FichError.write("Error semántico (32): Linea "+nLinea+" "+datos);
					break;
				case 33:
					FichError.write("Error semántico (33): Linea "+nLinea+" "+datos);
					break;
				case 34:
					FichError.write("Error semántico (34): Linea "+nLinea+" "+datos);
					break;
				case 35:
					FichError.write("Error semántico (35): Linea "+nLinea+" "+datos);
					break;
				case 36:
					FichError.write("Error semántico (36): Linea "+nLinea+" "+datos);
					break;
				case 40:
					FichError.write("Error semántico (40): Linea "+(nLinea-1)+" "+datos);
					break;
				case 41:
					FichError.write("Error semántico (41): Linea "+(nLinea-1)+" "+datos);
					break;
				case 44:
					FichError.write("Error semántico (44): Linea "+nLinea+" "+datos);
					break;
				case 45:
					FichError.write("Error semántico (45): Linea "+nLinea+" "+datos);
					break;
				case 50:
					FichError.write("Error semántico (50): Linea "+nLinea+" "+datos);
					break;
				case 51:
					FichError.write("Error semántico (51): Linea "+nLinea+" "+datos);
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

	@SuppressWarnings("unchecked")
	private static void imprimirTablaSimbolos() {
		try {
			Set<String> set1 = TGlobal.keySet();
			System.out.println(TGlobal);
			ArrayList<Pair<String,Integer>> pares = new ArrayList<>();
			for (String c : set1){
				pares.add(new Pair<String,Integer>((String)c,(Integer)TGlobal.get(c).get("pos")));
			}
			System.out.println(pares);
			//pares.remove(0);
			Collections.sort(pares);
			//System.out.println(pares);
			FichTablaSimb.write("TABLA DE SIMBOLOS GLOBAL #1 :\n");
			FichTablaSimb.write("\n");
			for (Pair<String,Integer> par : pares){
				FichTablaSimb.write("* LEXEMA : '" + par.getLeft() + "'\n");
				FichTablaSimb.write("  \tAtributos :\n");
				int j = 1;
				if (TGlobal.get(par.getLeft()).get("tipo").equals("func")){
					FichTablaSimb.write("  \t\t+ tipo: 'func'\n");
					FichTablaSimb.write("  \t\t+ TipoRetorno: '" + TGlobal.get(par.getLeft()).get("tipoRet") + "'\n");
					FichTablaSimb.write("  \t\t+ numParam: " + TGlobal.get(par.getLeft()).get("numParam") + "\n");
					ArrayList<String> tipos = (ArrayList<String>)TGlobal.get(par.getLeft()).get("tipoParam");
					for (int i = (Integer)TGlobal.get(par.getLeft()).get("numParam") - 1; i >= 0; i--){
						FichTablaSimb.write("  \t\t+ TipoParam" + j + ": '" + tipos.get(i) + "'\n");
						j++;
					}
					FichTablaSimb.write("  \t\t+ EtiqFuncion: '" + TGlobal.get(par.getLeft()).get("etiq") + "'\n");
				}
				else {
					FichTablaSimb.write("  \t\t+ tipo: '" + TGlobal.get(par.getLeft()).get("tipo") + "'\n");
					FichTablaSimb.write("  \t\t+ despl: " + TGlobal.get(par.getLeft()).get("desp") + "\n");
				}
			}
			
			FichTablaSimb.write("-----------------------------------------\n");
			for(int i=0; i<bufferTS.size();i++){
				pares.clear();
				Set<String> set = bufferTS.get(i).getTablaSimbolos().keySet();
				for (String c : set){
					pares.add(new Pair<String,Integer>((String)c,(Integer)bufferTS.get(i).getTablaSimbolos().get(c).get("pos")));
				}
				FichTablaSimb.write("TABLA " + bufferTS.get(i).getId() +" #"+(i+2)+" :\n");
				FichTablaSimb.write("\n");
				Collections.sort(pares);
				//System.out.println(pares);
				
				for (Pair<String,Integer> par : pares){
					FichTablaSimb.write("* LEXEMA : '" + par.getLeft() + "'\n");
					FichTablaSimb.write("  \tAtributos :\n");
					FichTablaSimb.write("  \t\t+ tipo: '" + bufferTS.get(i).getTablaSimbolos().get(par.getLeft()).get("tipo") + "'\n");
					FichTablaSimb.write("  \t\t+ despl: " + bufferTS.get(i).getTablaSimbolos().get(par.getLeft()).get("desp") + "\n");
				}
				FichTablaSimb.write("-----------------------------------------\n");
				//FichTablaSimb.write("\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	

	public static void main (String [] args) {
		palRes = new HashMap<>();
		rellenarTPR();
		afd = new DeterministFiniteAutomate();
		agt = new ActionGotoTable();
		rellenarConsecuentes();
		//agt.printTable();
		try {
			fr = new FileReader("./data/inputSemantico.txt");
			bf = new BufferedReader(fr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			car = (char)bf.read();
			FichToken = new FileWriter(new File("./data/tokens.txt"));
			FichTablaSimb = new FileWriter(new File("./data/TS.txt"));
			FichError = new FileWriter(new File("./data/errores.txt"));
			FichParse = new FileWriter(new File("./data/parse.txt"));
			FichParse.write("Ascendente ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ASintactico();
		//imprimirPilaSimbolos();
		//System.out.println(TGlobal);
		if (!hayError && pilaAtributos.get(pilaAtributos.size()-2).get("tipo").equals("tipo_ok")){
			System.out.println("Programa sin erores");
			imprimirTablaSimbolos();
		}
		else {
			System.out.println("Programa con errores. Mirar el fichero \"errores.txt\" para mas información sobre los errores");
		}
		
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
