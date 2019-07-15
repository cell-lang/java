package net.cell_lang;

import java.util.HashMap;
import java.util.ArrayList;


class SymbTable {
  static String[] defaultSymbols = {
    "false",
    "true",
    "void",
    "string",
    "nothing",
    "just",
    "success",
    "failure"
  };

  public static int FalseSymbId   = 0;
  public static int TrueSymbId    = 1;
  public static int VoidSymbId    = 2;
  public static int StringSymbId  = 3;
  public static int NothingSymbId = 4;
  public static int JustSymbId    = 5;
  public static int SuccessSymbId = 6;
  public static int FailureSymbId = 7;

  static ArrayList<String> symbTable = new ArrayList<String>();
  static HashMap<String, Integer> symbMap = new HashMap<String, Integer>();
  static ArrayList<SymbObj> symbObjs = new ArrayList<SymbObj>();

  static {
    int len = defaultSymbols.length;
    for (int i=0 ; i < len ; i++) {
      String str = defaultSymbols[i];
      symbTable.add(str);
      symbMap.put(str, i);
      symbObjs.add(new SymbObj(i));
    }

    for (int i=0 ; i < Generated.embeddedSymbols.length ; i++) {
      int idx = SymbTable.strToIdx(Generated.embeddedSymbols[i]);
      Miscellanea._assert(idx == i);
    }
  }

  public static SymbObj get(int id) {
    return symbObjs.get(id);
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
    int len = Generated.embeddedSymbols.length;
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
