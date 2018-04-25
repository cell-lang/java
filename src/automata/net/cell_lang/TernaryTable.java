package net.cell_lang;


class TernaryTable {
  public static final int Empty = 0xFFFFFFFF;

  static final int MinSize = 32;

  int[] flatTuples = new int[3 * MinSize];
  public int count = 0;
  int firstFree = 0;

  public Index index123, index12, index13, index23, index1, index2, index3;

  public ValueStore store1, store2, store3;

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
    Miscellanea.arrayCopy(flatTuples, newFlatTuples, len);
    flatTuples = newFlatTuples;
    int size = len / 3;
    for (int i=size ; i < 2 * size ; i++)
      setEntry(i, i+1, Empty, 0);
  }

  //////////////////////////////////////////////////////////////////////////////

  public TernaryTable(ValueStore store1, ValueStore store2, ValueStore store3) {
    for (int i=0 ; i < MinSize ; i++)
      setEntry(i, i+1, Empty, 0);

    index123 = new Index(MinSize);
    index12  = new Index(MinSize);

    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
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
    index123.insert(index, Miscellanea.hashcode(field1, field2, field3));
    index12.insert(index, Miscellanea.hashcode(field1, field2));
    if (index13 != null)
      index13.insert(index, Miscellanea.hashcode(field1, field3));
    if (index23 != null)
      index23.insert(index, Miscellanea.hashcode(field2, field3));
    if (index1 != null)
      index1.insert(index, Miscellanea.hashcode(field1));
    if (index2 != null)
      index2.insert(index, Miscellanea.hashcode(field2));
    if (index3 != null)
      index3.insert(index, Miscellanea.hashcode(field3));
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
    int hashcode = Miscellanea.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2 & field3(idx) == field3) {
        deleteAt(idx, hashcode);
        return;
      }
    }
  }

  public boolean contains(int field1, int field2, int field3) {
    int hashcode = Miscellanea.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Empty ; idx = index123.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains12(int field1, int field2) {
    int hashcode = Miscellanea.hashcode(field1, field2);
    for (int idx = index12.head(hashcode) ; idx != Empty ; idx = index12.next(idx)) {
      if (field1OrNext(idx) == field1 & field2OrEmptyMarker(idx) == field2)
        return true;
    }
    return false;
  }

  public boolean contains13(int field1, int field3) {
    if (index13 == null)
      buildIndex13();
    int hashcode = Miscellanea.hashcode(field1, field3);
    for (int idx = index13.head(hashcode) ; idx != Empty ; idx = index13.next(idx)) {
      if (field1OrNext(idx) == field1 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains23(int field2, int field3) {
    if (index23 == null)
      buildIndex23();
    int hashcode = Miscellanea.hashcode(field2, field3);
    for (int idx = index23.head(hashcode) ; idx != Empty ; idx = index23.next(idx)) {
      if (field2OrEmptyMarker(idx) == field2 & field3(idx) == field3)
        return true;
    }
    return false;
  }

  public boolean contains1(int field1) {
    if (index1 == null)
      buildIndex1();
    int hashcode = Miscellanea.hashcode(field1);
    for (int idx = index1.head(hashcode) ; idx != Empty ; idx = index1.next(idx)) {
      if (field1OrNext(idx) == field1)
        return true;
    }
    return false;
  }

  public boolean contains2(int field2) {
    if (index2 == null)
      buildIndex2();
    int hashcode = Miscellanea.hashcode(field2);
    for (int idx = index2.head(hashcode) ; idx != Empty ; idx = index2.next(idx)) {
      if (field2OrEmptyMarker(idx) == field2)
        return true;
    }
    return false;
  }

  public boolean contains3(int field3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(field3);
    for (int idx = index3.head(hashcode) ; idx != Empty ; idx = index3.next(idx)) {
      if (field3(idx) == field3)
        return true;
    }
    return false;
  }

  public Iter getIter() {
    return new Iter(Empty, Empty, Empty, 0, Iter.Type.F123, this);
  }

  public Iter getIter12(int field1, int field2) {
    int hashcode = Miscellanea.hashcode(field1, field2);
    return new Iter(field1, field2, Empty, index12.head(hashcode), Iter.Type.F12, this);
  }

  public Iter getIter13(int field1, int field3) {
    if (index13 == null)
      buildIndex13();
    int hashcode = Miscellanea.hashcode(field1, field3);
    return new Iter(field1, Empty, field3, index13.head(hashcode), Iter.Type.F13, this);
  }

  public Iter getIter23(int field2, int field3) {
    if (index23 == null)
      buildIndex23();
    int hashcode = Miscellanea.hashcode(field2, field3);
    return new Iter(Empty, field2, field3, index23.head(hashcode), Iter.Type.F23, this);
  }

  public Iter getIter1(int field1) {
    if (index1 == null)
      buildIndex1();
    int hashcode = Miscellanea.hashcode(field1);
    return new Iter(field1, Empty, Empty, index1.head(hashcode), Iter.Type.F1, this);
  }

  public Iter getIter2(int field2) {
    if (index2 == null)
      buildIndex2();
    int hashcode = Miscellanea.hashcode(field2);
    return new Iter(Empty, field2, Empty, index2.head(hashcode), Iter.Type.F2, this);
  }

  public Iter getIter3(int field3) {
    if (index3 == null)
      buildIndex3();
    int hashcode = Miscellanea.hashcode(field3);
    return new Iter(Empty, Empty, field3, index3.head(hashcode), Iter.Type.F3, this);
  }

  public Obj copy(int idx1, int idx2, int idx3) {
    if (count == 0)
      return EmptyRelObj.singleton();

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int size = capacity();
    int next = 0;
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty) {
        objs1[next] = store1.getValue(field1OrNext(i));
        objs2[next] = store2.getValue(field2);
        objs3[next] = store3.getValue(field3(i));
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
    index12.delete(index, Miscellanea.hashcode(field1, field2));
    if (index13 != null)
      index13.delete(index, Miscellanea.hashcode(field1, field3));
    if (index23 != null)
      index23.delete(index, Miscellanea.hashcode(field2, field3));
    if (index1 != null)
      index1.delete(index, Miscellanea.hashcode(field1));
    if (index2 != null)
      index2.delete(index, Miscellanea.hashcode(field2));
    if (index3 != null)
      index3.delete(index, Miscellanea.hashcode(field3));
  }

  void buildIndex123() {
    int size = capacity();
    index123 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index123.insert(i, Miscellanea.hashcode(field1OrNext(i), field2, field3(i)));
    }
  }

  void buildIndex12() {
    int size = capacity();
    index12 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index12.insert(i, Miscellanea.hashcode(field1OrNext(i), field2));
    }
  }

  void buildIndex13() {
    int size = capacity();
    index13 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index13.insert(i, Miscellanea.hashcode(field1OrNext(i), field3(i)));
    }
  }

  void buildIndex23() {
    int size = capacity();
    index23 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index23.insert(i, Miscellanea.hashcode(field2, field3(i)));
    }
  }

  void buildIndex1() {
    int size = capacity();
    index1 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index1.insert(i, Miscellanea.hashcode(field1OrNext(i)));
    }
  }

  void buildIndex2() {
    int size = capacity();
    index2 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index2.insert(i, Miscellanea.hashcode(field2));
    }
  }

  void buildIndex3() {
    int size = capacity();
    index3 = new Index(size);
    for (int i=0 ; i < size ; i++) {
      int field2 = field2OrEmptyMarker(i);
      if (field2 != Empty)
        index3.insert(i, Miscellanea.hashcode(field3(i)));
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static class Iter {
    public enum Type {F123, F12, F13, F23, F1, F2, F3};

    int field1, field2, field3;

    int index;
    Type type;

    TernaryTable table;

    public Iter(int field1, int field2, int field3, int index, Type type, TernaryTable table) {
      this.field1 = field1;
      this.field2 = field2;
      this.field3 = field3;
      this.index = index;
      this.type = type;
      this.table = table;
      if (index != Empty) {
        int field2OrEmptyMarker = table.field2OrEmptyMarker(index);
        boolean ok1 = field1 == Empty | table.field1OrNext(index) == field1;
        boolean ok2 = field2 == Empty | field2OrEmptyMarker == field2;
        boolean ok3 = field3 == Empty | table.field3(index) == field3;
        if ((type == Type.F123 & field2OrEmptyMarker == Empty) | !ok1 | !ok2 | !ok3) {
          next();
        }
      }
    }

    public boolean done() {
      return index == Empty;
    }

    public int get1() {
      Miscellanea._assert(index != Empty);
      return table.field1OrNext(index);
    }

    public int get2() {
      Miscellanea._assert(index != Empty);
      return table.field2OrEmptyMarker(index);
    }

    public int get3() {
      Miscellanea._assert(index != Empty);
      return table.field3(index);
    }

    public void next() {
      Miscellanea._assert(index != Empty);
      switch (type) {
        case F123:
          int size = table.capacity();
          do {
            index++;
            if (index == size) {
              index = Empty;
              return;
            }
          } while (table.field2OrEmptyMarker(index) == Empty);
          break;

        case F12:
          for ( ; ; ) {
            index = table.index12.next(index);
            if (index == Empty)
              return;
            if (table.field1OrNext(index) == field1 & table.field2OrEmptyMarker(index) == field2)
              return;
          }

        case F13:
          for ( ; ; ) {
            index = table.index13.next(index);
            if (index == Empty)
              return;
            if (table.field1OrNext(index) == field1 & table.field3(index) == field3)
              return;
          }

        case F23:
          for ( ; ; ) {
            index = table.index23.next(index);
            if (index == Empty)
              return;
            if (table.field2OrEmptyMarker(index) == field2 & table.field3(index) == field3)
              return;
          }

        case F1:
          do {
            index = table.index1.next(index);
          } while (index != Empty && table.field1OrNext(index) != field1);
          break;

        case F2:
          do {
            index = table.index2.next(index);
          } while (index != Empty && table.field2OrEmptyMarker(index) != field2);
          break;

        case F3:
          do {
            index = table.index3.next(index);
          } while (index != Empty && table.field3(index) != field3);
          break;
      }
    }

    public void dump() {
      System.out.printf("fields = (%d, %d, %d)", field1, field2, field3);
      System.out.printf("index  = %d", index);
      System.out.printf("type   = %s", type.toString());
      System.out.printf("done() = %s", done() ? "true" : "false");
    }
  }
}
