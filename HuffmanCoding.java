package huffman;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * 
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) {
        fileName = f;
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by
     * frequency
     */
    public void makeSortedList() {
        try {
            Scanner sc = new Scanner(new File(fileName));
            ArrayList<Character> c = new ArrayList<Character>();
            sortedCharFreqList = new ArrayList<CharFreq>();
            int numChars = 0;
            while (hasNextChar(sc)) {
                Character c2 = nextChar(sc);
                if (c.isEmpty() || !c.contains(c2))
                    c.add(c2);
                numChars++;
            }
            for (int i = 0; i < c.size(); i++) {
                sc = new Scanner(new File(fileName));
                int numOcs = 0;
                while (hasNextChar(sc)) {
                    Character c2 = nextChar(sc);
                    if (c.get(i).compareTo(c2) == 0)
                        numOcs++;
                }
                double prob = (double) numOcs / numChars;
                CharFreq cf = new CharFreq(c.get(i), prob);
                sortedCharFreqList.add(cf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(sortedCharFreqList);
        if (sortedCharFreqList.size() == 1) {
            ArrayList<CharFreq> temp = new ArrayList<CharFreq>();
            for (int i = 1; i <= 2; i++)
                temp.add(sortedCharFreqList.get(0));
            temp.set(0, new CharFreq('b', 0.0));
            sortedCharFreqList = temp;
        }
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<TreeNode> source = new Queue<TreeNode>(), target = new Queue<TreeNode>();
        for (CharFreq c : sortedCharFreqList) {
            TreeNode t = new TreeNode();
            t.setData(c);
            source.enqueue(t);
        }
        makeTree2(source, target);
        huffmanRoot = target.dequeue();
    }

    private void makeTree2(Queue<TreeNode> source, Queue<TreeNode> target) {
        while (!source.isEmpty() || target.size() != 1) {
            Queue<TreeNode> t = new Queue<TreeNode>();
            for (int i = 1; i <= 2; i++) {
                double p, q;
                if (source.isEmpty())
                    p = 2.0;
                else
                    p = source.peek().getData().getProbOcc();
                if (target.isEmpty())
                    q = 2.0;
                else
                    q = target.peek().getData().getProbOcc();
                if (Double.compare(p, q) <= 0)
                    t.enqueue(source.dequeue());
                else
                    t.enqueue(target.dequeue());
            }
            TreeNode l = t.dequeue(), r = t.dequeue();
            double s = l.getData().getProbOcc() + r.getData().getProbOcc();
            CharFreq cf = new CharFreq();
            cf.setProbOcc(s);
            TreeNode newNode = new TreeNode(cf, l, r);
            target.enqueue(newNode);
        }
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding.
     * Characters not
     * present in the huffman coding tree should have their spots in the array left
     * null.
     * Set encodings to this array.
     */
    public void makeEncodings() {
        encodings = new String[128];
        for (int i = 0; i < encodings.length; i++)
            encodings[i] = null;
        search(huffmanRoot, encodings, "");
    }

    private void search(TreeNode root, String[] arr, String bit) {
        TreeNode ptr = root;
        if (ptr.getLeft() == null) {
            arr[ptr.getData().getCharacter()] = bit;
            return;
        }
        search(ptr.getLeft(), arr, bit + "0");
        search(ptr.getRight(), arr, bit + "1");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString
     * method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        try {
            Scanner sc = new Scanner(new File(fileName));
            StringBuilder bitString = new StringBuilder();
            while (hasNextChar(sc)) {
                bitString.append(encodings[(int) nextChar(sc)]);
            }
            writeBitString(encodedFile, bitString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename  The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding - 1; i++)
            pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }
            if (c == '1')
                currentByte += 1 << (7 - byteIndex);
            byteIndex++;
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        } catch (Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString
     * method
     * to convert the file into a bit string, then decodes the bit string using the
     * tree, and writes it to a decoded file.
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        PrintWriter out = new PrintWriter(System.out);
        try {
            out = new PrintWriter(new FileOutputStream(new File(decodedFile)), true);
        } catch (Exception e) {
            try {
                File f = new File(decodedFile);
                f.createNewFile();
                out = new PrintWriter(new FileOutputStream(new File(decodedFile)), true);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        String s = readBitString(encodedFile);
        while (s.length() != 0) {
            TreeNode ptr = huffmanRoot;
            while (ptr.getData().getCharacter() == null) {
                if (s.charAt(0) == '0')
                    ptr = ptr.getLeft();
                else
                    ptr = ptr.getRight();
                s = s.substring(1);
            }
            out.print(ptr.getData().getCharacter());
            out.flush();
        }
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);
            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            for (byte b : bytes)
                bitString = bitString + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            for (int i = 0; i < 8; i++)
                if (bitString.charAt(i) == '1')
                    return bitString.substring(i + 1);
            return bitString.substring(8);
        } catch (Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver.
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() {
        return fileName;
    }

    public ArrayList<CharFreq> getSortedCharFreqList() {
        return sortedCharFreqList;
    }

    public TreeNode getHuffmanRoot() {
        return huffmanRoot;
    }

    public String[] getEncodings() {
        return encodings;
    }

    private boolean hasNextChar(Scanner sc) {
        sc.useDelimiter(Pattern.compile(""));
        boolean res = sc.hasNext();
        sc.useDelimiter(Pattern.compile("\\p{javaWhitespace}+"));
        return res;
    }

    private char nextChar(Scanner sc) {
        try {
            sc.useDelimiter(Pattern.compile(""));
            String ch = sc.next();
            assert ch.length() == 1 : "Error detected";
            sc.useDelimiter(Pattern.compile("\\p{javaWhitespace}+"));
            return ch.charAt(0);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("no tokens available");
        }
    }
}
