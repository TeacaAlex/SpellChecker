import java.io.*;
import java.util.*;

/**
  *clasa Pair in care se memoreaza dist de editare, si 
  *cuvantul corectat pentru a putea fi returnate amandoua odata
  */
class Pair {
  public int dist;
  public String word;
  
  Pair(String word, int dist) {
    this.word = word;
    this.dist = dist;
  }
}

class Spell {
  /*
     *Functia primeste calea spre dictionar, si il citeste intr un string
     */
  public static String readFileAsString(String filePath) {
    /* buffer de dimensiune egala cu dimensiunea fisierului */
    byte[] buffer = new byte[(int) new File(filePath).length()];
    try {
      BufferedInputStream f = null;
      f = new BufferedInputStream(new FileInputStream(filePath));
      /* o singura citire in dictionar */
      f.read(buffer);
      f.close();
    }
    catch(Exception e){}
    return new String(buffer);
  }

  /*
     *Functia primeste dictionarul intr-un string si returneaza 
     * un TreeMap care contine pentru o cheie i un HashMap
     * cu toate cuvinte de lungime i
     */
  public static TreeMap createTreeMapfromString(String file) {
    TreeMap tm = new TreeMap();
    String lines[] = file.split("\n");
    for(int i = 0 ; i < lines.length ; i ++){
      String word = lines[i].split(" ")[0];
      String fr = lines[i].split(" ")[1];
      int len = word.length();
      if(tm.containsKey(len)){
        HashMap entry = (HashMap)tm.get(len);
        entry.put(word, fr);
        tm.put(len, entry);
      }
      else{
        HashMap new_entry = new HashMap();
        new_entry.put(word, fr);
        tm.put(len, new_entry);
      }
    }
    return tm;
  }
  
  /*
     *calculeaza minimul a 2 intregi
     */
  public static int minim(int a, int b, int c) {
    int min = a;
    if(b < min)
      min = b;
    if(c < min)
      min = c;
    return min;
  }

  /*
     *primeste 2 stringuri si returneaza distanta de editare
     *dintre ele
     */
  public static int levenshtein(String sRow, String sCol) {
    /* date necesare */
    int RowLen = sRow.length();
    int ColLen = sCol.length();
    int RowIdx;
    int ColIdx;
    char Row_i;
    char Col_j;
    int cost;
    
    /* daca un string este vid, intoarce dimensiunea celuilalt */
    if(RowLen == 0)
      return ColLen;
    if(ColLen == 0)
      return RowLen;
    /* vectori in care se retin distantele levenshtein */
    int v0[] = new int[RowLen + 1];
    int v1[] = new int[RowLen + 1];
    int v[];//vector pentru swap
    v0[0] = 0;
    for(RowIdx = 1 ; RowIdx <= RowLen ; RowIdx ++)
      v0[RowIdx] = RowIdx;
    for(ColIdx = 1 ; ColIdx <= ColLen ; ColIdx ++) {
      /*pentru fiecare caracter din sCol */
      v1[0] = ColIdx;
      Col_j = sCol.charAt(ColIdx -1);
      for (RowIdx = 1; RowIdx <= RowLen; RowIdx++) {
        /* pentru fiecare caracter din sRow */
        Row_i = sRow.charAt(RowIdx - 1);
        if (Row_i == Col_j)//daca sunt egale
          cost = 0;        //cost = 0
        else
          cost = 1;        //cost = 1
        /* calcul minim(v0[i] + 1, v1[i-1]+1, v0[i-1] + cost) */
        int m_min = v0[RowIdx] + 1;
        int b = v1[RowIdx - 1] + 1;
        int c = v0[RowIdx - 1] + cost;
        if(b < m_min)
          m_min = b;
        if(c < m_min)
          m_min = c;
        v1[RowIdx] = m_min;
      }
      /* swap cei 2 vectori pentru a inainta in siruri */
      v = v0;
      v0 = v1;
      v1 = v;

    }
    /*returneaza v0[RowLen] deoarece v0 primeste v1 */
    return v0[RowLen];
  }

  /*
     *primeste un cuvant si returneaza cuvantul corectat
     * din dictionar
     */
  public static Pair spellCheckWord(String s, TreeMap tm) {
    int len = s.length();
    int min = 32; //memorez distanta minima de editare
    int fr = 0; //memorez frecventa
    String word = s;
    /* caut doar in cuvintele de dimensiune egala, mai mica, 
       sau mai mare cu 2
       */
    for(int i = len -2 ; i <= len + 2 ; i ++){
      if(tm.containsKey(i)) {
        HashMap hm = (HashMap)tm.get(i);
        int d;
        Set hs = new HashSet();
        hs = hm.keySet();
        Iterator it = hs.iterator();
        while(it.hasNext()) {
          String current = (String)it.next();
          int fr_cur = Integer.parseInt((String)hm.get(current));
          d = levenshtein(current, s);
          if( d < min || (d == min && ( fr_cur > fr )) || 
             ((d == min && ( fr_cur == fr )) && current.compareTo(word)<0)) {//daca verifica
            min = d;                                                         //conditia
            word = current;                                                  //actualizez
            fr = fr_cur;
          }
        }
      }
    }
    Pair p = new Pair(word, min);
    return p;
  }

  /*
     *functie care primeste un sir de caractere si
     *returneaza sirul de caractere corectat
     */
  public static String spellCheck(String s, TreeMap tm) {
    s = s.replaceAll("\\s+", "");//scoate spatiile albe
    int N = s.length();
    int Q[] = new int[N+1];//memorez abaterea pentru prefix j
    int F[] = new int[N+1];//memorez frecventa pentru prefix j
    String P[] = new String[N+1];//memorez corectia pentru prefix j
    int split, fr;//abatere, frecvente curente
    /* initializari */
    Q[0] = 0;
    F[0] = 0;
    P[0] = "";
    String nou;
    for(int i = 1 ; i <= N ; i ++){
      Q[i] = 1000;
      F[i] = 0;
    }

    for(int i = 0 ; i < N ; i ++) {
      for(int j = i + 1 ; j<= N ; j ++) {
        Pair p = spellCheckWord(s.substring(i, j), tm);//intoarce corectia substring de la i la j
        if(!(s.substring(i, j).equals(p.word) && p.dist == 32)) {//daca s-a putut face corectia
          /* noi calori pentru abatere, frecventa, string corectat */
          split = Q[i] + p.dist;
          fr = F[i] + Integer.parseInt((String)((HashMap)(tm.get(p.word.length()))).get(p.word));
          nou = P[i] + " " + p.word;
          /*daca se verifica conditia se actualizeaza valorile */
          if( split < Q[j] || ((split == Q[j])&&(nou.split(" ").length < P[j].split(" ").length)) ||
           (((split == Q[j])&&(nou.split(" ").length == P[j].split(" ").length))&&(fr > F[j])) ||
           ((((split == Q[j])&&(nou.split(" ").length == P[j].split(" ").length))&&
             (fr == F[j]))&&(nou.compareTo(P[j]) < 0))){
            Q[j] = split;
            P[j] = nou;
            F[j] = fr;
          }
        }
      }
    }
    return P[N].substring(1);//intorc corectia in P[N], fara primul spatiu
  }

  public static void main(String args[]) {
    String s = "";//initializare string
    try {
      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
      s = stdin.readLine();//citeste de la stdin
      stdin.close();
    }
    catch(Exception e){}
    String file = readFileAsString("dict.txt");
    TreeMap tm = createTreeMapfromString(file);
    System.out.println(spellCheck(s, tm));//afiseaza corectia
  }
}
