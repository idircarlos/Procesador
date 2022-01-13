public class Pair<X, Y> implements Comparable<Pair<String,Integer>>{
    private X left;
    private Y right;
    
    public Pair(X paramX, Y paramY) {
        this.left = paramX;
        this.right = paramY;
    }
    
    public Pair(Pair<X, Y> paramPair) {
        this.left = paramPair.getLeft();
        this.right = paramPair.getRight();
    }
    
    public X getLeft() {
        return this.left;
    }
    
    public Y getRight() {
        return this.right;
    }
    
    public void setLeft(X paramX) {
        this.left = paramX;
    }
    
    public void setRight(Y paramY) {
        this.right = paramY;
    }

    public String toString() {
        return "Pair(" + this.left + "," + this.right + ")";
      }

    @Override
    public int compareTo(Pair<String, Integer> o) {
        if ((Integer)right < o.right){
            return -1;
        }
        else{
            return 1;
        }
    }
}
