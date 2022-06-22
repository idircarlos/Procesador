class DeterministFiniteAutomate {

	private static final int NUM_STATES = 7; //Representamos solo los estados NO finales 0-6
	private static final int NUM_CHAR = 255; //ASCII Extendido
	private ParMT [][] matrix; 

	public DeterministFiniteAutomate() {
		matrix = new ParMT[NUM_STATES][NUM_CHAR];
		for (int i = 0; i < NUM_STATES; i++) {		
			crearPares(i);
		}
	}

	private void crearPares(int state) {
		switch(state) {
		//Recordar las equivalencias entre ASCII y valores num�ricos
		//https://www.asciitable.com/ 
		case 0:
			for(int j = 0; j < NUM_CHAR; j++) {
				if (j >= '0' && j <= '9') {							//Hacia el estado 1 (lee un d�gito)
					matrix[state][j] = new ParMT(1,"S");
				}
				else if ((j >= 'A' && j <= 'Z') || (j >= 'a' && j <= 'z')) { //Hacia el estado 2 (lee una letra -> ser� un identificador)
					matrix[state][j] = new ParMT(2,"X");
				}

				else if (j == '\'') {		 						//Hacia el estado 3 (s�mbolo ' empieza cadena de texto)
					matrix[state][j] = new ParMT(3,"L");
				}
				else if (j == '%') {								//Hacia el estado 4 (s�mbolo %)
					matrix[state][j] = new ParMT(4,"L");
				}
				else if (j == '/') {								//Hacia el estado 5
					matrix[state][j] = new ParMT(5,"L");
				}
				else if (j == '\n' || j == '\t' || j == 32 || j=='\r') {//Hacia el estado 0 (caracter delimitador "blanco,tab,salto linea")
					matrix[state][j] = new ParMT(0,"L");
				}
				else if(j == '=') {
					matrix[state][j] = new ParMT(7,"D");
				}
				else if(j == '(') {
					matrix[state][j] = new ParMT(8,"F");
				}
				else if(j == ')') {
					matrix[state][j] = new ParMT(9,"G");
				}
				else if(j == '{') {
					matrix[state][j] = new ParMT(10,"H");
				}
				else if(j == '}') {
					matrix[state][j] = new ParMT(11,"I");
				}
				else if(j == ',') {
					matrix[state][j] = new ParMT(12,"J");
				}
				else if(j == ';') {
					matrix[state][j] = new ParMT(13,"K");
				}
				else if(j == '-') {
					matrix[state][j] = new ParMT(14,"M");
				}
				else if(j == '!') {
					matrix[state][j] = new ParMT(15,"N");
				}
				else if(j == '<') {
					matrix[state][j] = new ParMT(16,"O");
				}

			}
			break;
		case 1:
			for(int j = 0; j < NUM_CHAR; j++) {
				if (j >= '0' && j <= '9'){
					matrix[state][j] = new ParMT(1,"S");
				}
				else{ //Cualquier otro s�mbolo que no sea un d�gito
					matrix[state][j] = new ParMT(17,"B");
				}
			}
			break;
		case 2:
			for(int j = 0; j < NUM_CHAR; j++) {
				if(j >='A' && j <= 'z'|| j >= '0' && j <= '9' || j=='_') {
					matrix[state][j] = new ParMT(2,"X");
				}
				else {
					matrix[state][j] = new ParMT(18,"A"); 
				}
			}
			break;
		case 3:
			for(int j = 0; j < NUM_CHAR; j++) {
				if (j != '\''){
					matrix[state][j] = new ParMT(3,"X");
				}
				else{
					matrix[state][j] = new ParMT(19,"C");
				}
			}
			break;
		case 4:
			for(int j = 0; j < NUM_CHAR; j++) {
				if(j =='=') {
					matrix[state][j] = new ParMT(20,"E");
				}
			}
			break;
		case 5:
			matrix[state]['/'] = new ParMT(6,"L");
			break;
		case 6:
			for(int j = 0; j < NUM_CHAR; j++) {
				if (j != '\n'){
					matrix[state][j] = new ParMT(6,"L");
				}
				else{
					matrix[state][j] = new ParMT(0,"L");
				}
			}
			break;
		}
	}
	
	public ParMT getPar(Integer estado, Character car) {
		return matrix[estado][car];
	}


	public void imprimirMatrizTransicion() {
		for(int i=0; i < NUM_STATES;i++ ) {
			System.out.println("ESTADO: " + i);
			for(int j=32; j < NUM_CHAR;j++) {
				if(matrix[i][j] == null) {
					System.out.println((char)j+"\t"+"null");
				}
				else{
					System.out.println((char)j+"\t"+matrix[i][j].getEstadoSig()+" "+matrix[i][j].getAcciones());
				}

			}
		}
	}
	


}
