package net.cell_lang;


class BinaryTable {
  public static class Iter {
    int[] entries;
    int next;
    int end;

    public Iter(int[] entries) {
      this.entries = entries;
      next = 0;
      end = entries.length;
    }

    public boolean done() {
      return next >= end;
    }

    public int get1() {
      return entries[next];
    }

    public int get2() {
      return entries[next+1];
    }

    public void next() {
      next += 2;
    }
  }


  OneWayBinTable table1 = new OneWayBinTable();
  OneWayBinTable table2 = new OneWayBinTable();

  public ValueStore store1;
  public ValueStore store2;

  public BinaryTable(ValueStore store1, ValueStore store2) {
    this.store1 = store1;
    this.store2 = store2;
    check();
  }

  public void check() {
    table1.check();
    table2.check();
  }

  public int size() {
    return table1.count;
  }

  public boolean contains(int surr1, int surr2) {
    return table1.contains(surr1, surr2);
  }

  public boolean contains1(int surr1) {
    return table1.containsKey(surr1);
  }

  public boolean contains2(int surr2) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.containsKey(surr2);
  }

  public int count1(int surr1) {
    return table1.count(surr1);
  }

  public int count2(int surr2) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.count(surr2);
  }

  public int[] lookupByCol1(int surr) {
    return table1.lookup(surr);
  }

  public int[] lookupByCol2(int surr) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.lookup(surr);
  }

  public Iter getIter() {
    return new Iter(table1.copy());
  }

  public Iter getIter1(int surr1) {
    int[] col2 = lookupByCol1(surr1);
    int count = col2.length;
    int[] entries = new int[2 * count];
    for (int i=0 ; i < count ; i++) {
      entries[2 * i] = surr1;
      entries[2 * i + 1] = col2[i];
    }
    return new Iter(entries);
  }

  public Iter getIter2(int surr2) {
    int[] col1 = lookupByCol2(surr2);
    int count = col1.length;
    int[] entries = new int[2 * count];
    for (int i=0 ; i < count ; i++) {
      entries[2 * i] = col1[i];
      entries[2 * i + 1] = surr2;
    }
    return new Iter(entries);
  }

  public void insert(int surr1, int surr2) {
    table1.insert(surr1, surr2);
    if (table2.count > 0)
      table2.insert(surr2, surr1);
    check();
  }

  public void clear() {
    table1 = new OneWayBinTable();
    table2 = new OneWayBinTable();
    check();
  }

  public void delete(int surr1, int surr2) {
    table1.delete(surr1, surr2);
    if (table2.count > 0)
      table2.delete(surr2, surr1);
    check();
  }

  public Obj copy(boolean flipped) {
    int count = table1.count;

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];

    int next = 0;
    for (int i=0 ; i < table1.column.length ; i++) {
      int code = table1.column[i];
      if (code != OverflowTable.EmptyMarker) {
        Obj val1 = store1.getValue(i);
        if (code >> 29 == 0) {
          objs1[next] = val1;
          objs2[next++] = store2.getValue(code);
        }
        else {
          OverflowTable.Iter it = table1.overflowTable.getIter(code);
          while (!it.done()) {
            int surr2 = it.get();
            objs1[next] = val1;
            objs2[next++] = store2.getValue(surr2);
            it.next();
          }
        }
      }
    }
    Miscellanea._assert(next == count);

    return Builder.createBinRel(flipped ? objs2 : objs1, flipped ? objs1 : objs2, count); //## THIS COULD BE MADE MORE EFFICIENT
  }

  public int[] rawCopy() {
    return table1.copy();
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

  public static Obj copy(BinaryTable[] tables, boolean flipped) {
    throw new RuntimeException();
  }
}
