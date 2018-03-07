package net.cell_lang;


class TernaryTable {
  public struct Tuple {
    public final int Empty = 0xFFFFFFFF;

    public int field1OrNext;
    public int field2OrEmptyMarker;
    public int field3;

    public Tuple(int field1, int field2, int field3) {
      this.field1OrNext = field1;
      this.field2OrEmptyMarker = field2;
      this.field3 = field3;
    }

    @Override
    public String toString() {
      return "(" + field1OrNext.toString() + ", " + field2OrEmptyMarker.toString() + ", " + field3.toString() + ")";
    }
  }


  public struct Iter {
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
      if (index != Tuple.Empty) {
        Tuple tuple = table.tuples[index];
        boolean ok1 = field1 == Tuple.Empty | tuple.field1OrNext == field1;
        boolean ok2 = field2 == Tuple.Empty | tuple.field2OrEmptyMarker == field2;
        boolean ok3 = field3 == Tuple.Empty | tuple.field3 == field3;
        if ((type == Type.F123 & tuple.field2OrEmptyMarker == Tuple.Empty) | !ok1 | !ok2 | !ok3) {
          Next();
        }
      }
    }

    public boolean done() {
      return index == Tuple.Empty;
    }

    public Tuple get() {
      Miscellanea._assert(index != Tuple.Empty);
      return table.tuples[index];
    }

    public int getField1() {
      Miscellanea._assert(index != Tuple.Empty);
      return table.tuples[index].field1OrNext;
    }

    public int getField2() {
      Miscellanea._assert(index != Tuple.Empty);
      return table.tuples[index].field2OrEmptyMarker;
    }

    public int getField3() {
      Miscellanea._assert(index != Tuple.Empty);
      return table.tuples[index].field3;
    }

    public void next() {
      Miscellanea._assert(index != Tuple.Empty);
      switch (type) {
        case Type.F123:
          int len = table.tuples.length;
          do {
            index++;
            if (index == len) {
              index = Tuple.Empty;
              return;
            }
          } while (table.tuples[index].field2OrEmptyMarker == Tuple.Empty);
          break;

        case Type.F12:
          for ( ; ; ) {
            index = table.index12.next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2)
              return;
          }

        case Type.F13:
          for ( ; ; ) {
            index = table.index13.next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field1OrNext == field1 & tuple.field3 == field3)
              return;
          }

        case Type.F23:
          for ( ; ; ) {
            index = table.index23.next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
              return;
          }

        case Type.F1:
          do {
            index = table.index1.next(index);
          } while (index != Tuple.Empty && table.tuples[index].field1OrNext != field1);
          break;

        case Type.F2:
          do {
            index = table.index2.next(index);
          } while (index != Tuple.Empty && table.tuples[index].field2OrEmptyMarker != field2);
          break;

