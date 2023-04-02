package huffman;

public class CharFreq implements Comparable{
  private double probOcc;
  private Character character;
  public CharFreq(Character c, double d){
    character = c;
    probOcc = d;
  }
  public CharFreq(){
    this(null, 0);
  }
  public static int compare(CharFreq cf1, CharFreq cf2){
    return cf1.compareTo(cf2);
  }
  public int compareTo(Object other){
    Double d1 = probOcc, d2 = cf.probOcc;
    return d1.compareTo(d2) != 0 ? d1.compareTo(d2) : character.compareTo(cf.character);
  }
  public Character getCharacter(){
    return character;
  }
  public double getProbOcc(){
    return probOcc;
  }
  public void setCharacter(Character c){
    character = c;
  }
  public void setProbOcc(double d){
    probOcc = d;
  }
}
