package net.cell_lang;


class BinaryTable {
  public static class Iter {
    int[] entries;
    boolean singleCol;
    int next;
    int end;

    public Iter(int[] entries, boolean singleCol) {
      this.entries = entries;
      this.singleCol = singleCol;
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
      Miscellanea._assert(!singleCol);
      return entries[next+1];
    }

    public void next() {
      next += singleCol ? 1 : 2;
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
    // table1.check();
    // table2.check();
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

  public int[] restrict1(int surr) {
    return table1.restrict(surr);
  }

  public int[] restrict2(int surr) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.restrict(surr);
  }

  public int lookup1(int surr) {
    return table1.lookup(surr);
  }

  public int lookup2(int surr) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    return table2.lookup(surr);
  }

  public Iter getIter() {
    return new Iter(table1.copy(), false);
  }

  public Iter getIter1(int surr1) {
    return new Iter(restrict1(surr1), true);
  }

  public Iter getIter2(int surr2) {
    return new Iter(restrict2(surr2), true);
  }

  public void insert(int surr1, int surr2) {
    table1.insert(surr1, surr2);
    if (table2.count > 0) //## BUG?
      table2.insert(surr2, surr1);
    check();
  }

  // Assuming there's at most one tuple that whose first argument is surr1
  public int update1(int surr1, int surr2) {
    int oldSurr2 = table1.update(surr1, surr2);
    if (oldSurr2 != -1 && oldSurr2 != surr2 && table2.count > 0) { //## BUG?
      table2.delete(oldSurr2, surr1);
      table2.insert(surr2, surr1);
    }
    check();
    return oldSurr2;
  }

  public void clear() {
    table1 = new OneWayBinTable();
    table2 = new OneWayBinTable();
    check();
  }

  public void delete(int surr1, int surr2) {
    table1.delete(surr1, surr2);
    if (table2.count > 0) //## BUG?
      table2.delete(surr2, surr1);
    check();
  }

  public Obj copy(boolean flipped) {
    return copy(new BinaryTable[] {this}, flipped);
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
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      BinaryTable table = tables[iT];
      int[] column = table.table1.column;
      ValueStore store1 = table.store1;
      ValueStore store2 = table.store2;
      for (int iS=0 ; iS < column.length ; iS++) {
        int code = column[iS];
        if (code != OverflowTable.EmptyMarker) {
          Obj val1 = store1.surrToObjValue(iS);
          if (code >> 29 == 0) {
            objs1[next] = val1;
            objs2[next++] = store2.surrToObjValue(code);
          }
          else {
            OverflowTable.Iter it = table.table1.overflowTable.getIter(code);
            while (!it.done()) {
              int arg2 = it.get();
              objs1[next] = val1;
              objs2[next++] = store2.surrToObjValue(arg2);
              it.next();
            }
          }
        }
      }
    }
    Miscellanea._assert(next == count);

    return Builder.createBinRel(flipped ? objs2 : objs1, flipped ? objs1 : objs2); //## THIS COULD BE MADE MORE EFFICIENT
  }
}
