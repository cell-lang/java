package net.cell_lang;

import java.util.Arrays;


class TernaryTable {
  public static final int Empty = 0xFFFFFFFF;

  static final int MinSize = 32;

  int[] flatTuples = new int[3 * MinSize];
  public int count = 0;
  int firstFree = 0;

  public Index index123, index12, index13, index23, index1, index2, index3;

  public SurrObjMapper mapper1, mapper2, mapper3;

  //////////////////////////////////////////////////////////////////////////////

  final int field1OrNext(int idx) {
    return flatTuples[3 * idx];
  }

  final int field2OrEmptyMarker(int idx) {
    return flatTuples[3 * idx + 1];
  }

  final int field3(int idx) {
    return flatTuples[3 * idx + 2];
  }

  final void setEntry(int idx, int field1, int field2, int field3) {
    int offset = 3 * idx;
    flatTuples[offset]   = field1;
    flatTuples[offset+1] = field2;
    flatTuples[offset+2] = field3;

    Miscellanea._assert(field1OrNext(idx) == field1);
    Miscellanea._assert(field2OrEmptyMarker(idx) == field2);
    Miscellanea._assert(field3(idx) == field3);
  }

  final int capacity() {
    return flatTuples.length / 3;
  }

  final void resize() {
    int len = flatTuples.length;
    Miscellanea._assert(3 * count == len);
    int[] newFlatTuples = new int[2 * len];
    Array.copy(flatTuples, newFlatTuples, len);
    flatTuples = newFlatTuples;
    int size = len / 3;
    for (int i=size ; i < 2 * size ; i++)
      setEntry(i, i+1, Empty, 0);
  }

  //////////////////////////////////////////////////////////////////////////////

  public TernaryTable(SurrObjMapper mapper1, SurrObjMapper mapper2, SurrObjMapper mapper3) {
    for (int i=0 ; i < MinSize ; i++)
      setEntry(i, i+1, Empty, 0);

    index123 = new Index(MinSize);
    index12  = new Index(MinSize);

    this.mapper1 = mapper1;
    this.mapper2 = mapper2;
    this.mapper3 = mapper3;
  }

  public int size() {
    return count;
  }

