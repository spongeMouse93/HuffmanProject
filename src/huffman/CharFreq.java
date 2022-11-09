package huffman;

public class CharFreq implements Comparable<CharFreq>{
  private Character character;
  private double probOcc;
  
  public CharFreq(Character c, double p) {
        character = c;
        probOcc = p;
  }
  
  public CharFreq(){this(null, 0);}
  
  public int compareTo(CharFreq cf) {
        Double d1 = probOcc, d2 = cf.probOcc;
        if (d1.compareTo(d2) != 0) 
          return d1.compareTo(d2);
        return character.compareTo(cf.character);
  }
  
  public Character getCharacter() { return character; }
  public double getProbOcc() { return probOcc; }
  public void setCharacter(Character c) { character = c; }
  public void setProbOcc(double p) { probOcc = p; }
}
