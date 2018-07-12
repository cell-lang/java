package net.cell_lang;

import java.util.ArrayList;
import java.util.Arrays;


class Builder {
  public static Obj createSet(Obj[] objs) {
    return createSet(objs, objs.length);
  }

  public static Obj createSet(ArrayList<Obj> objs) {
    return createSet(objs.toArray(new Obj[objs.size()]), objs.size());
  }

  public static Obj createSet(Obj[] objs, long count) {
    Miscellanea._assert(objs.length >= count);
    if (count != 0) {
      Obj[] normObjs = Algs.sortUnique(objs, (int) count);
      return new NeSetObj(normObjs);
    }
    else
      return EmptyRelObj.singleton;
  }

  public static Obj createMap(ArrayList<Obj> keys, ArrayList<Obj> vals) {
    return createMap(keys.toArray(new Obj[keys.size()]), vals.toArray(new Obj[vals.size()]));
  }

  public static Obj createMap(Obj[] keys, Obj[] vals) {
    return createMap(keys, vals, keys.length);
  }

  public static Obj createMap(Obj[] keys, Obj[] vals, long count) {
    Obj binRel = createBinRel(keys, vals, count);
    if (!binRel.isEmptyRel() && !binRel.isNeMap()) {
      throw Miscellanea.softFail("Error: map contains duplicate keys");
    }
    return binRel;
  }

  public static Obj createBinRel(ArrayList<Obj> col1, ArrayList<Obj> col2) {
    Miscellanea._assert(col1.size() == col2.size());
    return createBinRel(col1.toArray(new Obj[col1.size()]), col2.toArray(new Obj[col2.size()]), col1.size());
  }

  public static Obj createBinRel(Obj[] col1, Obj[] col2) {
    return createBinRel(col1, col2, col1.length);
  }

  public static Obj createBinRel(Obj[] col1, Obj[] col2, long count) {
    Miscellanea._assert(count <= col1.length & count <= col2.length);

    if (count == 0)
      return EmptyRelObj.singleton;

    Objs12 sorter = new Objs12(col1, col2);
    long[] hashIdxs = sorter.sortedLeftHashIdxPairs(0, (int) count);
    // The returned size is negated if it's not a map
    int size = sorter.clearDuplicatesAndCheckMap(hashIdxs);
    boolean isMap = size >= 0;
    if (size < 0)
      size = -size;

    Obj[] normCol1 = new Obj[size];
    Obj[] normCol2 = new Obj[size];
    int[] hashes = new int[size];

    int writeIdx = 0;
    for (int i=0 ; i < count ; i++) {
      long hashIdx = hashIdxs[i];
      if (hashIdx != -1) {
        int idx = (int) hashIdx;
        normCol1[writeIdx] = col1[idx];
        normCol2[writeIdx] = col2[idx];
        hashes[writeIdx++] = (int) (hashIdx >>> 32);
      }
    }
    Miscellanea._assert(writeIdx == size);

    return isMap ? new NeMapObj(normCol1, normCol2, hashes) : new NonMapNeBinRelObj(normCol1, normCol2, hashes);
  }

  public static Obj createBinRel(Obj obj1, Obj obj2) {
    return new NeMapObj(new Obj[] {obj1}, new Obj[] {obj2}, new int[] {obj1.hashcode()});
  }

  public static Obj createTernRel(ArrayList<Obj> col1, ArrayList<Obj> col2, ArrayList<Obj> col3) {
    Miscellanea._assert(col1.size() == col2.size() && col1.size() == col3.size());
    return createTernRel(col1.toArray(new Obj[col1.size()]), col2.toArray(new Obj[col2.size()]), col3.toArray(new Obj[col3.size()]), col1.size());
  }

  public static Obj createTernRel(Obj[] col1, Obj[] col2, Obj[] col3) {
    return createTernRel(col1, col2, col3, col1.length);
  }

  public static Obj createTernRel(Obj[] col1, Obj[] col2, Obj[] col3, long count) {
    Miscellanea._assert(count <= col1.length && count <= col2.length && count <= col3.length);
    if (col1.length != 0) {
      Obj[][] normCols = Algs.sortUnique(col1, col2, col3, (int) count);
      return new NeTernRelObj(normCols[0], normCols[1], normCols[2]);
    }
    else {
      return EmptyRelObj.singleton;
    }
  }

  public static Obj createTernRel(Obj obj1, Obj obj2, Obj obj3) {
    Obj[] col1 = new Obj[1];
    Obj[] col2 = new Obj[1];
    Obj[] col3 = new Obj[1];
    col1[0] = obj1;
    col2[0] = obj2;
    col3[0] = obj3;
    return new NeTernRelObj(col1, col2, col3);
  }

  public static Obj createTaggedObj(int tag, Obj obj) {
    return new TaggedObj(tag, obj);
  }

  public static Obj createTaggedIntObj(int tag, long value) {
    return new TaggedObj(tag, IntObj.get(value));
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj createSeq(ArrayList<Obj> objs) {
    return createSeq(objs.toArray(new Obj[objs.size()]));
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj createSeq(boolean[] vals) {
    if (vals.length == 0)
      return EmptySeqObj.singleton;

    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = SymbObj.get(vals[i]);
    return ArrayObjs.create(objs);
  }

  public static Obj createSeq(byte[] vals) {
    return vals.length != 0 ? IntArrayObjs.create(vals) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(short[] vals) {
    return vals.length != 0 ? IntArrayObjs.create(vals) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(int[] vals) {
    return vals.length != 0 ? IntArrayObjs.create(vals) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(long[] vals) {
    return vals.length != 0 ? IntArrayObjs.create(vals) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(double[] vals) {
    return vals.length != 0 ? FloatArrayObjs.create(vals) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(Obj[] objs) {
    if (objs.length == 0)
      return EmptySeqObj.singleton;

    int len = objs.length;
    for (int i=0 ; i < len ; i++)
      if (!objs[i].isInt())
        return ArrayObjs.create(objs);

    long[] longs = new long[len];
    for (int i=0 ; i < len ; i++)
      longs[i] = objs[i].getLong();
    return IntArrayObjs.create(longs);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj createSeq(boolean[] vals, int len) {
    if (len == 0)
      return EmptySeqObj.singleton;

    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = SymbObj.get(vals[i]);
    return ArrayObjs.create(objs);
  }

  public static Obj createSeq(byte[] vals, int len) {
    return len != 0 ? IntArrayObjs.create(vals, len) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(short[] vals, int len) {
    return len != 0 ? IntArrayObjs.create(vals, len) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(int[] vals, int len) {
    return len != 0 ? IntArrayObjs.create(vals, len) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(long[] vals, int len) {
    return len != 0 ? IntArrayObjs.create(vals, len) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(double[] vals, int len) {
    return len != 0 ? FloatArrayObjs.create(vals, len) : EmptySeqObj.singleton;
  }

  public static Obj createSeq(Obj[] objs, int len) {
    if (len == 0)
      return EmptySeqObj.singleton;

    for (int i=0 ; i < len ; i++)
      if (!objs[i].isInt())
        return ArrayObjs.create(objs, len);

    long[] longs = new long[len];
    for (int i=0 ; i < len ; i++)
      longs[i] = objs[i].getLong();
    return IntArrayObjs.create(longs);
  }
}
