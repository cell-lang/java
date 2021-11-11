package net.cell_lang;


final class MasterBinaryTable extends SurrPairStore {
  LoadedOneWayBinTable table1 = new LoadedOneWayBinTable();
  OneWayBinTable table2 = new OneWayBinTable();

  public SurrObjMapper mapper1, mapper2;


  public MasterBinaryTable(SurrObjMapper mapper1, SurrObjMapper mapper2) {
    this.mapper1 = mapper1;
    this.mapper2 = mapper2;
    check();
  }

  public void check() {
    // table1.check();
    // table2.check();
  }

  public int size() {
    return table1.count;
  }

  public boolean contains(int arg1, int arg2) {
    return table1.contains(arg1, arg2);
  }

  public boolean contains1(int arg1) {
    return table1.containsKey(arg1);
  }

  public boolean contains2(int arg2) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.containsKey(arg2);
  }

  public int count1(int arg1) {
    return table1.count(arg1);
  }

  public int count2(int arg2) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.count(arg2);
  }

  public int[] restrict1(int arg) {
    return table1.restrict(arg);
  }

  public int restrict1(int arg1, int[] args2) {
    return table1.restrict(arg1, args2);
  }

  public int[] restrict2(int arg) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.restrict(arg);
  }

  public int restrict2(int arg2, int[] args1) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.restrict(arg2, args1);
  }

  public int surrogate(int arg1, int arg2) {
    return table1.payload(arg1, arg2);
  }

  public int lookup1(int arg) {
    return table1.lookup(arg);
  }

  public int lookup2(int arg) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.lookup(arg);
  }

  public Iter getIter() {
    return new Iter(table1.copy(), false);
  }

  public Iter getIter1(int arg1) {
    return new Iter(restrict1(arg1), true);
  }

  public Iter getIter2(int arg2) {
    return new Iter(restrict2(arg2), true);
  }

  public boolean insert(int arg1, int arg2) {
    int code = insertEx(arg1, arg2);
    return code >= 0;
  }

  // Code to recover the surrogate:
  //   int code = masterBinaryTable.insertEx(arg1, arg2);
  //   int surr12 = code >= 0 ? code : -code - 1;
  public int insertEx(int arg1, int arg2) {
    int surr12 = table1.payload(arg1, arg2);
    if (surr12 != 0xFFFFFFFF)
      return -surr12 - 1;
    surr12 = allocateNew(arg1, arg2);
    table1.insertUnique(arg1, arg2, surr12);
    if (table2.count > 0)
      table2.insertUnique(arg2, arg1);
    check();
    return surr12;
  }

  public void clear() {
    table1 = new LoadedOneWayBinTable();
    table2 = new OneWayBinTable();
    clearSurrStore();
    check();
  }

  public boolean delete(int arg1, int arg2) {
    int data = table1.delete(arg1, arg2);
    if (data != 0xFFFFFFFF) {
      if (table2.count > 0)
        table2.delete(arg2, arg1);
      release(data);
      check();
      return true;
    }
    else
      return false;
  }

  public void delete1(int arg1, int[] args2) {
    int count = table1.count(arg1);
    if (count > 0) {
      int[] data = new int[count];
      table1.deleteByKey(arg1, args2, data);
      if (table2.count != 0)
        for (int i=0 ; i < count ; i++)
          table2.delete(args2[i], arg1);
      for (int i=0 ; i < count ; i++)
        release(data[i]);
    }
  }

  public void delete2(int arg2, int[] args1) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    int count = table2.count(arg2);
    table2.deleteByKey(arg2, args1);
    for (int i=0 ; i < count ; i++) {
      int data = table1.delete(args1[i], arg2);
      release(data);
    }
  }

  public Obj copy(boolean flipped) {
    return copy(new MasterBinaryTable[] {this}, flipped);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean col1IsKey() {
    return table1.isMap();
  }

  public boolean col2IsKey() {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.isMap();
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(MasterBinaryTable[] tables, boolean flipped) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];

    int[] buffer = new int[32];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      MasterBinaryTable table = tables[iT];
      LoadedOneWayBinTable oneWayTable = table.table1;

      SurrObjMapper mapper1 = table.mapper1;
      SurrObjMapper mapper2 = table.mapper2;

      int len = oneWayTable.column.length;
      for (int iS=0 ; iS < len ; iS++) {
        int count1 = oneWayTable.count(iS);
        if (count1 != 0) {
          if (count1 > buffer.length)
            buffer = new int[Array.capacity(buffer.length, count1)];
          Obj obj1 = mapper1.surrToObj(iS);
          int _count1 = oneWayTable.restrict(iS, buffer);
          // Miscellanea._assert(_count1 == count1);
          for (int i=0 ; i < count1 ; i++) {
            objs1[next] = obj1;
            objs2[next++] = mapper2.surrToObj(buffer[i]);
          }
        }
      }
    }
    // Miscellanea._assert(next == count);

    return Builder.createBinRel(flipped ? objs2 : objs1, flipped ? objs1 : objs2); //## THIS COULD BE MADE MORE EFFICIENT
  }

  //////////////////////////////////////////////////////////////////////////////

  public static class Iter extends BinaryTable.Iter {
    public Iter(int[] entries, boolean singleCol) {
      super(entries, singleCol);
    }
  }

  // public static class Iter {
  //   int[] entries;
  //   boolean singleCol;
  //   int next;
  //   int end;

  //   public Iter(int[] entries, boolean singleCol) {
  //     this.entries = entries;
  //     this.singleCol = singleCol;
  //     next = 0;
  //     end = entries.length;
  //   }

  //   public boolean done() {
  //     return next >= end;
  //   }

  //   public int get1() {
  //     return entries[next];
  //   }

  //   public int get2() {
  //     Miscellanea._assert(!singleCol);
  //     return entries[next+1];
  //   }

  //   public void next() {
  //     next += singleCol ? 1 : 2;
  //   }
  // }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class SurrPairStore {
  long[] slots = Array.emptyLongArray;
  int firstFree = 0;


  public static int left(long slot) {
    return ArraySliceAllocator.low(slot);
  }

  public static int right(long slot) {
    return ArraySliceAllocator.high(slot);
  }

  protected static long combine(int arg1, int arg2) {
    return ArraySliceAllocator.combine(arg1, arg2);
  }

  //////////////////////////////////////////////////////////////////////////////

  public final int arg1(int arg12) {
    return left(slots[arg12]);
  }

  public final int arg2(int arg12) {
    return right(slots[arg12]);
  }

  public final boolean isValidSurr(int surr12) {
    throw new RuntimeException();
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final long slot(int idx) {
    // Miscellanea._assert(slots[idx] >>> 29 == 0);
    return slots[idx];
  }

  protected final int allocateNew(int arg1, int arg2) {
    int size = slots.length;

    if (firstFree >= size) {
      // Miscellanea._assert(firstFree == size);
      int newSize = size > 0 ? 2 * size : 256;
      long[] newSlots = new long[newSize];
      Array.copy(slots, newSlots, size);
      for (int i=size ; i < newSize ; i++)
        newSlots[i] = i + 1;
      slots = newSlots;
    }

    int idx = firstFree;
    firstFree = (int) slots[firstFree];
    slots[idx] = combine(arg1, arg2);
    return idx;
  }

  protected final void release(int idx) {
    slots[idx] = firstFree;
    firstFree = idx;
  }

  protected final void clearSurrStore() {
    long[] slots = Array.emptyLongArray;
    int firstFree = 0;
  }
}