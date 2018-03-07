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

    override public string ToString() {
      return "(" + field1OrNext.ToString() + ", " + field2OrEmptyMarker.ToString() + ", " + field3.ToString() + ")";
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
        bool ok1 = field1 == Tuple.Empty | tuple.field1OrNext == field1;
        bool ok2 = field2 == Tuple.Empty | tuple.field2OrEmptyMarker == field2;
        bool ok3 = field3 == Tuple.Empty | tuple.field3 == field3;
        if ((type == Type.F123 & tuple.field2OrEmptyMarker == Tuple.Empty) | !ok1 | !ok2 | !ok3) {
          Next();
        }
      }
    }

    public bool Done() {
      return index == Tuple.Empty;
    }

    public Tuple Get() {
      Miscellanea.Assert(index != Tuple.Empty);
      return table.tuples[index];
    }

    public int GetField1() {
      Miscellanea.Assert(index != Tuple.Empty);
      return table.tuples[index].field1OrNext;
    }

    public int GetField2() {
      Miscellanea.Assert(index != Tuple.Empty);
      return table.tuples[index].field2OrEmptyMarker;
    }

    public int GetField3() {
      Miscellanea.Assert(index != Tuple.Empty);
      return table.tuples[index].field3;
    }

    public void Next() {
      Miscellanea.Assert(index != Tuple.Empty);
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
            index = table.index12.Next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2)
              return;
          }

        case Type.F13:
          for ( ; ; ) {
            index = table.index13.Next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field1OrNext == field1 & tuple.field3 == field3)
              return;
          }

        case Type.F23:
          for ( ; ; ) {
            index = table.index23.Next(index);
            if (index == Tuple.Empty)
              return;
            Tuple tuple = table.tuples[index];
            if (tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
              return;
          }

        case Type.F1:
          do {
            index = table.index1.Next(index);
          } while (index != Tuple.Empty && table.tuples[index].field1OrNext != field1);
          break;

        case Type.F2:
          do {
            index = table.index2.Next(index);
          } while (index != Tuple.Empty && table.tuples[index].field2OrEmptyMarker != field2);
          break;

        case Type.F3:
          do {
            index = table.index3.Next(index);
          } while (index != Tuple.Empty && table.tuples[index].field3 != field3);
          break;
      }
    }

    public void Dump() {
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
      Miscellanea.Assert(tuples[i].field1OrNext == i + 1);
      Miscellanea.Assert(tuples[i].field2OrEmptyMarker == Tuple.Empty);
    }

    index123.Init(MinSize);
    index12.Init(MinSize);
  }

  public int Size() {
    return (int) count;
  }

  public void Insert(int field1, int field2, int field3) {
    if (Contains(field1, field2, field3))
      return;

    // Increasing the size of the table if need be
    if (firstFree >= tuples.length) {
      int size = tuples.length;
      Miscellanea.Assert(count == size);
      Tuple[] newTuples = new Tuple[2*size];
      Array.Copy(tuples, newTuples, size);
      for (int i=size ; i < 2 * size ; i++) {
        newTuples[i].field1OrNext = i + 1;
        newTuples[i].field2OrEmptyMarker = Tuple.Empty;
        Miscellanea.Assert(newTuples[i].field1OrNext == i + 1);
        Miscellanea.Assert(newTuples[i].field2OrEmptyMarker == Tuple.Empty);
      }
      tuples = newTuples;
      index123.Reset();
      index12.Reset();
      index13.Reset();
      index1.Reset();
      index2.Reset();
      index3.Reset();
      BuildIndex123();
      BuildIndex12();
    }

    // Inserting the new tuple
    int index = firstFree;
    firstFree = tuples[firstFree].field1OrNext;
    tuples[index] = new Tuple(field1, field2, field3);
    count++;

    // Updating the indexes
    index123.Insert(index, Miscellanea.Hashcode(field1, field2, field3));
    index12.Insert(index, Miscellanea.Hashcode(field1, field2));
    if (!index13.IsBlank())
      index13.Insert(index, Miscellanea.Hashcode(field1, field3));
    if (!index23.IsBlank())
      index23.Insert(index, Miscellanea.Hashcode(field2, field3));
    if (!index1.IsBlank())
      index1.Insert(index, Miscellanea.Hashcode(field1));
    if (!index2.IsBlank())
      index2.Insert(index, Miscellanea.Hashcode(field2));
    if (!index3.IsBlank())
      index3.Insert(index, Miscellanea.Hashcode(field3));

    // Updating the reference count in the value stores
    store1.AddRef(field1);
    store2.AddRef(field2);
    store3.AddRef(field3);
  }

  public void Clear() {
    count = 0;
    firstFree = 0;

    int size = tuples.length;
    for (int i=0 ; i < size ; i++) {
      tuples[i].field1OrNext = i + 1;
      tuples[i].field2OrEmptyMarker = Tuple.Empty;
    }

    index123.Clear();
    index12.Clear();
    index13.Clear();
    index23.Clear();
    index1.Clear();
    index2.Clear();
    index3.Clear();
  }

  public void Delete(int field1, int field2, int field3) {
    int hashcode = Miscellanea.Hashcode(field1, field2, field3);
    for (int idx = index123.Head(hashcode) ; idx != Tuple.Empty ; idx = index123.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3) {
        DeleteAt(idx, hashcode);
        return;
      }
    }
  }

  public bool Contains(long field1, long field2, long field3) {
    int hashcode = Miscellanea.Hashcode(field1, field2, field3);
    for (int idx = index123.Head(hashcode) ; idx != Tuple.Empty ; idx = index123.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public bool Contains12(int field1, int field2) {
    int hashcode = Miscellanea.Hashcode(field1, field2);
    for (int idx = index12.Head(hashcode) ; idx != Tuple.Empty ; idx = index12.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field2OrEmptyMarker == field2)
        return true;
    }
    return false;
  }

  public bool Contains13(int field1, int field3) {
    if (index13.IsBlank())
      BuildIndex13();
    int hashcode = Miscellanea.Hashcode(field1, field3);
    for (int idx = index13.Head(hashcode) ; idx != Tuple.Empty ; idx = index13.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public bool Contains23(int field2, int field3) {
    if (index23.IsBlank())
      BuildIndex23();
    int hashcode = Miscellanea.Hashcode(field2, field3);
    for (int idx = index23.Head(hashcode) ; idx != Tuple.Empty ; idx = index23.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field2OrEmptyMarker == field2 & tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public bool Contains1(int field1) {
    if (index1.IsBlank())
      BuildIndex1();
    int hashcode = Miscellanea.Hashcode(field1);
    for (int idx = index1.Head(hashcode) ; idx != Tuple.Empty ; idx = index1.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field1OrNext == field1)
        return true;
    }
    return false;
  }

  public bool Contains2(int field2) {
    if (index2.IsBlank())
      BuildIndex2();
    int hashcode = Miscellanea.Hashcode(field2);
    for (int idx = index2.Head(hashcode) ; idx != Tuple.Empty ; idx = index2.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field2OrEmptyMarker == field2)
        return true;
    }
    return false;
  }

  public bool Contains3(int field3) {
    if (index3.IsBlank())
      BuildIndex3();
    int hashcode = Miscellanea.Hashcode(field3);
    for (int idx = index3.Head(hashcode) ; idx != Tuple.Empty ; idx = index3.Next(idx)) {
      Tuple tuple = tuples[idx];
      if (tuple.field3 == field3)
        return true;
    }
    return false;
  }

  public Iter GetIter() {
    return new Iter(Tuple.Empty, Tuple.Empty, Tuple.Empty, 0, Iter.Type.F123, this);
  }

  public Iter GetIter12(long field1, long field2) {
    int hashcode = Miscellanea.Hashcode(field1, field2);
    return new Iter(field1, field2, Tuple.Empty, index12.Head(hashcode), Iter.Type.F12, this);
  }

  public Iter GetIter13(long field1, long field3) {
    if (index13.IsBlank())
      BuildIndex13();
    int hashcode = Miscellanea.Hashcode(field1, field3);
    return new Iter(field1, Tuple.Empty, field3, index13.Head(hashcode), Iter.Type.F13, this);
  }

  public Iter GetIter23(long field2, long field3) {
    if (index23.IsBlank())
      BuildIndex23();
    int hashcode = Miscellanea.Hashcode(field2, field3);
    return new Iter(Tuple.Empty, field2, field3, index23.Head(hashcode), Iter.Type.F23, this);
  }

  public Iter GetIter1(long field1) {
    if (index1.IsBlank())
      BuildIndex1();
    int hashcode = Miscellanea.Hashcode(field1);
    return new Iter(field1, Tuple.Empty, Tuple.Empty, index1.Head(hashcode), Iter.Type.F1, this);
  }

  public Iter GetIter2(long field2) {
    if (index2.IsBlank())
      BuildIndex2();
    int hashcode = Miscellanea.Hashcode(field2);
    return new Iter(Tuple.Empty, field2, Tuple.Empty, index2.Head(hashcode), Iter.Type.F2, this);
  }

  public Iter GetIter3(long field3) {
    if (index3.IsBlank())
      BuildIndex3();
    int hashcode = Miscellanea.Hashcode(field3);
    return new Iter(Tuple.Empty, Tuple.Empty, field3, index3.Head(hashcode), Iter.Type.F3, this);
  }

  public Obj Copy(int idx1, int idx2, int idx3) {
    if (count == 0)
      return EmptyRelObj.Singleton();

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int len = tuples.length;
    int next = 0;
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty) {
        objs1[next] = store1.GetValue(tuple.field1OrNext);
        objs2[next] = store2.GetValue(tuple.field2OrEmptyMarker);
        objs3[next] = store3.GetValue(tuple.field3);
        next++;
      }
    }
    Miscellanea.Assert(next == count);

    Obj[][] cols = new Obj[3][];
    cols[idx1] = objs1;
    cols[idx2] = objs2;
    cols[idx3] = objs3;

    return Builder.CreateTernRel(cols[0], cols[1], cols[2], count);
  }

  ////////////////////////////////////////////////////////////////////////////

  void DeleteAt(int index, int hashcode) {
    Tuple tuple = tuples[index];
    Miscellanea.Assert(tuple.field2OrEmptyMarker != Tuple.Empty);

    // Removing the tuple
    tuples[index].field1OrNext = firstFree;
    tuples[index].field2OrEmptyMarker = Tuple.Empty;
    Miscellanea.Assert(tuples[index].field1OrNext == firstFree);
    Miscellanea.Assert(tuples[index].field2OrEmptyMarker == Tuple.Empty);
    firstFree = index;
    count--;

    // Updating the indexes
    index123.Delete(index, hashcode);
    index12.Delete(index, Miscellanea.Hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker));
    if (!index13.IsBlank())
      index13.Delete(index, Miscellanea.Hashcode(tuple.field1OrNext, tuple.field3));
    if (!index23.IsBlank())
      index23.Delete(index, Miscellanea.Hashcode(tuple.field2OrEmptyMarker, tuple.field3));
    if (!index1.IsBlank())
      index1.Delete(index, Miscellanea.Hashcode(tuple.field1OrNext));
    if (!index2.IsBlank())
      index2.Delete(index, Miscellanea.Hashcode(tuple.field2OrEmptyMarker));
    if (!index3.IsBlank())
      index3.Delete(index, Miscellanea.Hashcode(tuple.field3));

    // Updating the reference count in the value stores
    store1.Release(tuple.field1OrNext);
    store2.Release(tuple.field2OrEmptyMarker);
    store3.Release(tuple.field3);
  }

  void BuildIndex123() {
    int len = tuples.length;
    index123.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index123.Insert(i, Miscellanea.Hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3));
    }
  }

  void BuildIndex12() {
    int len = tuples.length;
    index12.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index12.Insert(i, Miscellanea.Hashcode(tuple.field1OrNext, tuple.field2OrEmptyMarker));
    }
  }

  void BuildIndex13() {
    int len = tuples.length;
    index13.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index13.Insert(i, Miscellanea.Hashcode(tuple.field1OrNext, tuple.field3));
    }
  }

  void BuildIndex23() {
    int len = tuples.length;
    index23.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty) {
        int hashcode = Miscellanea.Hashcode(tuple.field2OrEmptyMarker, tuple.field3);
        index23.Insert(i, hashcode);
      }
    }
  }

  void BuildIndex1() {
    int len = tuples.length;
    index1.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index1.Insert(i, Miscellanea.Hashcode(tuple.field1OrNext));
    }
  }

  void BuildIndex2() {
    int len = tuples.length;
    index2.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index2.Insert(i, Miscellanea.Hashcode(tuple.field2OrEmptyMarker));
    }
  }

  void BuildIndex3() {
    int len = tuples.length;
    index3.Init(len);
    for (int i=0 ; i < len ; i++) {
      Tuple tuple = tuples[i];
      if (tuple.field2OrEmptyMarker != Tuple.Empty)
        index3.Insert(i, Miscellanea.Hashcode(tuple.field3));
    }
  }
}
