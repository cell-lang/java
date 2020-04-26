package net.cell_lang;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.Writer;


final class SymbObj extends Obj {
  String string;
  int minPrintedSize;
  int hashcode;


  private SymbObj(int id) {
    data = symbObjData(id);
    extraData = symbObjExtraData();
    Miscellanea._assert(getSymbId() == id);
    string = idxToStr(id);
    minPrintedSize = string.length();

    // Calculating the hash code
    long hcode = 0;
    int len = string.length();
    for (int i=0 ; i < len ; i++)
      hcode = 31 * hcode + string.charAt(i);
    hashcode = Hashing.hashcode64(hcode);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    throw Miscellanea.internalFail(this);
  }

  @Override
  public int hashcode() {
    return hashcode;
  }

  public TypeCode getTypeCode() {
    return TypeCode.SYMBOL;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      writer.write(string);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    return minPrintedSize;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static SymbObj get(int id) {
    return symbObjs.get(id);
  }

  //## THIS COULD BE OPTIMIZED
  public static SymbObj get(boolean b) {
    return get(b ? TrueSymbId : FalseSymbId);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static String[] defaultSymbols = {
    "false",
    "true",
    "void",
    "string",
    "date",
    "time",
    "nothing",
    "just",
    "success",
    "failure"
  };

  public static int FalseSymbId   = 0;
  public static int TrueSymbId    = 1;
  public static int VoidSymbId    = 2;
  public static int StringSymbId  = 3;
  public static int DateSymbId    = 4;
  public static int TimeSymbId    = 5;
  public static int NothingSymbId = 6;
  public static int JustSymbId    = 7;
  public static int SuccessSymbId = 8;
  public static int FailureSymbId = 9;

  private static String[] embeddedSymbols;

  private static ArrayList<String> symbTable = new ArrayList<String>();
  private static HashMap<String, Integer> symbMap = new HashMap<String, Integer>();
  private static ArrayList<SymbObj> symbObjs = new ArrayList<SymbObj>();

  static {
    for (int i=0 ; i < defaultSymbols.length ; i++) {
      String str = defaultSymbols[i];
      symbTable.add(str);
      symbMap.put(str, i);
      symbObjs.add(new SymbObj(i));
    }

    try {
      Class c = Class.forName("net.cell_lang.Generated");
      embeddedSymbols = (String[]) c.getField("embeddedSymbols").get(null);
    }
    catch (ClassNotFoundException e) {
      embeddedSymbols = defaultSymbols;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    for (int i=0 ; i < embeddedSymbols.length ; i++) {
      int idx = strToIdx(embeddedSymbols[i]);
      Miscellanea._assert(idx == i);
    }
  }

  public static int bytesToIdx(byte[] bytes, int len) {
    return strToIdx(new String(bytes, 0, len));
  }

  public static int bytesToIdx(byte[] bytes) {
    return strToIdx(new String(bytes));
  }

  public static int strToIdx(String str) {
    Integer idxObj = symbMap.get(str);
    if (idxObj != null)
      return idxObj;
    int count = symbTable.size();
    if (count < 65535) {
      symbTable.add(str);
      symbMap.put(str, count);
      symbObjs.add(new SymbObj(count));
      return count;
    }
    throw new UnsupportedOperationException();
  }

  public static String idxToStr(int idx) {
    return symbTable.get(idx);
  }

  public static int compSymbs(int id1, int id2) {
    if (id1 == id2)
      return 0;
    int len = embeddedSymbols.length;
    if (id1 < len | id2 < len)
      return id1 < id2 ? 1 : -1;
    String str1 = symbTable.get(id1);
    String str2 = symbTable.get(id2);
    return str1.compareTo(str2) < 0 ? 1 : -1;
  }

  public static int compBools(boolean b1, boolean b2) {
    return b1 == b2 ? 0 : (b1 ? -1 : 1);
  }
}
