package net.cell_lang;

import java.util.Arrays;


class SlaveTernaryTable {
  public MasterBinaryTable table12;
  public BinaryTable table3;

  // Temporary buffer used throughout the class
  // Does not hold any permanent information
  private final int[] ___buffer = new int[256];

  public SurrObjMapper mapper1, mapper2, mapper3;

  //////////////////////////////////////////////////////////////////////////////

  public boolean insert(int arg1, int arg2, int arg3) {
    int surr12 = table12.surrogate(arg1, arg2);
    Miscellanea._assert(surr12 != 0xFFFFFFFF);
    return table3.insert(surr12, arg3);
  }

  public boolean insert(int surr12, int arg3) {
    return table3.insert(surr12, arg3);
  }

  // public boolean delete(int arg1, int arg2, int arg3) {
  //   int surr12 = table12.surrogate(arg1, arg2);
  //   if (surr12 != 0xFFFFFFFF)
  //     return table3.delete(surr12, arg3);
  //   else
  //     return false;
  // }

  public boolean delete(int surr12, int arg3) {
    return table3.delete(surr12, arg3);
  }

  public void clear() {
    table3.clear();
  }

  //////////////////////////////////////////////////////////////////////////////

  // public SlaveTernaryTable(MasterBinaryTable table12, BinaryTable table3, SurrObjMapper mapper1, SurrObjMapper mapper2, SurrObjMapper mapper3) {
  //   this.table12 = table12;
  //   this.table3  = table3;
  //   this.mapper1 = mapper1;
  //   this.mapper2 = mapper2;
  //   this.mapper3 = mapper3;
  // }

  public SlaveTernaryTable(MasterBinaryTable table12, SurrObjMapper mapper1, SurrObjMapper mapper2, SurrObjMapper mapper3) {
    this.table12 = table12;
    this.table3  = new BinaryTable(null, null);
    this.mapper1 = mapper1;
    this.mapper2 = mapper2;
    this.mapper3 = mapper3;
  }

  public int size() {
    return table3.size();
  }

  public boolean contains(int arg1, int arg2, int arg3) {
    int surr12 = table12.surrogate(arg1, arg2);
    return surr12 != 0xFFFFFFFF && table3.contains(surr12, arg3);
  }

  public boolean contains12(int arg1, int arg2) {
    int surr12 = table12.surrogate(arg1, arg2);
    return surr12 != 0xFFFFFFFF && table3.contains1(surr12);
  }

  public boolean contains13(int arg1, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS
    int count = table12.count1(arg1);
    if (count > 0) {
      int[] args2 = buffer(count);
      int _count = table12.restrict1(arg1, args2);
      Miscellanea._assert(_count == count);
      for (int i=0 ; i < count ; i++) {
        int surr12 = table12.surrogate(arg1, args2[i]);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        if (table3.contains(surr12, arg3))
          return true;
      }
    }
    return false;
  }

  public boolean contains23(int arg2, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS
    int count = table12.count2(arg2);
    if (count > 0) {
      int[] args1 = buffer(count);
      int _count = table12.restrict2(arg2, args1);
      Miscellanea._assert(_count == count);
      for (int i=0 ; i < count ; i++) {
        int surr12 = table12.surrogate(args1[i], arg2);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        if (table3.contains(surr12, arg3))
          return true;
      }
    }
    return false;
  }

  public boolean contains1(int arg1) {
    return table12.contains1(arg1);
  }

  public boolean contains2(int arg2) {
    return table12.contains2(arg2);
  }

  public boolean contains3(int arg3) {
    return table3.contains2(arg3);
  }

  public int lookup12(int arg1, int arg2) {
    int surr12 = table12.surrogate(arg1, arg2);
    if (surr12 == 0xFFFFFFFF)
      throw Miscellanea.softFail();
    return table3.lookup1(surr12);
  }

  public int lookup13(int arg1, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS, AND ALSO CONSIDER AN INDEX
    int count = table12.count1(arg1);
    if (count > 0) {
      int[] args2 = buffer(count);
      int _count = table12.restrict1(arg1, args2);
      Miscellanea._assert(_count == count);
      int arg2 = -1;
      for (int i=0 ; i < count ; i++) {
        int anArg2 = args2[i];
        int surr12 = table12.surrogate(arg1, anArg2);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        if (table3.contains(surr12, arg3))
          if (arg2 == -1)
            arg2 = anArg2;
          else
            throw Miscellanea.softFail();
      }
      if (arg2 != -1)
        return arg2;
    }
    throw Miscellanea.softFail();
  }

  public int lookup23(int arg2, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS, AND ALSO CONSIDER AN INDEX
    int count = table12.count2(arg2);
    if (count > 0) {
      int[] args1 = buffer(count);
      int _count = table12.restrict2(arg2, args1);
      Miscellanea._assert(_count == count);
      int arg1 = -1;
      for (int i=0 ; i < count ; i++) {
        int anArg1 = args1[i];
        int surr12 = table12.surrogate(anArg1, arg2);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        if (table3.contains(surr12, arg3))
          if (arg1 == -1)
            arg1 = anArg1;
          else
            throw Miscellanea.softFail();
      }
      if (arg1 != -1)
        return arg1;
    }
    throw Miscellanea.softFail();
  }

