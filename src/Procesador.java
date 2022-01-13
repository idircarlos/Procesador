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
			
			ParMT par = afd.getPar(estadoActual,car);	
			if(par!=null) { // Estado final o transicion error
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
					pos = posTG;
					if(zonaDecl) { //Zona declaracion activa 
						 //Mira si existe en una tabla o no
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
						if(GlobalActiva){ // Se busca en la global
							existente = TGlobal.containsKey(cadena);
							if(existente){
								if (TGlobal.get(cadena).get("pos") != null && !TGlobal.get(cadena).get("pos").equals("") ){
									pos = Integer.parseInt(""+TGlobal.get(cadena).get("pos"));
								}
								else {
									pos = null;
								}
							} 
						} 
						else{ //Se busca en local y luego en la global si es que no existia en la local
							existente = TLocal.containsKey(cadena);
							if(!existente){
								existente = TGlobal.containsKey(cadena);
								if(existente){
									pos = (Integer)TGlobal.get(cadena).get("pos"); //Existe en la global
								}
							}
							else{ //Existe en la local (existente = true) para la local
								pos = (Integer)TLocal.get(cadena).get("pos"); //Existe en la local
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
					token = GenToken(11, pos);
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
			accion = agt.getAccion(estado, simbolo);
			if(accion!=null){
				operacion = accion.charAt(0); // Miramos si es desplazamiento ('d') regla ('r') aceptado ('acc') o error (' ')
			}
			else {
				ErrorSintactico(estado, simbolo);
				hayError = true;
				break;
			}
			if(operacion == 'd'){
				estado = accion.substring(1,accion.length());
				pilaAsc.push(simbolo);
				Map<String,Object> atributos = new HashMap<String,Object>();
				if (simbolo.equals("id")){
					Integer id = (Integer)token.getVal();
					if (!GlobalActiva){ //Si no esta activa la tabla
						encontrado = TLocal.containsKey(ListTL.get(id));
						if (encontrado){
							atributos = TLocal.get(ListTL.get(id));
						}
						else if(TGlobal.containsKey(ListTG.get(id))){
							atributos = TGlobal.get(ListTG.get(id));
						}
						else {
							atributos.put("lexema",ListTL.get(id));
						}
					}
					else if (TGlobal.containsKey(ListTG.get(id))){
						atributos = TGlobal.get(ListTG.get(id));
					}
					else {
						atributos.put("lexema", ListTG.get(id));
					}
				}
				else {
					atributos.put("lexema", simbolo);
				}
				pilaAtributos.push(atributos);
				pilaAsc.push(estado);
				pilaAtributos.push(new HashMap<String,Object>());
				if (simbolo.equals("function") || simbolo.equals("let")){
					ASemantico("-2");
				}
				else if (simbolo.equals(";") || simbolo.equals("=")){
					ASemantico("-1");
				}
				token = ALexico();
				simbolo = obtenerLexema(token);
			}
			else if (operacion == 'r'){
				regla = accion.substring(1, accion.length());
				String[] linea = consecuentes[Integer.parseInt(regla)].split(":");
				k = Integer.parseInt(linea[1]);
				antecedente = linea[0];
				try{
					FichParse.write(regla+" ");
				}
				catch (IOException e){

				}
				for (int i = 1; i <= 2*k; i++){
					pilaAsc.pop();
				}
				estado = agt.getAccion(pilaAsc.lastElement(), antecedente);
				pilaAsc.push(antecedente);
				pilaAsc.push(estado);
				ASemantico(regla);
				
			}
			else if(operacion == 'a'){ //Aceptado sintácticamente
				return;
			}
			else {
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
		switch(Integer.parseInt(regla)){
			case 0:
				TGlobal.clear(); //Borrar contenido de Tabla Global
				break;
		    case 1:
				if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_vacio")){
					atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				} 
				else if(pilaAtributos.get(cima-3).get("tipo").equals("tipo_error") ||
						pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
								atributos.put("tipo", "tipo_error");
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
				}
				else{
					atributos.put("tipo", "tipo_ok");
				}
				break;
			
			case 3:  
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
				ArrayList<String> auxiliar = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (auxiliar.size() == 0){
					atributos.put("numParam", 1);
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

			case 5: 
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
					TGlobal.put(id, atributosIDS);
					despG = despG + ancho;
				}
				else{
					atributosIDS.put("desp", despL);
					TLocal.put(id, atributosIDS);
					despL = despL + ancho;
				}

				pilaAtributos.set(cima-5, atributosIDS);
				if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_vacio") || 
					pilaAtributos.get(cima-5).get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
						atributos.put("tipo", "tipo_ok");
				}
				else{
					atributos.put("tipo", "tipo_error");
					if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")){
						GenerarErrores(21, "Se esperaba un tipo de dato '" + pilaAtributos.get(cima-5).get("tipo") + "' pero se encontró una expresion incorrecta\n");
					}
					else {
						GenerarErrores(21, "Se esperaba un tipo de dato '" + pilaAtributos.get(cima-5).get("tipo") + "' pero se encontró '" + pilaAtributos.get(cima-3).get("tipo") + "'\n");
					}
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
						GenerarErrores(22, "La sentencia if esperaba una expresión lógica pero encontró una expresión errónea\n");
						else{
							GenerarErrores(22, "La sentencia if esperaba una expresión lógica pero encontró '"+pilaAtributos.get(cima-5).get("tipo")+"'\n");
						}
					}
				}
				
				break;
			case 8:
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
					if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")){
						//GenerarErrores(24,"El cuerpo del bucle for es incorrecto\n");
					}
					if (pilaAtributos.get(cima-9).get("tipo").equals("tipo_error")){
						GenerarErrores(25,"Fallo en el tercer campo del bucle for: Se esperaba una asiganción correcta\n");
					}
					if (!pilaAtributos.get(cima-13).get("tipo").equals("log")){
						GenerarErrores(26,"Fallo en el segundo campo del bucle for: Se esperaba una expresión lógica\n");
					}
					if (!pilaAtributos.get(cima-17).get("tipo").equals("tipo_ok")){
						GenerarErrores(27,"Fallo en el primer campo del bucle for: Se esperaba una asiganción correcta\n");
					}
				}
				
				break;
			case 10:
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 11: 
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
				if(atributosIDS.get("tipo").equals("func")){
					if(pilaAtributos.get(cima-9).get("tipo").equals("func")){
						Integer numeroParams = (Integer)atributosIDS.get("numParam");
						ArrayList<String> tipoParametros = (ArrayList<String>) atributosIDS.get("tipoParam");
						ArrayList<String> tipoParametrosL = (ArrayList<String>) pilaAtributos.get(cima-5).get("tipoParam");
						if(pilaAtributos.get(cima-9).get("numParam").equals(numeroParams) &&
								tipoParametrosL.equals(tipoParametros)){
							atributos.put("tipo", "tipo_ok");
						}
						else if(tipoParametros.size()==0 && tipoParametrosL.size()!=0){
							atributos.put("tipo", "tipo_error");
							GenerarErrores(30, "Llamada a a función "+id+" no recibe parámetros");
							
						}
						else if (tipoParametros.size() == tipoParametrosL.size() && !tipoParametros.equals(tipoParametrosL)){
							atributos.put("tipo", "tipo_error");
							Collections.reverse(tipoParametros);
							Collections.reverse(tipoParametrosL);
							GenerarErrores(36, "Los parámetros que se esperaban para la función " + id + " son: " + tipoParametros + " pero se encontraron: " + tipoParametrosL + "\n");
							
						}
						else{
							atributos.put("tipo", "tipo_error");
							GenerarErrores(31, "El número de parametros de la funcion " + id + " es incorrecto, se esperaban " + pilaAtributos.get(cima-9).get("numParam") + "\n");
						}
					}
					else{
						atributos.put("tipo", "tipo_error");
						GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
					}
					
				}
				else if (!pilaAtributos.get(cima-9).get("tipo").equals("func")){
					atributos.put("tipo", "tipo_error");
					GenerarErrores(51, "La función " + id + " no esta declarada\n");
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
				if (atributosIDS.get("tipo").equals("ent") && atributosIDS.get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					if (pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")){
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la derecha de la asignación pero encontró una expresión incorrecta\n");
					}
					if(!atributosIDS.get("tipo").equals("ent")){
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la izquierda de la asignación pero encontró: '" + atributosIDS.get("tipo") + "'\n");
					}
					if(!pilaAtributos.get(cima-3).get("tipo").equals("tipo_error")) {
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la derecha de la asignación pero encontró: '" + pilaAtributos.get(cima-3).get("tipo") + "'\n");
					}
				}
				
				break;

			case 14:
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
				pilaAtributos.set(cima-7, atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-3).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					GenerarErrores(29, "Se esperaba un tipo de dato '" +atributosIDS.get("tipo") + "' pero se encontró un tipo erróneo\n");
				}
				break;
				
			case 15:
				if (pilaAtributos.get(cima-5).get("tipo").equals("ent") ||
					pilaAtributos.get(cima-5).get("tipo").equals("cad"))
					atributos.put("tipo", "tipo_ok");
				else {
					atributos.put("tipo", "tipo_error");
					if (pilaAtributos.get(cima-5).get("tipo").equals("log"))
						GenerarErrores(32, "La llamada a print esperaba una expresión de tipo 'ent' o 'cad' pero se encontró: '" + pilaAtributos.get(cima-5).get("tipo") + "'\n");
					else {
						GenerarErrores(32, "La llamada a print esperaba una expresión de tipo 'ent' o 'cad'\n");
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
					GenerarErrores(33,"La llamada a input esperaba un identificador 'ent' o 'cad' pero se encontró 'log': " + id + "\n");
				}
				break;

			case 17:
				if(!GlobalActiva){
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
						if(tipoRet.equals("tipo_vacio")){
							GenerarErrores(34,"La función " + funcActual + " no devuelve nada, pero se encontró un tipo '" + pilaAtributos.get(cima-3).get("tipo") +"'\n");
						}
						else {
							GenerarErrores(35, "La función " + funcActual + " debe devolver '" +tipoRet +  "' pero se encontró '" + pilaAtributos.get(cima-3).get("tipo") + "'\n");
						}
					}
				}else{
					atributos.put("tipo", "tipo_ok");
					atributos.put("tipoRet", pilaAtributos.get(cima-3).get("tipo"));
					
				}
				break;
				
			case 18:
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
				pilaAtributos.set(cima-5, atributosIDS);
				if (atributosIDS.get("tipo").equals("ent") && atributosIDS.get("tipo").equals(pilaAtributos.get(cima-1).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					if (pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")){
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la derecha de la asignación pero encontró una expresión incorrecta\n");
					}
					if(!atributosIDS.get("tipo").equals("ent")){
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la izquierda de la asignación pero encontró: '" + atributosIDS.get("tipo") + "'\n");
					}
					if(!pilaAtributos.get(cima-1).get("tipo").equals("tipo_error")) {
						GenerarErrores(28, "La operación %= esperaba un tipo de dato entero a la derecha de la asignación pero encontró: '" + pilaAtributos.get(cima-1).get("tipo") + "'\n");
					}
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
				pilaAtributos.set(cima-5, atributosIDS);
				if (atributosIDS.get("tipo").equals(pilaAtributos.get(cima-1).get("tipo"))){
					atributos.put("tipo", "tipo_ok");
				}
				else {
					atributos.put("tipo", "tipo_error");
					GenerarErrores(29, "Se esperaba un tipo de dato '" +atributosIDS.get("tipo") + "' pero se encontró un tipo erróneo\n");
				}
				
				break;
			case 21: 
				atributos.put("tipo", (String) pilaAtributos.get(cima-1).get("tipo"));
				bufferTS.add(new ParFunc(funcActual, new HashMap<String,Map<String,Object>>(TLocal)));
				TLocal.clear();
				funcActual = null;
				GlobalActiva = true;
				break;

			case 22: 
				GlobalActiva = false;
				funcActual = (String) pilaAtributos.get(cima-3).get("lexema");
				despL = 0;
				Map<String,Object> atribAux = TGlobal.get((String) pilaAtributos.get(cima-3).get("lexema"));
				atribAux.put("tipo", "func");
				atribAux.put("tipoParam", new ArrayList<>());
				atribAux.put("tipoRet", (String) pilaAtributos.get(cima-1).get("tipo"));
				atribAux.put("etiq", generarEtiqueta());
				TGlobal.put(funcActual, atribAux);
				break;

			case 23: 
				Map<String,Object> aux = TGlobal.get(funcActual);
				aux.put("numParam", pilaAtributos.get(cima-3).get("numParam"));
				aux.put("tipoParam", pilaAtributos.get(cima-3).get("tipoParam"));
				TGlobal.put(funcActual, aux);
				atributos.put("numParam", (Integer)pilaAtributos.get(cima-3).get("numParam"));
				atributos.put("tipoParam", (ArrayList<String>) pilaAtributos.get(cima-3).get("tipoParam"));
				zonaDecl = false;
				break;
			
			case 24: 
				atributos.put("tipo", (String) pilaAtributos.get(cima-3).get("tipo"));
				atributos.put("tipoRet", (String) pilaAtributos.get(cima-3).get("tipoRet"));
				break;

			case 25: 
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
					
			case 26: 
				atributos.put("tipo", "tipo_vacio");
				break;

			case 27: 
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 28: 
				atributos.put("tipo", "tipo_vacio");
				break;
			
			case 29:
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
				
			case 30:
				atributos.put("tipoParam", new ArrayList<String>());
				break;

			case 31:
				ArrayList<String> params1 = (ArrayList<String>) pilaAtributos.get(cima-1).get("tipoParam");
				if (params1.size() == 0){
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

			case 32: 
				atributos.put("tipo", "tipo_vacio");
				break;
			
			case 33: 
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
			case 34: 
				atributos.put("tipo", "tipo_vacio");
				break;
			case 35: 
				atributos.put("tipo", "ent");
				atributos.put("desp", 1);
				break;
			case 36: 
			   	atributos.put("tipo", "cad");
				atributos.put("desp", 64);
				break;
			case 37: 
				atributos.put("tipo", "log");
				atributos.put("desp", 1);
				break;
 
			case 38: 
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 39: 
				atributos.put("tipo", "tipo_vacio");
				break;

			case 40:
				if(pilaAtributos.get(cima-1).get("tipo").equals("ent") && 
				pilaAtributos.get(cima-5).get("tipo").equals("ent")){
					atributos.put("tipo", "log");
				}else{
					atributos.put("tipo", "tipo_error");
					GenerarErrores(44, "Los dos tipos deben ser enteros pero son: '"+ pilaAtributos.get(cima-1).get("tipo")+ "' y '" +pilaAtributos.get(cima-5).get("tipo") +"'\n");
				}
				
				break;

			case 41: 
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));				
				break;

			case 42:
			 	if(pilaAtributos.get(cima-1).get("tipo").equals("ent") && 
				pilaAtributos.get(cima-5).get("tipo").equals("ent")){
					atributos.put("tipo", "ent");
				} else{
					atributos.put("tipo", "tipo_error");
						GenerarErrores(44, "Los dos tipos deben ser enteros pero son: '"+ pilaAtributos.get(cima-1).get("tipo")+ "' y '" +pilaAtributos.get(cima-5).get("tipo") +"'\n");
				}
				break;
				
			case 43: 
				atributos.put("tipo",pilaAtributos.get(cima-1).get("tipo"));
				break;

			case 44:
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
				}	
				break;

			case 45: 
				atributos.put("tipo",pilaAtributos.get(cima-3).get("tipo"));
				break;
			
			case 46: 
				id = (String) pilaAtributos.get(cima-1).get("lexema");
				boolean encontrado;
				if (!GlobalActiva){	// si no estamos en la global
					encontrado = TLocal.containsKey(id);
					if(encontrado){
						atributosIDS = TLocal.get(id);
					}else{
						encontrado = TGlobal.containsKey(id);
						if(encontrado){
							atributosIDS = TGlobal.get(id);
						}
						else{
							atributosIDS.put("lexema", id);
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
						atributosIDS=TGlobal.get(id);
					}
					else{
						atributosIDS.put("lexema", id);
						atributosIDS.put("tipo", "ent");
						atributosIDS.put("desp", despG);
						atributosIDS.put("tipoParam", new ArrayList<String>());
						atributosIDS.put("pos", pilaAtributos.get(cima-1).get("pos"));
						TGlobal.put(id, atributosIDS);
						despG++;
					}
					
				}
				atributos = new HashMap<>(atributosIDS);
				pilaAtributos.set(cima-1, atributosIDS);
				break;
			
			case 47:
				id = (String) pilaAtributos.get(cima-7).get("lexema");
				Integer numParametros = (Integer)pilaAtributos.get(cima-3).get("numParam");
				ArrayList<String> tipoParametros = (ArrayList<String>)pilaAtributos.get(cima-3).get("tipoParam");
				ArrayList<String> tipoParametrosID = (ArrayList<String>)pilaAtributos.get(cima-7).get("tipoParam");
				if (GlobalActiva){
					if (TGlobal.get(id).get("numParam").equals(numParametros) && 
						TGlobal.get(id).get("tipoParam").equals(tipoParametros) ){
							atributos.put("tipo", pilaAtributos.get(cima-7).get("tipoRet"));
							atributos.put("numParam", numParametros);
							atributos.put("tipoParam", tipoParametros);
					}
					else {
						if (tipoParametrosID.size()==0 && tipoParametros.size()!=0){
							atributos.put("tipo", "tipo_error");
							GenerarErrores(30, "Llamada a a función "+id+" no recibe parámetros");
						}
						else if(!TGlobal.get(id).get("numParam").equals(numParametros)){
							atributos.put("tipo", "tipo_error");
							GenerarErrores(31, "El número de parametros de la funcion " + id + " es incorrecto, se esperaban " + pilaAtributos.get(cima-7).get("numParam") + "\n");
						}
						else if(TGlobal.get(id).get("numParam").equals(numParametros) && !tipoParametros.equals(tipoParametrosID)){
							Collections.reverse(tipoParametrosID);
							Collections.reverse(tipoParametros);
							atributos.put("tipo", "tipo_error");
							GenerarErrores(36, "Los parámetros que se esperaban para la función " + id + " son: " + tipoParametrosID + " pero se encontraron: " + tipoParametros + "\n");

						}

						else{
							atributos.put("tipo", "tipo_error");
							GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
						}
						
					}
				}
				else {
					if (TGlobal.get(id).get("numParam").equals(numParametros) && 
						TGlobal.get(id).get("tipoParam").equals(tipoParametros) ){
							atributos.put("tipo", pilaAtributos.get(cima-7).get("tipoRet"));
							atributos.put("numParam", numParametros);
							atributos.put("tipoParam", tipoParametros);
					}
					else {
						if (tipoParametrosID.size()==0  && tipoParametros.size()!=0){
							atributos.put("tipo", "tipo_error");
							GenerarErrores(30, "Llamada a a función "+id+" no recibe parámetros");
						}
						else if(!TGlobal.get(id).get("numParam").equals(numParametros)){
							GenerarErrores(31, "El número de parametros de la funcion " + id + " es incorrecto, se esperaban " + pilaAtributos.get(cima-7).get("numParam") + "\n");
						}
						else if(TGlobal.get(id).get("numParam").equals(numParametros) && !tipoParametros.equals(tipoParametrosID)){
							Collections.reverse(tipoParametrosID);
							Collections.reverse(tipoParametros);
							atributos.put("tipo", "tipo_error");
							GenerarErrores(36, "Los parámetros que se esperaban para la función " + id + " son: " + tipoParametrosID + " pero se encontraron: " + tipoParametros + "\n");

						}

						else{
						atributos.put("tipo", "tipo_error");
						GenerarErrores(50, "La llamada a la función " + id + " es incorrecta\n");
						}
						
					}
				}
				break;

			case 48: 
				atributos.put("tipo","ent");
				break;
				
			case 49: 
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
					FichError.write("Error léxico (6): Linea "+nLinea+" "+datos);
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
					FichError.write("Error semántico (30): Linea "+nLinea+" " + datos);
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
					FichError.write("Error semántico (40): Linea "+nLinea+" "+datos);
					break;
				case 41:
					FichError.write("Error semántico (41): Linea "+nLinea+" "+datos);
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
			ArrayList<Pair<String,Integer>> pares = new ArrayList<>();
			for (String c : set1){
				pares.add(new Pair<String,Integer>((String)c,(Integer)TGlobal.get(c).get("pos")));
			}
			Collections.sort(pares);
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
				
				for (Pair<String,Integer> par : pares){
					FichTablaSimb.write("* LEXEMA : '" + par.getLeft() + "'\n");
					FichTablaSimb.write("  \tAtributos :\n");
					FichTablaSimb.write("  \t\t+ tipo: '" + bufferTS.get(i).getTablaSimbolos().get(par.getLeft()).get("tipo") + "'\n");
					FichTablaSimb.write("  \t\t+ despl: " + bufferTS.get(i).getTablaSimbolos().get(par.getLeft()).get("desp") + "\n");
				}
				FichTablaSimb.write("-----------------------------------------\n");
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
		try {
			fr = new FileReader("./data/input.txt");
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
