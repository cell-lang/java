package net.cell_lang;

import java.util.ArrayList;


class Builder {
  public static Obj createSeq(ArrayList<Obj> objs) {
    return wrapSeq(objs.toArray(new Obj[objs.size()]));
  }

  public static Obj createSeq(Obj[] objs, long count) {
    Miscellanea._assert(objs != null && count <= objs.length);
    for (int i=0 ; i < count ; i++)
      Miscellanea._assert(objs[i] != null);

    Obj[] objsCopy = new Obj[(int) count];
    for (int i=0 ; i < count ; i++)
      objsCopy[i] = objs[i];
    return wrapSeq(objsCopy);
  }

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
      return EmptyRelObj.singleton();
  }

  public static Obj createMap(ArrayList<Obj> keys, ArrayList<Obj> vals) {
    Miscellanea._assert(keys.size() == vals.size());
    return createMap(keys.toArray(new Obj[keys.size()]), vals.toArray(new Obj[vals.size()]), keys.size());
  }

  public static Obj createMap(Obj[] keys, Obj[] vals) {
    return createMap(keys, vals, keys.length);
  }

  public static Obj createMap(Obj[] keys, Obj[] vals, long count) {
    Obj binRel = createBinRel(keys, vals, count);
    if (!binRel.isEmptyRel() && !binRel.isNeMap()) {
      throw Miscellanea.softFail("Error: map contains duplicate keys");
      // BinRelIter iter = binRel.getBinRelIter();
      // //## REMOVE WHEN DONE
      // while (!iter.done()) {
      //   System.out.println(iter.get1().toString());
      //   iter.next();
      // }
      // throw new RuntimeException();
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
    if (count != 0) {
      Obj[][] normCols = Algs.sortUnique(col1, col2, (int) count);
      Obj[] normCol1 = normCols[0];
      if (Algs.sortedArrayHasDuplicates(normCol1))
        return new NeBinRelObj(normCol1, normCols[1], false);
      else
        return new NeHashMapObj(normCol1, normCols[1]);
    }
    else
      return EmptyRelObj.singleton();
  }

  public static Obj createBinRel(Obj obj1, Obj obj2) {
    Obj[] col1 = new Obj[1];
    Obj[] col2 = new Obj[1];
    col1[0] = obj1;
    col2[0] = obj2;
    return new NeBinRelObj(col1, col2, true);
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
      return EmptyRelObj.singleton();
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
    if (obj.isInt())
      return createTaggedIntObj(tag, obj.getLong());
    else
      return new TaggedObj(tag, obj);
  }

  public static TaggedIntObj createTaggedIntObj(int tag, long value) {
    return new TaggedIntObj(tag, value);
  }

  public static Obj buildConstIntSeq(byte[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj buildConstIntSeq(short[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj buildConstIntSeq(int[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj buildConstIntSeq(long[] vals) {
    return IntArrayObjs.create(vals);
  }


  //## IMPLEMENT IMPLEMENT IMPLEMENT

  public static Obj wrapSeq(boolean[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = SymbObj.get(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj wrapSeq(byte[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj wrapSeq(short[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj wrapSeq(int[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj wrapSeq(long[] vals) {
    return IntArrayObjs.create(vals);
  }

  public static Obj wrapSeq(double[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = new FloatObj(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj wrapSeq(Obj[] objs) {
    int len = objs.length;
    for (int i=0 ; i < len ; i++)
      if (!objs[i].isInt())
        return new MasterSeqObj(objs);

    long[] longs = new long[len];
    for (int i=0 ; i < len ; i++)
      longs[i] = objs[i].getLong();
    return IntArrayObjs.create(longs);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj wrapSeq(boolean[] vals, int len) {
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = SymbObj.get(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj wrapSeq(byte[] vals, int len) {
    return IntArrayObjs.create(vals, len);
  }

  public static Obj wrapSeq(short[] vals, int len) {
    return IntArrayObjs.create(vals, len);
  }

  public static Obj wrapSeq(int[] vals, int len) {
    return IntArrayObjs.create(vals, len);
  }

  public static Obj wrapSeq(long[] vals, int len) {
    return IntArrayObjs.create(vals, len);
  }

  public static Obj wrapSeq(double[] vals, int len) {
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = new FloatObj(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj wrapSeq(Obj[] objs, int len) {
    for (int i=0 ; i < len ; i++)
      if (!objs[i].isInt())
        return new MasterSeqObj(objs, len);

    long[] longs = new long[len];
    for (int i=0 ; i < len ; i++)
      longs[i] = objs[i].getLong();
    return IntArrayObjs.create(longs);
  }
}