  public int count12(int arg1, int arg2) {
    int surr12 = table12.surrogate(arg1, arg2);
    return surr12 != 0xFFFFFFFF ? table3.count1(surr12) : 0;
  }

  public int count13(int arg1, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS, AND ALSO CONSIDER AN INDEX
    int count13 = 0;
    int count1 = table12.count1(arg1);
    if (count1 > 0) {
      int[] args2 = buffer(count1);
      //## COULD BE MORE EFFICIENT IF WE HAD A MasterBinaryTable.restrict1(int, int[], int[]) HERE
      int _count = table12.restrict1(arg1, args2);
      Miscellanea._assert(_count == count1);
      for (int i=0 ; i < count1 ; i++) {
        int arg2 = args2[i];
        int surr12 = table12.surrogate(arg1, arg2);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        if (table3.contains(surr12, arg3))
          count13++;
      }
    }
    return count13;
  }

  public int count23(int arg2, int arg3) {
    //## HERE WE SHOULD EVALUATE ALL THE POSSIBLE EXECUTION PATHS, AND ALSO CONSIDER AN INDEX
    int count23 = 0;
    int count3 = table3.count2(arg3);
    if (count3 > 0) {
      int[] surrs12 = buffer(count3);
      int _count = table3.restrict2(arg3, surrs12);
      Miscellanea._assert(_count == count3);
      for (int i=0 ; i < count3 ; i++) {
        int surr12 = surrs12[i];
        if (table12.arg2(surr12) == arg2)
          count23++;
      }
    }
    return count23;
  }

  public int count1(int arg1) {
    int fullCount = 0;
    int count1 = table12.count1(arg1);
    if (count1 > 0) {
      int[] args2 = buffer(count1);
      int _count = table12.restrict1(arg1, args2);
      Miscellanea._assert(_count == count1);
      for (int i=0 ; i < count1 ; i++) {
        int surr12 = table12.surrogate(arg1, args2[i]);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        fullCount += table3.count1(surr12);
      }
    }
    return fullCount;
  }

  public int count2(int arg2) {
    int fullCount = 0;
    int count2 = table12.count2(arg2);
    if (count2 > 0) {
      int[] args1 = buffer(count2);
      int _count = table12.restrict2(arg2, args1);
      Miscellanea._assert(_count == count2);
      for (int i=0 ; i < count2 ; i++) {
        int surr12 = table12.surrogate(args1[i], arg2);
        Miscellanea._assert(surr12 != 0xFFFFFFFF);
        fullCount += table3.count1(surr12);
      }
    }
    return fullCount;
  }

  public int count3(int arg3) {
    return table3.count2(arg3);
  }

  public Iter123 getIter() {
    return new Iter123();
  }

  public Iter12 getIter12(int arg1, int arg2) {
    return new Iter12(arg1, arg2);
  }

  public Iter13 getIter13(int arg1, int arg3) {
    // int count1 = table12.count1(arg1);
    // int count3 = table3.count2(arg3);
    // if (count1 <= count3)
      return new Iter13(arg1, arg3);
    // else
    //   return new RevIter13(arg1, arg3);
  }

  public Iter23 getIter23(int arg2, int arg3) {
    // int count2 = table12.count2(arg2);
    // int count3 = table3.count2(arg3);
    // if (count2 <= count3)
      return new Iter23(arg2, arg3);
    // else
    //   return new RevIter23(arg2, arg3);
  }

  public Iter1 getIter1(int arg1) {
    return new Iter1(arg1);
  }

  public Iter2 getIter2(int arg2) {
    return new Iter2(arg2);
  }

  public Iter3 getIter3(int arg3) {
    return new Iter3(arg3);
  }

  //////////////////////////////////////////////////////////////////////////////

  private int[] buffer(int size) {
    return size <= ___buffer.length ? ___buffer : new int[size];
  }

  ////////////////////////////////////////////////////////////////////////////

  public boolean col3IsKey() {
    return table3.col2IsKey();
  }

  public boolean cols12AreKey() {
    return table3.col1IsKey();
  }

  // public boolean cols13AreKey() {
  //   if (table12.col1IsKey() || table3.col2IsKey())
  //     return true;
  //
  // }

  // public boolean cols23AreKey() {
  //
  // }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(SlaveTernaryTable[] tables, int idx1, int idx2, int idx3) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      SlaveTernaryTable table = tables[iT];

      SurrObjMapper mapper1 = table.mapper1;
      SurrObjMapper mapper2 = table.mapper2;
      SurrObjMapper mapper3 = table.mapper3;

