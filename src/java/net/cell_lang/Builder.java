package net.cell_lang;

import java.util.ArrayList;


class Builder {
  public static Obj CreateSeq(ArrayList<Obj> objs) {
    return new MasterSeqObj(objs.toArray(new Obj[objs.size()]));
  }

  public static Obj CreateSeq(Obj[] objs, long count) {
    Miscellanea.Assert(objs != null && count <= objs.length);
    for (int i=0 ; i < count ; i++)
      Miscellanea.Assert(objs[i] != null);

    Obj[] objsCopy = new Obj[(int) count];
    for (int i=0 ; i < count ; i++)
      objsCopy[i] = objs[i];
    return new MasterSeqObj(objsCopy);
  }

  public static Obj CreateSet(Obj[] objs) {
    return CreateSet(objs, objs.length);
  }

  public static Obj CreateSet(ArrayList<Obj> objs) {
    return CreateSet(objs.toArray(new Obj[objs.size()]), objs.size());
  }

  public static Obj CreateSet(Obj[] objs, long count) {
    Miscellanea.Assert(objs.length >= count);
    if (count != 0) {
      Obj[] normObjs = Algs.SortUnique(objs, (int) count);
      return new NeSetObj(normObjs);
    }
    else
      return EmptyRelObj.Singleton();
  }

  public static Obj CreateMap(ArrayList<Obj> keys, ArrayList<Obj> vals) {
    Miscellanea.Assert(keys.size() == vals.size());
    return CreateMap(keys.toArray(new Obj[keys.size()]), vals.toArray(new Obj[vals.size()]), keys.size());
  }

  public static Obj CreateMap(Obj[] keys, Obj[] vals, long count) {
    Obj binRel = CreateBinRel(keys, vals, count);
    if (!binRel.IsEmptyRel() && !binRel.IsNeMap()) {
      BinRelIter iter = binRel.GetBinRelIter();
      //## REMOVE WHEN DONE
      while (!iter.Done()) {
        System.out.println(iter.Get1().toString());
        iter.Next();
      }
      throw new RuntimeException();
    }
    return binRel;
  }

  public static Obj CreateBinRel(ArrayList<Obj> col1, ArrayList<Obj> col2) {
    Miscellanea.Assert(col1.size() == col2.size());
    return CreateBinRel(col1.toArray(new Obj[col1.size()]), col2.toArray(new Obj[col2.size()]), col1.size());
  }

  public static Obj CreateBinRel(Obj[] col1, Obj[] col2, long count) {
    Miscellanea.Assert(count <= col1.length & count <= col2.length);
    if (count != 0) {
      Obj[][] norm_cols = Algs.SortUnique(col1, col2, (int) count);
      Obj[] norm_col_1 = norm_cols[0];
      return new NeBinRelObj(norm_col_1, norm_cols[1], !Algs.SortedArrayHasDuplicates(norm_col_1));
    }
    else
      return EmptyRelObj.Singleton();
  }

  public static Obj CreateBinRel(Obj obj1, Obj obj2) {
    Obj[] col1 = new Obj[1];
    Obj[] col2 = new Obj[1];
    col1[0] = obj1;
    col2[0] = obj2;
    return new NeBinRelObj(col1, col2, true);
  }

  public static Obj CreateTernRel(ArrayList<Obj> col1, ArrayList<Obj> col2, ArrayList<Obj> col3) {
    Miscellanea.Assert(col1.size() == col2.size() && col1.size() == col3.size());
    return CreateTernRel(col1.toArray(new Obj[col1.size()]), col2.toArray(new Obj[col2.size()]), col3.toArray(new Obj[col3.size()]), col1.size());
  }

  public static Obj CreateTernRel(Obj[] col1, Obj[] col2, Obj[] col3, long count) {
    Miscellanea.Assert(count <= col1.length && count <= col2.length && count <= col3.length);
    if (col1.length != 0) {
      Obj[][] norm_cols = Algs.SortUnique(col1, col2, col3, (int) count);
      return new NeTernRelObj(norm_cols[0], norm_cols[1], norm_cols[2]);
    }
    else {
      return EmptyRelObj.Singleton();
    }
  }

  public static Obj CreateTernRel(Obj obj1, Obj obj2, Obj obj3) {
    Obj[] col1 = new Obj[1];
    Obj[] col2 = new Obj[1];
    Obj[] col3 = new Obj[1];
    col1[0] = obj1;
    col2[0] = obj2;
    col3[0] = obj3;
    return new NeTernRelObj(col1, col2, col3);
  }

  public static Obj BuildConstIntSeq(byte[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = IntObj.Get(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj BuildConstIntSeq(short[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = IntObj.Get(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj BuildConstIntSeq(int[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = IntObj.Get(vals[i]);
    return new MasterSeqObj(objs);
  }

  public static Obj BuildConstIntSeq(long[] vals) {
    int len = vals.length;
    Obj[] objs = new Obj[len];
    for (int i=0 ; i < len ; i++)
      objs[i] = IntObj.Get(vals[i]);
    return new MasterSeqObj(objs);
  }
}
