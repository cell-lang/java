package net.cell_lang;


class BinaryTable {
  public static class Iter {
    int[][] entries;
    int next;
    int end;

    public Iter(int[][] entries) {
      this.entries = entries;
      next = 0;
      end = entries[0].length;
    }

    public boolean done() {
      return next >= end;
    }

    public int getField1() {
      return entries[0][next];
    }

    public int getField2() {
      return entries[1][next];
    }

    public void next() {
      next++;
    }
  }


  OneWayBinTable table1;
  OneWayBinTable table2;

  public ValueStore store1;
  public ValueStore store2;

  public BinaryTable(ValueStore store1, ValueStore store2) {
    table1.init();
    table2.init();
    this.store1 = store1;
    this.store2 = store2;
    // Check();
  }

  public void check() {
    table1.check();
    table2.check();
  }

  public int size() {
    return table1.count;
  }

  public boolean contains(long surr1, long surr2) {
    return table1.contains((int) surr1, (int) surr2);
  }

  public boolean containsField1(int surr1) {
    return table1.containsKey(surr1);
  }

  public boolean containsField2(int surr2) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.containsKey(surr2);
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

  public Iter getIter1(long surr1) {
    int[] col2 = lookupByCol1((int) surr1);
    int[][] entries = new int[][] {new int[col2.length], new int[col2.length]};
    for (int i=0 ; i < col2.length ; i++) {
      entries[0][i] = (int) surr1;
      entries[1][i] = col2[i];
    }
    return new Iter(entries);
  }

  public Iter getIter2(long surr2) {
    int[] col1 = lookupByCol2((int) surr2);
    int[][] entries = new int[][] {new int[col1.length], new int[col1.length]};
    for (int i=0 ; i < col1.length ; i++) {
      entries[0][i] = col1[i];
      entries[1][i] = (int) surr2;
    }
    return new Iter(entries);
  }

  public void insert(int surr1, int surr2) {
    table1.insert(surr1, surr2);
    if (table2.count > 0)
      table2.insert(surr2, surr1);
    // Check();
  }

  public void clear() {
    table1.init();
    table2.init();
    // Check();
  }

  public void delete(int surr1, int surr2) {
    table1.delete(surr1, surr2);
    if (table2.count > 0)
      table2.delete(surr2, surr1);
    // Check();
  }

  public Obj copy(boolean flipped) {
    int count = table1.count;

    if (count == 0)
      return EmptyRelObj.singleton();

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

  public int[][] rawCopy() {
    return table1.copy();
  }
}