        case Type.F3:
          do {
            index = table.index3.next(index);
          } while (index != Tuple.Empty && table.tuples[index].field3 != field3);
          break;
      }
    }

    public void dump() {
      System.out.println("fields = ({0}, {1}, {2})", field1, field2, field3);
      System.out.println("index  = {0}", index);
      System.out.println("type   = {0}", type);
      System.out.println("Done() = {0}", Done());
    }
  }


  final int MinSize = 32;

  Tuple[] tuples = new Tuple[MinSize];
  public int count = 0;
  int firstFree = 0;

  public Index index123, index12, index13, index23, index1, index2, index3;

  public ValueStore store1, store2, store3;

  public TernaryTable(ValueStore store1, ValueStore store2, ValueStore store3) {
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;

    for (int i=0 ; i < MinSize ; i++) {
      tuples[i].field1OrNext = i + 1;
      tuples[i].field2OrEmptyMarker = Tuple.Empty;
    }

    for (int i=0 ; i < MinSize ; i++) {
      Miscellanea._assert(tuples[i].field1OrNext == i + 1);
      Miscellanea._assert(tuples[i].field2OrEmptyMarker == Tuple.Empty);
    }

    index123.init(MinSize);
    index12.init(MinSize);
  }

  public int size() {
    return (int) count;
  }

  public void insert(int field1, int field2, int field3) {
    if (Contains(field1, field2, field3))
      return;

    // Increasing the size of the table if need be
    if (firstFree >= tuples.length) {
      int size = tuples.length;
      Miscellanea._assert(count == size);
      Tuple[] newTuples = new Tuple[2*size];
      Miscellanea.arrayCopy(tuples, newTuples, size);
      for (int i=size ; i < 2 * size ; i++) {
        newTuples[i].field1OrNext = i + 1;
        newTuples[i].field2OrEmptyMarker = Tuple.Empty;
        Miscellanea._assert(newTuples[i].field1OrNext == i + 1);
        Miscellanea._assert(newTuples[i].field2OrEmptyMarker == Tuple.Empty);
      }
      tuples = newTuples;
      index123.reset();
      index12.reset();
      index13.reset();
      index1.reset();
      index2.reset();
      index3.reset();
      BuildIndex123();
      BuildIndex12();
    }

    // Inserting the new tuple
    int index = firstFree;
    firstFree = tuples[firstFree].field1OrNext;
    tuples[index] = new Tuple(field1, field2, field3);
    count++;

    // Updating the indexes
    index123.insert(index, Miscellanea.hashcode(field1, field2, field3));
    index12.insert(index, Miscellanea.hashcode(field1, field2));
    if (!index13.isBlank())
      index13.insert(index, Miscellanea.hashcode(field1, field3));
    if (!index23.isBlank())
      index23.insert(index, Miscellanea.hashcode(field2, field3));
    if (!index1.isBlank())
      index1.insert(index, Miscellanea.hashcode(field1));
    if (!index2.isBlank())
      index2.insert(index, Miscellanea.hashcode(field2));
    if (!index3.isBlank())
      index3.insert(index, Miscellanea.hashcode(field3));

    // Updating the reference count in the value stores
    store1.addRef(field1);
    store2.addRef(field2);
    store3.addRef(field3);
  }

  public void clear() {
    count = 0;
    firstFree = 0;

    int size = tuples.length;
    for (int i=0 ; i < size ; i++) {
      tuples[i].field1OrNext = i + 1;
      tuples[i].field2OrEmptyMarker = Tuple.Empty;
    }

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
    for (int idx = index123.head(hashcode) ; idx != Tuple.Empty ; idx = index123.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3) {
        DeleteAt(idx, hashcode);
        return;
      }
    }
  }

  public boolean contains(long field1, long field2, long field3) {
    int hashcode = Miscellanea.hashcode(field1, field2, field3);
    for (int idx = index123.head(hashcode) ; idx != Tuple.Empty ; idx = index123.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public boolean contains12(int field1, int field2) {
    int hashcode = Miscellanea.hashcode(field1, field2);
    for (int idx = index12.head(hashcode) ; idx != Tuple.Empty ; idx = index12.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2)
        return true;
    }
    return false;
  }

  public boolean contains13(int field1, int field3) {
    if (index13.isBlank())
      BuildIndex13();
    int hashcode = Miscellanea.hashcode(field1, field3);
    for (int idx = index13.head(hashcode) ; idx != Tuple.Empty ; idx = index13.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public boolean contains23(int field2, int field3) {
    if (index23.isBlank())
      BuildIndex23();
    int hashcode = Miscellanea.hashcode(field2, field3);
    for (int idx = index23.head(hashcode) ; idx != Tuple.Empty ; idx = index23.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public boolean contains1(int field1) {
    if (index1.isBlank())
      BuildIndex1();
    int hashcode = Miscellanea.hashcode(field1);
    for (int idx = index1.head(hashcode) ; idx != Tuple.Empty ; idx = index1.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1)
        return true;
    }
    return false;
  }

  public boolean contains2(int field2) {
    if (index2.isBlank())
      BuildIndex2();
    int hashcode = Miscellanea.hashcode(field2);
    for (int idx = index2.head(hashcode) ; idx != Tuple.Empty ; idx = index2.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field2OrEmptyMarker == field2)
        return true;
    }
    return false;
  }

  public boolean contains3(int field3) {
    if (index3.isBlank())
      BuildIndex3();
    int hashcode = Miscellanea.hashcode(field3);
    for (int idx = index3.head(hashcode) ; idx != Tuple.Empty ; idx = index3.next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public Iter getIter() {
    return new Iter(Tuple.Empty, Tuple.Empty, Tuple.Empty, 0, Iter.Type.F123, this);
  }

  public Iter getIter12(long field1, long field2) {
    int hashcode = Miscellanea.hashcode(field1, field2);
    return new Iter(field1, field2, Tuple.Empty, index12.head(hashcode), Iter.Type.F12, this);
  }

  public Iter getIter13(long field1, long field3) {
    if (index13.isBlank())
      BuildIndex13();
    int hashcode = Miscellanea.hashcode(field1, field3);
    return new Iter(field1, Tuple.Empty, field3, index13.head(hashcode), Iter.Type.F13, this);
  }

  public Iter getIter23(long field2, long field3) {
    if (index23.isBlank())
      BuildIndex23();
    int hashcode = Miscellanea.hashcode(field2, field3);
    return new Iter(Tuple.Empty, field2, field3, index23.head(hashcode), Iter.Type.F23, this);
  }

  public Iter getIter1(long field1) {
    if (index1.isBlank())
      BuildIndex1();
    int hashcode = Miscellanea.hashcode(field1);
    return new Iter(field1, Tuple.Empty, Tuple.Empty, index1.head(hashcode), Iter.Type.F1, this);
  }

  public Iter getIter2(long field2) {
    if (index2.isBlank())
      BuildIndex2();
    int hashcode = Miscellanea.hashcode(field2);
    return new Iter(Tuple.Empty, field2, Tuple.Empty, index2.head(hashcode), Iter.Type.F2, this);
  }

  public Iter getIter3(long field3) {
    if (index3.isBlank())
      BuildIndex3();
    int hashcode = Miscellanea.hashcode(field3);
    return new Iter(Tuple.Empty, Tuple.Empty, field3, index3.head(hashcode), Iter.Type.F3, this);
  }

  public Obj copy(int idx1, int idx2, int idx3) {
    if (count == 0)
      return EmptyRelObj.singleton();

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int len = tuples.length;
    int next = 0;
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty) {
        objs1[next] = store1.getValue(tuple.field1OrNext);
        objs2[next] = store2.getValue(tuple.field2OrEmptyMarker);
        objs3[next] = store3.getValue(tuple.field3);
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

  void DeleteAt(int index, int hashcode) {
    Tuple tuple = tuples[index];
    Miscellanea._assert(tuple.field2OrEmptyMarker != Tuple.Empty);

    // Removing the tuple
    tuples[index].field1OrNext = firstFree;
    tuples[index].field2OrEmptyMarker = Tuple.Empty;
    Miscellanea._assert(tuples[index].field1OrNext == firstFree);
    Miscellanea._assert(tuples[index].field2OrEmptyMarker == Tuple.Empty);
    firstFree = index;
    count--;

    // Updating the indexes
    index123.delete(index, hashcode);
    index12.delete(index, Miscellanea.hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker));
    if (!index13.isBlank())
      index13.delete(index, Miscellanea.hashcode(tuple.field1OrNext, tuple.field3));
    if (!index23.isBlank())
      index23.delete(index, Miscellanea.hashcode(tuple.field2OrEmptyMarker, tuple.field3));
    if (!index1.isBlank())
      index1.delete(index, Miscellanea.hashcode(tuple.field1OrNext));
    if (!index2.isBlank())
      index2.delete(index, Miscellanea.hashcode(tuple.field2OrEmptyMarker));
    if (!index3.isBlank())
      index3.delete(index, Miscellanea.hashcode(tuple.field3));

    // Updating the reference count in the value stores
    store1.release(tuple.field1OrNext);
    store2.release(tuple.field2OrEmptyMarker);
    store3.release(tuple.field3);
  }

  void BuildIndex123() {
    int len = tuples.length;
    index123.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index123.insert(i, Miscellanea.hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3));
    }
  }

  void BuildIndex12() {
    int len = tuples.length;
    index12.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index12.insert(i, Miscellanea.hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker));
    }
  }

  void BuildIndex13() {
    int len = tuples.length;
    index13.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index13.insert(i, Miscellanea.hashcode(tuple.field1OrNext, tuple.field3));
    }
  }

  void BuildIndex23() {
    int len = tuples.length;
    index23.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty) {
        int hashcode = Miscellanea.hashcode(tuple.field2OrEmptyMarker, tuple.field3);
        index23.insert(i, hashcode);
      }
    }
  }

  void BuildIndex1() {
    int len = tuples.length;
    index1.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index1.insert(i, Miscellanea.hashcode(tuple.field1OrNext));
    }
  }

  void BuildIndex2() {
    int len = tuples.length;
    index2.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index2.insert(i, Miscellanea.hashcode(tuple.field2OrEmptyMarker));
    }
  }

  void BuildIndex3() {
    int len = tuples.length;
    index3.init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index3.insert(i, Miscellanea.hashcode(tuple.field3));
    }
  }
}