  public void insert(int field1, int field2, int field3) {
    if (contains(field1, field2, field3))
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
    firstFree = field1OrNext(firstFree);
    setEntry(index, field1, field2, field3);
    count++;

    // Updating the indexes
    index123.insert(index, Hashing.hashcode(field1, field2, field3));
    index12.insert(index, Hashing.hashcode(field1, field2));
    if (index13 != null)
      index13.insert(index, Hashing.hashcode(field1, field3));
    if (index23 != null)
      index23.insert(index, Hashing.hashcode(field2, field3));
    if (index1 != null)
      index1.insert(index, Hashing.hashcode(field1));
    if (index2 != null)
      index2.insert(index, Hashing.hashcode(field2));
    if (index3 != null)
      index3.insert(index, Hashing.hashcode(field3));
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

  public void delete(int field1, int field2, int field3) {
    int hashcode = Hashing.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2 & field3(idx) == field3) {
        deleteAt(idx, hashcode);
        return;
      }
    }
  }

  public int containsAt(int field1, int field2, int field3) {
    int hashcode = Hashing.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2 & field3(idx) == field3)
        return idx;
    }
    return -1;
  }

  public boolean contains(int field1, int field2, int field3) {
    int hashcode = Hashing.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains12(int field1, int field2) {
    int hashcode = Hashing.hashcode(field1, field2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2)
        return true;
    }
    return false;
  }

  public boolean contains13(int field1, int field3) {
    if (index13 == null)
      buildIndex13();
    int hashcode = Hashing.hashcode(field1, field3);
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx)) {
      if (field1OrNext(idx) == field1 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains23(int field2, int field3) {
    if (index23 == null)
      buildIndex23();
    int hashcode = Hashing.hashcode(field2, field3);
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx)) {
      if (field2OrEmptyMarker(idx) == field2 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains1(int field1) {
    if (index1 == null)
      buildIndex1();
    int hashcode = Hashing.hashcode(field1);
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx)) {
      if (field1OrNext(idx) == field1)
        return true;
    }
    return false;
  }

  public boolean contains2(int field2) {
    if (index2 == null)
      buildIndex2();
    int hashcode = Hashing.hashcode(field2);
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx)) {
      if (field2OrEmptyMarker(idx) == field2)
        return true;
    }
    return false;
  }

  public boolean contains3(int field3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Hashing.hashcode(field3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx)) {
      if (field3(idx) == field3)
        return true;
    }
    return false;
  }


  public int lookup12(int arg1, int arg2) {
    int hashcode = Hashing.hashcode(arg1, arg2);
    int value = -1;
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx)) {
      if (field1OrNext(idx) == arg1 && field2OrEmptyMarker(idx) == arg2)
        if (value == -1)
          value = field3(idx);
        else
          throw Miscellanea.softFail();
    }
    return value;
  }

  public int lookup13(int arg1, int arg3) {
    if (index13 == null)
      buildIndex13();
    int hashcode = Hashing.hashcode(arg1, arg3);
    int value = -1;
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx))
      if (field1OrNext(idx) == arg1 && field3(idx) == arg3)
        if (value == -1)
          value = field2OrEmptyMarker(idx);
        else
          throw Miscellanea.softFail();
    return value;
  }

  public int lookup23(int arg2, int arg3) {
    if (index23 == null)
      buildIndex23();
    int hashcode = Hashing.hashcode(arg2, arg3);
    int value = -1;
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx))
      if (field2OrEmptyMarker(idx) == arg2 && field3(idx) == arg3)
        if (value == -1)
          value = field1OrNext(idx);
        else
          throw Miscellanea.softFail();
    return value;
  }

  public int count12(int arg1, int arg2) {
    int count = 0;
    int hashcode = Hashing.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx))
      if (field1OrNext(idx) == arg1 & field2OrEmptyMarker(idx) == arg2)
        count++;
    return count;
  }

  public int count13(int arg1, int arg3) {
    if (index13 == null)
      buildIndex13();
    int count = 0;
    int hashcode = Hashing.hashcode(arg1, arg3);
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx))
      if (field1OrNext(idx) == arg1 & field3(idx) == arg3)
        count++;
    return count;
  }

  public int count23(int arg2, int arg3) {
    if (index23 == null)
      buildIndex23();
    int count = 0;
    int hashcode = Hashing.hashcode(arg2, arg3);
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx))
      if (field2OrEmptyMarker(idx) == arg2 & field3(idx) == arg3)
        count++;
    return count;
  }

  public int count1(int arg1) {
    if (index1 == null)
      buildIndex1();
    int count = 0;
    int hashcode = Hashing.hashcode(arg1);
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx))
      if (field1OrNext(idx) == arg1)
        count++;
    return count;
  }

  public int count2(int arg2) {
    if (index2 == null)
      buildIndex2();
    int count = 0;
    int hashcode = Hashing.hashcode(arg2);
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx))
      if (field2OrEmptyMarker(idx) == arg2)
        count++;
    return count;
  }

  public int count3(int arg3) {
    if (index3 == null)
      buildIndex3();
    int count = 0;
    int hashcode = Hashing.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx))
      if (field3(idx) == arg3)
        count++;
    return count;
  }

  public boolean count12Eq(int arg1, int arg2, int expCount) {
    int count = 0;
    int hashcode = Hashing.hashcode(arg1, arg2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx))
      if (field1OrNext(idx) == arg1 & field2OrEmptyMarker(idx) == arg2) {
        count++;
        if (count > expCount)
          return false;
      }
    return count == expCount;
  }

  public boolean count1Eq(int arg1, int expCount) {
    if (index1 == null)
      buildIndex1();
    int count = 0;
    int hashcode = Hashing.hashcode(arg1);
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx))
      if (field1OrNext(idx) == arg1) {
        count++;
        if (count > expCount)
          return false;
      }
    return count == expCount;
  }

  public boolean count2Eq(int arg2, int expCount) {
    if (index2 == null)
      buildIndex2();
    int count = 0;
    int hashcode = Hashing.hashcode(arg2);
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx))
      if (field2OrEmptyMarker(idx) == arg2) {
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
    int hashcode = Hashing.hashcode(arg3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx))
      if (field3(idx) == arg3) {
        count++;
        if (count > expCount)
          return false;
      }
    return count == expCount;
  }

  public Iter123 getIter() {
    return new Iter123();
  }

  public Iter12 getIter12(int field1, int field2) {
    int hashcode = Hashing.hashcode(field1, field2);
    return new Iter12(field1, field2, index12.head(hashcode));
  }

  public Iter13 getIter13(int field1, int field3) {
    if (index13 == null)
      buildIndex13();
    int hashcode = Hashing.hashcode(field1, field3);
    return new Iter13(field1, field3, index13.head(hashcode));
  }

  public Iter23 getIter23(int field2, int field3) {
    if (index23 == null)
      buildIndex23();
    int hashcode = Hashing.hashcode(field2, field3);
    return new Iter23(field2, field3, index23.head(hashcode));
  }

  public Iter1 getIter1(int field1) {
    if (index1 == null)
      buildIndex1();
    int hashcode = Hashing.hashcode(field1);
    return new Iter1(field1, index1.head(hashcode));
  }

  public Iter2 getIter2(int field2) {
    if (index2 == null)
      buildIndex2();
    int hashcode = Hashing.hashcode(field2);
    return new Iter2(field2, index2.head(hashcode));
  }

  public Iter3 getIter3(int field3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Hashing.hashcode(field3);
    return new Iter3(field3, index3.head(hashcode));
  }

  public Obj copy(int idx1, int idx2, int idx3) {
    return copy(new TernaryTable[] {this}, idx1, idx2, idx3);
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
        bucket = Array.append(bucket, count++, flatTuples[3 * idx + 2]);
        idx = index3.next(idx);
      }

      if (count > 1) {
        if (count > 2)
          Arrays.sort(bucket, 0, count);
        int last = bucket[0];
        for (int j=1 ; j < count ; j++) {
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

  public boolean cols13AreKey() {
    if (index13 == null)
      buildIndex13();
    return colsAreKey(index13, 0, 2);
  }

  public boolean cols23AreKey() {
    if (index23 == null)
      buildIndex23();
    return colsAreKey(index23, 1, 2);
  }

  ////////////////////////////////////////////////////////////////////////////

  boolean colsAreKey(Index index, int col1, int col2) {
    int[] hashtable = index.hashtable;
    long[] bucket = new long[32];

    for (int i=0 ; i < hashtable.length ; i++) {
      int count = 0;

      int idx = hashtable[i];
      while (idx != Empty) {
        int offset = 3 * idx;
        long arg1 = flatTuples[offset + col1];
        long arg2 = flatTuples[offset + col2];
        long packedArgs = arg1 | (arg2 << 32);
        Miscellanea._assert(arg1 == (packedArgs & 0xFFFFFFFFL));
        Miscellanea._assert(arg2 == (packedArgs >>> 32));
        bucket = Array.append(bucket, count++, packedArgs);
        idx = index.next(idx);
      }

      if (count > 1) {
        if (count > 2)
          Arrays.sort(bucket, 0, count);
        long last = bucket[0];
        for (int j=1 ; j < count ; j++) {
          long val = bucket[j];
          if (val == last)
            return false;
          last = val;
        }
      }
    }

    return true;
  }

  public boolean deleteAt(int index) {
    int field2 = field2OrEmptyMarker(index);
    if (field2 == Empty)
      return false;

    int field1 = field1OrNext(index);
    int field3 = field3(index);

    // Removing the tuple
    setEntry(index, firstFree, Empty, 0);
    firstFree = index;
    count--;

    // Updating the indexes
    index123.delete(index, Hashing.hashcode(field1, field2, field3));
    index12.delete(index, Hashing.hashcode(field1, field2));
    if (index13 != null)
      index13.delete(index, Hashing.hashcode(field1, field3));
    if (index23 != null)
      index23.delete(index, Hashing.hashcode(field2, field3));
    if (index1 != null)
      index1.delete(index, Hashing.hashcode(field1));
    if (index2 != null)
      index2.delete(index, Hashing.hashcode(field2));
    if (index3 != null)
      index3.delete(index, Hashing.hashcode(field3));

    return true;
  }

  //////////////////////////////////////////////////////////////////////////////

  Index getIndex123() {
    if (index123 == null)
      buildIndex123();
    return index123;
  }

  Index getIndex12() {
    if (index12 == null)
      buildIndex12();
    return index12;
  }

  Index getIndex13() {
    if (index13 == null)
      buildIndex13();
    return index13;
  }

  Index getIndex23() {
    if (index23 == null)
      buildIndex23();
    return index23;
  }

  Index getIndex1() {
    if (index1 == null)
      buildIndex1();
    return index1;
  }

  Index getIndex2() {
    if (index2 == null)
      buildIndex2();
    return index2;
  }

  Index getIndex3() {
    if (index3 == null)
      buildIndex3();
    return index3;
  }

  //////////////////////////////////////////////////////////////////////////////

  void deleteAt(int index, int hashcode) {
    int field1 = field1OrNext(index);
    int field2 = field2OrEmptyMarker(index);
    int field3 = field3(index);
    Miscellanea._assert(field2 != Empty);

    // Removing the tuple
    setEntry(index, firstFree, Empty, 0);
    firstFree = index;
    count--;

    // Updating the indexes
    index123.delete(index, hashcode);
    index12.delete(index, Hashing.hashcode(field1, field2));
    if (index13 != null)
      index13.delete(index, Hashing.hashcode(field1, field3));
    if (index23 != null)
      index23.delete(index, Hashing.hashcode(field2, field3));
    if (index1 != null)
      index1.delete(index, Hashing.hashcode(field1));
    if (index2 != null)
      index2.delete(index, Hashing.hashcode(field2));
    if (index3 != null)
      index3.delete(index, Hashing.hashcode(field3));
  }

  void buildIndex123() {
    int size = capacity();
    index123 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index123.insert(i, Hashing.hashcode(field1OrNext(i), field2, field3(i)));
    }
  }

  void buildIndex12() {
    int size = capacity();
    index12 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index12.insert(i, Hashing.hashcode(field1OrNext(i), field2));
    }
  }

  void buildIndex13() {
    int size = capacity();
    index13 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index13.insert(i, Hashing.hashcode(field1OrNext(i), field3(i)));
    }
  }

  void buildIndex23() {
    int size = capacity();
    index23 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index23.insert(i, Hashing.hashcode(field2, field3(i)));
    }
  }

  void buildIndex1() {
    int size = capacity();
    index1 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index1.insert(i, Hashing.hashcode(field1OrNext(i)));
    }
  }

  void buildIndex2() {
    int size = capacity();
    index2 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index2.insert(i, Hashing.hashcode(field2));
    }
  }

  void buildIndex3() {
    int size = capacity();
    index3 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index3.insert(i, Hashing.hashcode(field3(i)));
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(TernaryTable[] tables, int idx1, int idx2, int idx3) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].count;

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      TernaryTable table = tables[iT];
      SurrObjMapper mapper1 = table.mapper1;
      SurrObjMapper mapper2 = table.mapper2;
      SurrObjMapper mapper3 = table.mapper3;
      int size = table.capacity();
      for (int iS=0 ; iS < size ; iS++) {
        int field2 = table.field2OrEmptyMarker(iS);
        if (field2 != Empty) {
          objs1[next] = mapper1.surrToObj(table.field1OrNext(iS));
          objs2[next] = mapper2.surrToObj(field2);
          objs3[next] = mapper3.surrToObj(table.field3(iS));
          next++;
        }
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

  public abstract static class Iter {
    int index;

    public boolean done() {
      return index == Empty;
    }

    public int index() {
      return index;
    }

    public int get1() {
      throw Miscellanea.internalFail();
    }

    public int get2() {
      throw Miscellanea.internalFail();
    }

    public int get3() {
      throw Miscellanea.internalFail();
    }

    public abstract void next();
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter123 extends Iter {
    public Iter123() {
      if (count > 0) {
        index = 0;
        while (field2OrEmptyMarker(index) == Empty)
          index++;
      }
      else
        index = Empty;
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return field2OrEmptyMarker(index);
    }

    public int get3() {
      Miscellanea._assert(index != Empty);
      return field3(index);
    }

    public void next() {
      Miscellanea._assert(index != Empty);
      int size = capacity();
      do {
        index++;
        if (index == size) {
          index = Empty;
          return;
        }
      } while (field2OrEmptyMarker(index) == Empty);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter12 extends Iter {
    int arg1;
    int arg2;

    public Iter12(int arg1, int arg2, int index) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field3(index);
    }

    public void next() {
      Miscellanea._assert(index != Empty);
      do {
        index = index12.next(index);
      } while (index != Empty && !isMatch());
    }


    private boolean isMatch() {
      return field1OrNext(index) == arg1 && field2OrEmptyMarker(index) == arg2;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter13 extends Iter {
    int arg1;
    int arg3;

    public Iter13(int arg1, int arg3, int index) {
      this.arg1 = arg1;
      this.arg3 = arg3;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field2OrEmptyMarker(index);
    }

    public void next() {
      Miscellanea._assert(index != Empty);
      do {
        index = index13.next(index);
      } while (index != Empty && !isMatch());
    }

    private boolean isMatch() {
      return field1OrNext(index) == arg1 && field3(index) == arg3;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter23 extends Iter {
    int arg2;
    int arg3;

    public Iter23(int arg2, int arg3, int index) {
      this.arg2 = arg2;
      this.arg3 = arg3;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field1OrNext(index);
    }

    public void next() {
      Miscellanea._assert(index != Empty);
      do {
        index = index23.next(index);
      } while (index != Empty && !isMatch());
    }

    private boolean isMatch() {
      return field2OrEmptyMarker(index) == arg2 && field3(index) == arg3;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter1 extends Iter {
    int arg1;

    public Iter1(int arg1, int index) {
      this.arg1 = arg1;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field2OrEmptyMarker(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return field3(index);
    }

    public void next() {
      do {
        index = index1.next(index);
      } while (index != Empty && field1OrNext(index) != arg1);
    }

    private boolean isMatch() {
      return field1OrNext(index) == arg1;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter2 extends Iter {
    int arg2;

    public Iter2(int arg2, int index) {
      this.arg2 = arg2;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return field3(index);
    }

    public void next() {
      do {
        index = index2.next(index);
      } while (index != Empty && field2OrEmptyMarker(index) != arg2);
    }

    private boolean isMatch() {
      return field2OrEmptyMarker(index) == arg2;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public final class Iter3 extends Iter {
    int arg3;

    public Iter3(int arg3, int index) {
      this.arg3 = arg3;
      this.index = index;
      if (index != Empty && !isMatch())
        next();
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return field1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return field2OrEmptyMarker(index);
    }

    public void next() {
      do {
        index = index3.next(index);
      } while (index != Empty && field3(index) != arg3);
    }

    private boolean isMatch() {
      return field3(index) == arg3;
    }
  }
}
