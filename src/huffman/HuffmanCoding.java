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
        Scanner sc = new Scanner(System.in);
        try {
            sc = new Scanner(new File(fileName), "UTF-8");
            sc.useLocale(Locale.US);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            try {
                sc = new Scanner(new File(fileName), "UTF-8");
                sc.useLocale(Locale.US);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        while (!source.isEmpty() || target.size() != 1) {
            Queue<TreeNode> queue = new Queue<TreeNode>();
            for (int i = 1; i <= 2; i++) {
                double p = source.isEmpty() ? 2 : source.peek().getData().getProbOcc(),
                        q = target.isEmpty() ? 2 : target.peek().getData().getProbOcc();
                if (Double.compare(p, q) <= 0)
                    queue.enqueue(source.dequeue());
                else
                    queue.enqueue(target.dequeue());
            }
            TreeNode l = queue.dequeue(), r = queue.dequeue();
            double s = l.getData().getProbOcc() + r.getData().getProbOcc();
            CharFreq c = new CharFreq(null, s);
            target.enqueue(new TreeNode(c, l, r));
        }
        huffmanRoot = target.dequeue();
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
        for (String s : encodings)
            s = null;
        search(huffmanRoot, "");
    }

    private void search(TreeNode root, String bit) {
        TreeNode ptr = root;
        if (ptr.getLeft() == null) {
            encodings[ptr.getData().getCharacter()] = bit;
            return;
        }
        search(ptr.getLeft(), bit + "0");
        search(ptr.getRight(), bit + "1");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString
     * method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        Scanner sc = new Scanner(System.in);
        try {
            sc = new Scanner(new File(fileName), "UTF-8");
            sc.useLocale(Locale.US);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder("");
        while (hasNextChar(sc))
            sb.append(encodings[nextChar(sc)]);
        writeBitString(encodedFile, sb.toString());
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
        int bytesIndex = 0, byteIndex = 0, currentByte = 0, padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding - 1; i++)
            pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
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

        // Write the array of bytes to the provided file
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
        } catch (IOException e) {
            try {
                File f = new File(decodedFile);
                f.createNewFile();
                out = new PrintWriter(new FileOutputStream(f), true);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        String s = readBitString(encodedFile);
        while (s.length() != 0) {
            TreeNode ptr = huffmanRoot;
            while (ptr.getData().getCharacter() == null) {
                ptr = s.charAt(0) == '0' ? ptr.getLeft() : ptr.getRight();
                s = s.substring(1);
            }
            out.print(ptr.getData().getCharacter());
        }
        out.flush();
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
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes)
                bitString = bitString + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
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
