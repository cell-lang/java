package net.cell_lang;

import java.util.Arrays;


class Sym12TernaryTable {
  public static final int Empty = 0xFFFFFFFF;

  static final int MinSize = 32;

  int[] flatTuples = new int[3 * MinSize];
  public int count = 0;
  int firstFree = 0;

  public Index index123, index12, index_13_23, index_1_2, index3;

  public ValueStore store12, store3;

  //////////////////////////////////////////////////////////////////////////////

  final int arg1OrNext(int idx) {
    return flatTuples[3 * idx];
  }

  final int arg2OrEmptyMarker(int idx) {
    return flatTuples[3 * idx + 1];
  }

  final int arg3(int idx) {
    return flatTuples[3 * idx + 2];
  }

  final void setEntry(int idx, int arg1, int arg2, int arg3) {
    int offset = 3 * idx;
    flatTuples[offset]   = arg1;
    flatTuples[offset+1] = arg2;
    flatTuples[offset+2] = arg3;

    Miscellanea._assert(arg1OrNext(idx) == arg1);
    Miscellanea._assert(arg2OrEmptyMarker(idx) == arg2);
    Miscellanea._assert(arg3(idx) == arg3);
  }

  final int capacity() {
    return flatTuples.length / 3;
  }

  final void resize() {
    int len = flatTuples.length;
    Miscellanea._assert(3 * count == len);
    int[] newFlatTuples = new int[2 * len];
    Miscellanea.arrayCopy(flatTuples, newFlatTuples, len);
    flatTuples = newFlatTuples;
    int size = len / 3;
    for (int i=size ; i < 2 * size ; i++)
      setEntry(i, i+1, Empty, 0);
  }

  //////////////////////////////////////////////////////////////////////////////

  public Sym12TernaryTable(ValueStore store12, ValueStore store3) {
    for (int i=0 ; i < MinSize ; i++)
      setEntry(i, i+1, Empty, 0);

    index123 = new Index(MinSize);
    index12  = new Index(MinSize);

    this.store12 = store12;
    this.store3 = store3;
  }

  public int size() {
    return count;
  }

