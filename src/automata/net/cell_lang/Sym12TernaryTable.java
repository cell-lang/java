package net.cell_lang;

import java.util.Arrays;


class Sym12TernaryTable {
  public static final int Empty = 0xFFFFFFFF;

  static final int MinSize = 32;

  int[] flatTuples = new int[3 * MinSize];
  public int count = 0;
  int firstFree = 0;

  public Index index123, index12, index13, index23, index1, index2, index3;

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
      index13 = null;
      index23 = null;
      index1 = null;
      index2 = null;
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
    if (index13 != null)
      index13.insert(index, Miscellanea.hashcode(arg1, arg3));
    if (index23 != null)
      index23.insert(index, Miscellanea.hashcode(arg2, arg3));
    if (index1 != null)
      index1.insert(index, Miscellanea.hashcode(arg1));
    if (index2 != null)
      index2.insert(index, Miscellanea.hashcode(arg2));
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
    index13.clear();
    index23.clear();
    index1.clear();
    index2.clear();
    index3.clear();
  }

  public void delete(int arg1, int arg2, int arg3) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2, arg3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx))
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2 & arg3(idx) == arg3) {
        deleteAt(idx, hashcode);
        return;
      }
  }

  public boolean contains(int arg1, int arg2, int arg3) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2, arg3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx))
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2 & arg3(idx) == arg3)
        return true;
    return false;
  }

  public boolean contains12(int arg1, int arg2) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx))
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2)
        return true;
    return false;
  }

  public boolean contains_13_23(int arg12, int arg3) {
    int hashcode = Miscellanea.hashcode(arg12, arg3);
    if (index13 == null)
      buildIndex13();
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx))
      if ((arg1OrNext(idx) == arg12 || arg2OrEmptyMarker(idx) == arg12) & arg3(idx) == arg3)
        return true;
    if (index23 == null)
      buildIndex23();
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx))
      if ((arg1OrNext(idx) == arg12 || arg2OrEmptyMarker(idx) == arg12) & arg3(idx) == arg3)
        return true;
    return false;
  }

  public boolean contains_1_2(int arg12) {
    int hashcode = Miscellanea.hashcode(arg12);
    if (index1 == null)
      buildIndex1();
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx))
      if (arg1OrNext(idx) == arg12 | arg2OrEmptyMarker(idx) == arg12)
        return true;
    if (index2 == null)
      buildIndex2();
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx))
      if (arg1OrNext(idx) == arg12 | arg2OrEmptyMarker(idx) == arg12)
        return true;
    return false;
  }

  public boolean contains3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx))
      if (arg3(idx) == arg3)
        return true;
    return false;
  }

  public int count12(int arg1, int arg2) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx))
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2)
        count++;
    return count;
  }

  public int count_13_23(int arg12, int arg3) {
    int hashcode = Miscellanea.hashcode(arg12, arg3);
    int count = 0;
    if (index13 == null)
      buildIndex13();
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx))
      if (arg1OrNext(idx) == arg12 && arg3(idx) == arg3)
        count++;
    if (index23 == null)
      buildIndex23();
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx))
      if (arg1OrNext(idx) != arg12 && arg2OrEmptyMarker(idx) == arg12 && arg3(idx) == arg3)
        count++;
    return count;
  }

  public int count_1_2(int arg12) {
    int hashcode = Miscellanea.hashcode(arg12);
    int count = 0;
    if (index1 == null)
      buildIndex1();
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx))
      if (arg1OrNext(idx) == arg12)
        count++;
    if (index2 == null)
      buildIndex2();
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx))
      if (arg1OrNext(idx) != arg12 && arg2OrEmptyMarker(idx) == arg12)
        count++;
    return count;
  }

  public int count3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx))
      if (arg3(idx) == arg3)
        count++;
    return count;
  }

  public boolean count12Eq(int arg1, int arg2, int expCount) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    int count = 0;
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx))
      if (arg1OrNext(idx) == arg1 & arg2OrEmptyMarker(idx) == arg2) {
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
    return new Iter123();
  }

  public Iter12 getIter12(int arg1, int arg2) {
    if (arg1 > arg2) {
      int tmp = arg1;
      arg1 = arg2;
      arg2 = tmp;
    }
    Miscellanea._assert(arg1 <= arg2);
    int hashcode = Miscellanea.hashcode(arg1, arg2);
    return new Iter12(arg1, arg2, index12.head(hashcode));
  }

  public Iter getIter_13_23(int arg12, int arg3) {
    int hashcode = Miscellanea.hashcode(arg12, arg3);
    if (index13 == null)
      buildIndex13();
    Iter iter1 = new Iter13(arg12, arg3, index13.head(hashcode));
    if (index23 == null)
      buildIndex23();
    Iter iter2 = new Iter23(arg12, arg3, index23.head(hashcode));
    if (iter1.done())
      return iter2;
    if (iter2.done())
      return iter1;
    return new IterPair(iter1, iter2);
  }

  public Iter getIter_1_2(int arg12) {
    int hashcode = Miscellanea.hashcode(arg12);
    if (index1 == null)
      buildIndex1();
    Iter iter1 = new Iter1(arg12, index1.head(hashcode));
    if (index2 == null)
      buildIndex2();
    Iter iter2 = new Iter2(arg12, index2.head(hashcode));
    if (iter1.done())
      return iter2;
    if (iter2.done())
      return iter1;
    return new IterPair(iter1, iter2);
  }

  public Iter3 getIter3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(arg3);
    return new Iter3(arg3, index3.head(hashcode));
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
    if (index13 != null)
      index13.delete(index, Miscellanea.hashcode(arg1, arg3));
    if (index23 != null)
      index23.delete(index, Miscellanea.hashcode(arg2, arg3));
    if (index1 != null)
      index1.delete(index, Miscellanea.hashcode(arg1));
    if (index2 != null)
      index2.delete(index, Miscellanea.hashcode(arg2));
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

  void buildIndex13() {
    int size = capacity();
    index13 = new Index(size);
    for (int i=0 ; i < size ; i++)
      if (arg2OrEmptyMarker(i) != Empty)
        index13.insert(i, Miscellanea.hashcode(arg1OrNext(i), arg3(i)));
  }

  void buildIndex23() {
    int size = capacity();
    index23 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty)
        index23.insert(i, Miscellanea.hashcode(arg2, arg3(i)));
    }
  }

  void buildIndex1() {
    int size = capacity();
    index1 = new Index(size);
    for (int i=0 ; i < size ; i++)
      if (arg2OrEmptyMarker(i) != Empty)
        index1.insert(i, Miscellanea.hashcode(arg1OrNext(i)));
  }

  void buildIndex2() {
    int size = capacity();
    index2 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int arg2 = arg2OrEmptyMarker(i);
      if (arg2 != Empty)
        index2.insert(i, Miscellanea.hashcode(arg2));
    }
  }

  void buildIndex3() {
    int size = capacity();
    index3 = new Index(size);
    for (int i=0 ; i < size ; i++)
      if (arg2OrEmptyMarker(i) != Empty)
        index3.insert(i, Miscellanea.hashcode(arg3(i)));
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(Sym12TernaryTable[] tables, int idx1, int idx2, int idx3) {
    throw new RuntimeException();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public abstract static class Iter {
    public abstract boolean done();
    public abstract void next();

    public int get1() {
      System.out.println(toString());
      throw Miscellanea.internalFail();
    }

    public int get2() {
      System.out.println(toString());
      throw Miscellanea.internalFail();
    }

    public int get3() {
      System.out.println(toString());
      throw Miscellanea.internalFail();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static final class IterPair extends Iter {
    Iter iter1;
    Iter iter2;

    public IterPair(Iter iter1, Iter iter2) {
      Miscellanea._assert(!iter1.done() & !iter2.done());
      this.iter1 = iter1;
      this.iter2 = iter2;
    }

    public boolean done() {
      return iter1 == null;
    }

    public void next() {
      iter1.next();
      if (iter1.done()) {
        iter1 = iter2;
        iter2 = null;
      }
    }

    public int get1() {
      return iter1.get1();
    }

    public int get2() {
      return iter1.get2();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract static class IdxIter extends Iter {
    protected int index;

    protected IdxIter(int index) {
      this.index = index;
    }

    public boolean done() {
      return index == Empty;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter123 extends IdxIter {
    int end;

    public Iter123() {
      super(0);
      end = capacity();
      while (arg2OrEmptyMarker(index) == Empty) {
        index++;
        if (index == end) {
          index = Empty;
          break;
        }
      }
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return arg2OrEmptyMarker(index);
    }

    public int get3() {
      Miscellanea._assert(index != Empty);
      return arg3(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index++;
        if (index == end) {
          index = Empty;
          return;
        }
      } while (arg2OrEmptyMarker(index) == Empty);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter12 extends IdxIter {
    int arg1, arg2;

    public Iter12(int arg1, int arg2, int index0) {
      super(index0);
      Miscellanea._assert(arg1 <= arg2);
      this.arg1 = arg1;
      this.arg2 = arg2;
      while (index != Empty && !isMatch())
        index = index12.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg3(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index12.next(index);
      }
      while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return arg1OrNext(index) == arg1 && arg2OrEmptyMarker(index) == arg2;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private final class Iter13 extends IdxIter {
    int arg1, arg3;

    public Iter13(int arg1, int arg3, int index0) {
      super(index0);
      this.arg1 = arg1;
      this.arg3 = arg3;
      while (index != Empty && !isMatch())
        index = index13.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg2OrEmptyMarker(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index13.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return arg1OrNext(index) == arg1 && arg3(index) == arg3;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private final class Iter23 extends IdxIter {
    int arg2, arg3;

    public Iter23(int arg2, int arg3, int index0) {
      super(index0);
      this.arg2 = arg2;
      this.arg3 = arg3;
      while (index != Empty && !isMatch())
        index = index23.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg1OrNext(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index23.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      // Since it's always used together with Iter13, in order to avoid duplicates we skip entries
      // of the form (arg2, arg2, arg3) that would be found by a search through the other index
      return arg1OrNext(index) != arg2 &&
             arg2OrEmptyMarker(index) == arg2 &&
             arg3(index) == arg3;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private final class Iter1 extends IdxIter {
    int arg1;

    public Iter1(int arg1, int index0) {
      super(index0);
      this.arg1 = arg1;
      while (index != Empty && !isMatch())
        index = index1.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg2OrEmptyMarker(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return arg3(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index1.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      return arg1OrNext(index) == arg1;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private final class Iter2 extends IdxIter {
    int arg2;

    public Iter2(int arg2, int index0) {
      super(index0);
      this.arg2 = arg2;
      while (index != Empty && !isMatch())
        index = index2.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return arg3(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index2.next(index);
      } while (index != Empty && !isMatch());
    }

    boolean isMatch() {
      // Since it's always used together with Iter1, in order to avoid duplicates we skip entries
      // of the form (arg2, arg2, *) that would be found by a search through the other index
      return arg1OrNext(index) != arg2 && arg2OrEmptyMarker(index) == arg2;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter3 extends IdxIter {
    int arg3;

    public Iter3(int arg3, int index0) {
      super(index0);
      this.arg3 = arg3;
      while (index != Empty && arg3(index) != arg3)
        index = index3.next(index);
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return arg1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return arg2OrEmptyMarker(index);
    }

    public void next() {
      Miscellanea._assert(!done());
      do {
        index = index3.next(index);
      } while (index != Empty && arg3(index) != arg3);
    }
  }
}
