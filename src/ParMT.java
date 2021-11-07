/*
 * En esta clase se definen los pares utilizados en la Matriz de Transiciones
 * 
 * Cada par est� compuesto de 2 atributos
 * 		estadoSig = estado al que se va a transitar con un caracter
 * 		acciones = conjunto de acciones sem�nticas a realizar (si no hay estadoSig es error)
 */
public class ParMT {

	
	Integer estadoSig;
	String acciones;
	
	public ParMT() {
		
	}
	public ParMT(int estado, String accion) {
		this.estadoSig=estado;
		this.acciones=accion;
	}
	public Integer getEstadoSig() {
		return estadoSig;
	}
	public String getAcciones() {
		return acciones;
	}
	
	public String toString() {
		return this.getEstadoSig() + ", " + this.getAcciones();
	}
	
	
}