  public void insert(int arg1, int arg2, int arg3) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }

    if (contains(arg1, arg2, arg3))
      return;

    // Increasing the size of the table if need be
    if (firstFree >= capacity()) {
      resize();
      index123 = null;
      index12 = null;
      index_13_23 = null;
      index_1_2 = null;
      index3 = null;
      buildIndex123();
      buildIndex12();
    }

    // Inserting the new tuple
    int index = firstFree;
    firstFree = arg1OrNext(firstFree);
    setEntry(index, arg1, arg2, arg3);
    count++;

    // Updating the indexes
    index123.insert(index, Miscellanea.hashcode(arg1, arg2, arg3));
    index12.insert(index, Miscellanea.hashcode(arg1, arg2));
    if (index_13_23 != null) {
      index_13_23.insert(index, Miscellanea.hashcode(arg1, arg3));
      index_13_23.insert(index, Miscellanea.hashcode(arg2, arg3));
    }
    if (index_1_2 != null) {
      index_1_2.insert(index, Miscellanea.hashcode(arg1));
      index_1_2.insert(index, Miscellanea.hashcode(arg2));
    }
    if (index3 != null)
      index3.insert(index, Miscellanea.hashcode(arg3));
  }

  public void clear() {
    count = 0;
    firstFree = 0;

    int size = capacity();
    for (int i=0 ; i < size ; i++)
      setEntry(i, i+1, Empty, 0);

    index123.clear();
    index12.clear();
    index_13_23.clear();
    index_1_2.clear();
    index3.clear();
  }

  public void delete(int arg1, int arg2, int arg3) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2, arg3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2 & arg3(idx) == arg3) {
        deleteAt(idx, hashcode);
        return;
      }
    }
  }

  public boolean contains(int arg1, int arg2, int arg3) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2, arg3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2 & arg3(idx) == arg3)
        return true;
    }
    return false;
  }

  public boolean contains12(int arg1, int arg2) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx)) {
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2)
        return true;
    }
    return false;
  }

  public boolean contains_13_23(int arg12, int arg3) {
    if (index_13_23 == null)
      buildIndex_13_23();
    int hashcode = Miscellanea.hashcode(arg12, arg3);
    for (int idx = index_13_23.head(hashcode) ; idx != Empty ; idx = index_13_23.next(idx)) {
      if ((arg1OrNext(idx) == arg12 || arg2OrEmptyMarker(idx) == arg12) & arg3(idx) == arg3)
        return true;
    }
    return false;
  }

  public boolean contains_1_2(int arg12) {
    if (index_1_2 == null)
      buildIndex_1_2();
    int hashcode = Miscellanea.hashcode(arg12);
    for (int idx = index_1_2.head(hashcode) ; idx != Empty ; idx = index_1_2.next(idx)) {
      if (arg1OrNext(idx) == arg12 | arg2OrEmptyMarker(idx) == arg12)
        return true;
    }
    return false;
  }

  public boolean contains3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx)) {
      if (arg3(idx) == arg3)
        return true;
    }
    return false;
  }

  public boolean count12Eq(int arg1, int arg2, int expCount) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx)) {
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2) {
        count++;
        if (count > expCount)
          return false;
      }
    }
    return count == expCount;
  }

  //## IS THIS ACTUALLY USED?
  public boolean count12Eq(int arg12, int expCount) {
    if (index_1_2 == null)
      buildIndex_1_2();
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg12);
    for (int idx = index_1_2.head(hashcode) ; idx != Empty ; idx = index_1_2.next(idx))
      if (arg1OrNext(idx) == arg12 || arg2OrEmptyMarker(idx) == arg12) {
        count++;
        if (count > expCount)
          return false;
      }
    return count == expCount;
  }

  public boolean count3Eq(int arg3, int expCount) {
    if (index3 == null)
      buildIndex3();
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx))
      if (arg3(idx) == arg3) {
        count++;
        if (count > expCount)
          return false;
      }
    return count == expCount;
  }

  public Iter123 getIter() {
    return new Iter123(this);
  }

  public Iter12 getIter12(int arg1, int arg2) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    return new Iter12(arg1, arg2, index12.head(hashcode), this);
  }

  public Iter_13_23 getIter_13_23(int arg12, int arg3) {
    if (index_13_23 == null)
      buildIndex_13_23();
    int hashcode = Miscellanea.hashcode(arg12, arg3);
    return new Iter_13_23(arg12, arg3, index_13_23.head(hashcode), this);
  }

  public Iter_1_2 getIter_1_2(int arg12) {
    if (index_1_2 == null)
      buildIndex_1_2();
    int hashcode = Miscellanea.hashcode(arg12);
    return new Iter_1_2(arg12, index_1_2.head(hashcode), this);
  }

  public Iter3 getIter3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(arg3);
    return new Iter3(arg3, index3.head(hashcode), this);
  }

  public Obj copy(int idx1, int idx2, int idx3) {
    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int size = capacity();
    int next = 0;
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty) {
        objs1[next] = store12.getValue(arg1OrNext(i));
        objs2[next] = store12.getValue(arg2);
        objs3[next] = store3.getValue(arg3(i));
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

  ////////////////////////////////////////////////////////////////////////////

  public boolean col3IsKey() {
    if (index3 == null)
      buildIndex3();

    int[] hashtable = index3.hashtable;
    int[] bucket = new int[32];

    for (int i=0 ; i < hashtable.length ; i++) {
      int count = 0;

      int idx = hashtable[i];
      while (idx != Empty) {
        bucket = Miscellanea.arrayAppend(bucket, count++, flatTuples[3 * i]);
        idx = index3.next(idx);
      }

      if (count > 1) {
        if (count > 2)
          Arrays.sort(bucket, 0, count);
        int last = bucket[0];
        for (int j=1 ; j < count ; i++) {
          int val = bucket[j];
          if (val == last)
            return false;
          last = val;
        }
      }
    }

    return true;
  }

  public boolean cols12AreKey() {
    return colsAreKey(index12, 0, 1);
  }

  ////////////////////////////////////////////////////////////////////////////

  boolean colsAreKey(Index index, int col1, int col2) {
    int[] hashtable = index.hashtable;
    long[] bucket = new long[32];

    for (int i=0 ; i < hashtable.length ; i++) {
      int count = 0;

      int idx = hashtable[i];
      while (idx != Empty) {
        int offset = 3 * i;
        int arg1 = flatTuples[offset + col1];
        int arg2 = flatTuples[offset + col2];
        long packedArgs = arg1 | (arg2 << 32);
        Miscellanea._assert(arg1 == (packedArgs & 0xFFFFFFFF));
        Miscellanea._assert(arg2 == (packedArgs >>> 32));
        bucket = Miscellanea.arrayAppend(bucket, count++, packedArgs);
        idx = index.next(idx);
      }

      if (count > 1) {
        if (count > 2)
          Arrays.sort(bucket, 0, count);
        long last = bucket[0];
        for (int j=1 ; j < count ; i++) {
          long val = bucket[j];
          if (val == last)
            return false;
          last = val;
        }
      }
    }

    return true;
  }

  void deleteAt(int index, int hashcode) {
    int arg1 = arg1OrNext(index);
    int arg2 = arg2OrEmptyMarker(index);
    int arg3 = arg3(index);
    Miscellanea._assert(arg2 != Empty);

    // Removing the tuple
    setEntry(index, firstFree, Empty, 0);
    firstFree = index;
    count--;

    // Updating the indexes
    index123.delete(index, hashcode);
    index12.delete(index, Miscellanea.hashcode(arg1, arg2));
    if (index_13_23 != null) {
      index_13_23.delete(index, Miscellanea.hashcode(arg1, arg3));
      index_13_23.delete(index, Miscellanea.hashcode(arg2, arg3));
    }
    if (index_1_2 != null) {
      index_1_2.delete(index, Miscellanea.hashcode(arg1));
      index_1_2.delete(index, Miscellanea.hashcode(arg2));
    }
    if (index3 != null)
      index3.delete(index, Miscellanea.hashcode(arg3));
  }

  void buildIndex123() {
    int size = capacity();
    index123 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty)
        index123.insert(i, Miscellanea.hashcode(arg1OrNext(i), arg2, arg3(i)));
    }
  }

  void buildIndex12() {
    int size = capacity();
    index12 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty)
        index12.insert(i, Miscellanea.hashcode(arg1OrNext(i), arg2));
    }
  }

  void buildIndex_13_23() {
    int size = capacity();
    index_13_23 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty) {
        index_13_23.insert(i, Miscellanea.hashcode(arg1OrNext(i), arg3(i)));
        index_13_23.insert(i, Miscellanea.hashcode(arg2, arg3(i)));
      }
    }
  }

  void buildIndex_1_2() {
    int size = capacity();
    index_1_2 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty) {
        index_1_2.insert(i, Miscellanea.hashcode(arg1OrNext(i)));
        index_1_2.insert(i, Miscellanea.hashcode(arg2));
      }
    }
  }

  void buildIndex3() {
    int size = capacity();
    index3 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty)
        index3.insert(i, Miscellanea.hashcode(arg3(i)));
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public Obj copy(TernaryTable[] tables, int idx1, int idx2, int idx3) {
    throw new RuntimeException();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static abstract class Iter {
    protected int index;
    protected Sym12TernaryTable table;


    protected Iter(int index, Sym12TernaryTable table) {
      this.index = index;
      this.table = table;
    }

    public boolean done() {
      return index == Empty;
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return table.arg1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return table.arg2OrEmptyMarker(index);
    }

    public int get3() {
      Miscellanea._assert(index != Empty);
      return table.arg3(index);
    }

    public abstract void next();
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class Iter123 extends Iter {
    int end;

    public Iter123(Sym12TernaryTable table) {
      super(0, table);
      end = table.capacity();
      while (table.arg2OrEmptyMarker(index) == Empty) {
        index++;
        if (index == end) {
          index = Empty;
          break;
        }
      }
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index++;
        if (index == end) {
          index = Empty;
          return;
        }
      } while (table.arg2OrEmptyMarker(index) == Empty);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class Iter12 extends Iter {
    int arg1, arg2;

    public Iter12(int index, int arg1, int arg2, Sym12TernaryTable table) {
      super(index, table);
      Miscellanea._assert(arg1 <= arg2);
      this.arg1 = arg1;
      this.arg2 = arg2;
      while (index != Empty && !isMatch())
        index = table.index12.next(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = table.index12.next(index);
      }
      while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return table.arg1OrNext(index) == arg1 && table.arg2OrEmptyMarker(index) == arg2;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class Iter_13_23 extends Iter {
    int arg12, arg3;

    public Iter_13_23(int index, int arg12, int arg3, Sym12TernaryTable table) {
      super(index, table);
      this.arg12 = arg12;
      this.arg3 = arg3;
      while (index != Empty && !isMatch())
        index = table.index_13_23.next(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = table.index_13_23.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return ( table.arg1OrNext(index)        == arg12 ||
               table.arg2OrEmptyMarker(index) == arg12
             ) && table.arg3(index)           == arg3;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class Iter_1_2 extends Iter {
    int arg12;

    public Iter_1_2(int index, int arg12, Sym12TernaryTable table) {
      super(index, table);
      this.arg12 = arg12;
      while (index != Empty && !isMatch())
        index = table.index_1_2.next(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = table.index_1_2.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return table.arg1OrNext(index) == arg12 || table.arg2OrEmptyMarker(index) == arg12;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class Iter3 extends Iter {
    int arg3;

    public Iter3(int index, int arg3, Sym12TernaryTable table) {
      super(index, table);
      this.arg3 = arg3;
      while (index != Empty && table.arg3(index) != arg3)
        index = table.index3.next(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = table.index3.next(index);
      } while (index != Empty && table.arg3(index) != arg3);
    }
  }
}
