package net.cell_lang;


class AssocTable {
  private static final int MIN_SIZE = 32;

  private long[] slots = new long[MIN_SIZE];

  private int count = 0;
  private int firstFree = 0;

  private Index index = new Index(MIN_SIZE);

  private OneWayBinTable table1, table2;

  public ValueStore store1, store2;

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int arg1, int arg2) {
    if (!contains(arg1, arg2)) {
      if (count == slots.length)
        resize();
      count++;
      long tuple = filledSlot(arg1, arg2);
      int idx = firstFree;
      firstFree = next(slots[idx]);
      slots[idx] = tuple;
      index.insert(idx, Hashing.hashcode(arg1, arg2));
      if (table1 != null)
        table1.insert(arg1, arg2);
      if (table2 != null)
        table2.insert(arg2, arg1);
    }
  }

  private void resize() {
    Miscellanea._assert(count == slots.length);
    long[] currSlots = slots;
    slots = new long[2 * count];
    index = new Index(2 * count);
    for (int i=0 ; i < count ; i++) {
      long slot = currSlots[i];
      slots[i] = slot;
      index.insert(i, Hashing.hashcode(arg1(slot), arg2(slot)));
    }
    for (int i=count ; i < 2 * count ; i++)
      slots[i] = emptySlot(i + 1);
  }

  public void clear() {
    for (int i=0 ; i < slots.length ; i++)
      slots[i] = emptySlot(i+1);
    count = 0;
    firstFree = 0;
    index.clear();
    table1 = null;
    table2 = null;
  }

  public void delete(int arg1, int arg2) {
    int idx = tupleIdx(arg1, arg2);
    if (idx != -1) {
      count--;
      slots[idx] = emptySlot(firstFree);
      firstFree = idx;
      index.delete(idx, Hashing.hashcode(arg1, arg2));
      if (table1 != null)
        table1.delete(arg1, arg2);
      if (table2 != null)
        table2.delete(arg2, arg1);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  private static long emptySlot(int next) {
    Miscellanea._assert(next >= 0);
    long slot = 0xFFFFFFFF00000000L | next; //## ADD AN EXPLICIT CAST?
    Miscellanea._assert(isEmpty(slot));
    Miscellanea._assert(next(slot) == next);
    return slot;
  }

  private static long filledSlot(int arg1, int arg2) {
    Miscellanea._assert(arg1 >= 0 & arg1 <= 0x1FFFFFFF & arg2 >= 0 & arg2 <= 0x1FFFFFFF);
    long slot = (((long) arg2) << 32) | arg1;
    Miscellanea._assert(!isEmpty(slot));
    Miscellanea._assert(arg1(slot) == arg1);
    Miscellanea._assert(arg2(slot) == arg2);
    return slot;
  }

  private static int next(long slot) {
    Miscellanea._assert(isEmpty(slot));
    return (int) slot;
  }

  private static int arg1(long slot) {
    Miscellanea._assert(!isEmpty(slot));
    return (int) slot;
  }

  private static int arg2(long slot) {
    Miscellanea._assert(!isEmpty(slot));
    return (int) (slot >>> 32);
  }

  private static boolean isEmpty(long slot) {
    return (slot >> 32) == -1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public AssocTable(ValueStore store1, ValueStore store2) {
    for (int i=0 ; i < MIN_SIZE ; i++)
      slots[i] = emptySlot(i+1);
    this.store1 = store1;
    this.store2 = store2;
  }

  public int size() {
    return count;
  }

  public boolean contains(int arg1, int arg2) {
    return tupleIdx(arg1, arg2) != -1;
  }

  public int tupleIdx(int arg1, int arg2) {
    long tuple = filledSlot(arg1, arg2);
    int hashcode = Hashing.hashcode(arg1, arg2);
    for (int idx = index.head(hashcode) ; idx != Index.Empty ; idx = index.next(idx))
      if (slots[idx] == tuple)
        return idx;
    return -1;
  }

  public int arg1At(int index) {
    return arg1(slots[index]);
  }

  public int arg2At(int index) {
    return arg2(slots[index]);
  }

  public boolean contains1(int arg1) {
    if (table1 == null)
      buildTable1();
    return table1.containsKey(arg1);
  }

  public boolean contains2(int arg2) {
    if (table2 == null)
      buildTable2();
    return table2.containsKey(arg2);
  }

  public int count1(int arg1) {
    if (table1 == null)
      buildTable1();
    return table1.count(arg1);
  }

  public int count2(int arg2) {
    if (table2 == null)
      buildTable2();
    return table2.count(arg2);
  }

  public int[] restrict1(int surr) {
    if (table1 == null)
      buildTable1();
    return table1.restrict(surr);
  }

  public int[] restrict2(int surr) {
    if (table2 == null)
      buildTable2();
    return table2.restrict(surr);
  }

  public int lookup1(int surr) {
    if (table1 == null)
      buildTable1();
    return table1.lookup(surr);
  }

  public int lookup2(int surr) {
    if (table2 == null)
      buildTable2();
    return table2.lookup(surr);
  }

  public Iter getIter() {
    return new Iter(slots);
  }

  public ArrayIter getIter1(int arg1) {
    return new ArrayIter(restrict1(arg1));
  }

  public ArrayIter getIter2(int arg2) {
    return new ArrayIter(restrict2(arg2));
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj copy() {
    return copy(new AssocTable[] {this});
  }

  public int[] rawCopy() {
    return table1.copy();
  }

  //////////////////////////////////////////////////////////////////////////////

  private void buildTable1() {
    Miscellanea._assert(table1 == null);
    table1 = new OneWayBinTable();
    for (int i=0 ; i < slots.length ; i++) {
      long slot = slots[i];
      if (!isEmpty(slot))
        table1.insert(arg1(slot), arg2(slot));
    }
  }

  private void buildTable2() {
    Miscellanea._assert(table2 == null);
    table2 = new OneWayBinTable();
    for (int i=0 ; i < slots.length ; i++) {
      long slot = slots[i];
      if (!isEmpty(slot))
        table2.insert(arg2(slot), arg1(slot));
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(AssocTable[] tables) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      AssocTable table = tables[iT];
      long[] slots = table.slots;
      ValueStore store1 = table.store1;
      ValueStore store2 = table.store2;
      for (int iS=0 ; iS < slots.length ; iS++) {
        long slot = slots[iS];
        if (!isEmpty(slot)) {
          objs1[next]   = store1.surrToObjValue(arg1(slot));
          objs2[next++] = store2.surrToObjValue(arg2(slot));
        }
      }
    }
    Miscellanea._assert(next == count);

    return Builder.createBinRel(objs1, objs2); //## THIS COULD BE MADE MORE EFFICIENT
  }

  //////////////////////////////////////////////////////////////////////////////

  public static class Iter {
    long[] slots;
    int idx;

    public Iter(long[] slots) {
      this.slots = slots;
      idx = 0;
      while (idx < slots.length && isEmpty(slots[idx]))
        idx++;
    }

    public boolean done() {
      return idx >= slots.length;
    }

    public int get1() {
      return arg1(slots[idx]);
    }

    public int get2() {
      return arg2(slots[idx]);
    }

    public int getIdx() {
      return idx;
    }

    public void next() {
      if (idx < slots.length)
        do
          idx++;
        while (idx < slots.length && isEmpty(slots[idx]));
    }
  }
}
