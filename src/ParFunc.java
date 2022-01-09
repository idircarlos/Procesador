import java.util.Map;

public class ParFunc {
    private String id;
    private Map<String,Map<String,Object>> tablaSimbolos;

    public ParFunc(String id, Map<String,Map<String,Object>> tablaSimbolos){
        this.id = id;
        this.tablaSimbolos = tablaSimbolos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String,Map<String,Object>> getTablaSimbolos() {
        return tablaSimbolos;
    }

    public void setTablaSimbolos(Map<String,Map<String,Object>> tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
    }

    public String toString(){
        return "<" + id + "," + tablaSimbolos + ">";
    }
}