      Iter123 it = table.getIter();
      while (!it.done()) {
        objs1[next] = mapper1.surrToObj(it.get1());
        objs2[next] = mapper2.surrToObj(it.get2());
        objs3[next] = mapper3.surrToObj(it.get3());
        next++;
      }
    }
    Miscellanea._assert(next == count);

    Obj[][] cols = new Obj[3][];
    cols[idx1] = objs1;
    cols[idx2] = objs2;
    cols[idx3] = objs3;

    return Builder.createTernRel(cols[0], cols[1], cols[2], count);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final class Iter123 extends TernaryTable.Iter {
    BinaryTable.Iter iter;

    public Iter123() {
      iter = table3.getIter();
    }

    public void next() {
      iter.next();
    }

    public boolean done() {
      return iter.done();
    }

    public int get1() {
      return table12.arg1(iter.get1());
    }

    public int get2() {
      return table12.arg2(iter.get1());
    }

    public int get3() {
      return iter.get2();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter12 extends TernaryTable.Iter {
    BinaryTable.Iter iter;

    public Iter12(int arg1, int arg2) {
      int surr12 = table12.surrogate(arg1, arg2);
      if (surr12 != 0xFFFFFFFF)
        iter = table3.getIter1(surr12);
    }

    public void next() {
      Miscellanea._assert(!done());
      iter.next();
    }

    public boolean done() {
      return iter == null || iter.done();
    }

    public int get1() {
      return iter.get1();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter13 extends TernaryTable.Iter {
    int arg1, arg3;
    BinaryTable.Iter iter;

    public Iter13(int arg1, int arg3) {
      this.arg1 = arg1;
      this.arg3 = arg3;

      iter = table12.getIter1(arg1);
      while (!iter.done()) {
        int arg2 = iter.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        if (table3.contains(surr12, arg3))
          break;
        iter.next();
      }
    }

    public void next() {
      Miscellanea._assert(!done());

      do {
        iter.next();
        int arg2 = iter.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        if (table3.contains(surr12, arg3))
          break;
      } while (!iter.done());
    }

    public boolean done() {
      return iter.done();
    }

    public int get1() {
      return iter.get1();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter23 extends TernaryTable.Iter {
    int arg2, arg3;
    BinaryTable.Iter iter;

    public Iter23(int arg2, int arg3) {
      this.arg2 = arg2;
      this.arg3 = arg3;

      iter = table12.getIter2(arg2);
      while (!iter.done()) {
        int arg1 = iter.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        if (table3.contains(surr12, arg3))
          break;
        iter.next();
      }
    }

    public void next() {
      Miscellanea._assert(!done());

      do {
        iter.next();
        int arg1 = iter.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        if (table3.contains(surr12, arg3))
          break;
      } while (!iter.done());
    }

    public boolean done() {
      return iter.done();
    }

    public int get1() {
      return iter.get1();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter1 extends TernaryTable.Iter {
    int arg1;
    BinaryTable.Iter iter2;
    BinaryTable.Iter iter3;

    public Iter1(int arg1) {
      this.arg1 = arg1;
      iter2 = table12.getIter1(arg1);
      while (!iter2.done()) {
        int arg2 = iter2.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        iter3 = table3.getIter1(surr12);
        if (!iter3.done())
          break;
      }
    }

    public void next() {
      iter3.next();
      while (iter3.done()) {
        iter2.next();
        if (iter2.done())
          break;
        int arg2 = iter2.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        iter3 = table3.getIter1(surr12);
      }
    }

    public boolean done() {
      return iter2.done();
    }

    public int get1() {
      return iter2.get1();
    }

    public int get2() {
      return iter3.get1();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter2 extends TernaryTable.Iter {
    int arg2;
    BinaryTable.Iter iter1;
    BinaryTable.Iter iter3;

    public Iter2(int arg2) {
      this.arg2 = arg2;
      iter1 = table12.getIter2(arg2);
      while (!iter1.done()) {
        int arg1 = iter1.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        iter3 = table3.getIter1(surr12);
        if (!iter3.done())
          break;
      }
    }

    public void next() {
      iter3.next();
      while (iter3.done()) {
        iter1.next();
        if (iter1.done())
          break;
        int arg1 = iter1.get1();
        int surr12 = table12.surrogate(arg1, arg2);
        iter3 = table3.getIter1(surr12);
      }
    }

    public boolean done() {
      return iter1.done();
    }

    public int get1() {
      return iter1.get1();
    }

    public int get2() {
      return iter3.get1();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter3 extends TernaryTable.Iter {
    BinaryTable.Iter iter;

    public Iter3(int arg3) {
      iter = table3.getIter2(arg3);
    }

    public void next() {
      iter.next();
    }

    public boolean done() {
      return iter.done();
    }

    public int get1() {
      return table12.arg1(iter.get1());
    }

    public int get2() {
      return table12.arg2(iter.get1());
    }
  }
}
