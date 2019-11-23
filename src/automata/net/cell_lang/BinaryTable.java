package net.cell_lang;


class BinaryTable {
  OneWayBinTable table1 = new OneWayBinTable();
  OneWayBinTable table2 = new OneWayBinTable();

  public SurrObjMapper mapper1, mapper2;


  public BinaryTable(SurrObjMapper mapper1, SurrObjMapper mapper2) {
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

  public boolean insert(int surr1, int surr2) {
    boolean wasNew = table1.insert(surr1, surr2);
    if (wasNew && table2.count > 0)
      table2.insertUnique(surr2, surr1);
    check();
    return wasNew;
  }

  public void clear() {
    table1 = new OneWayBinTable();
    table2 = new OneWayBinTable();
    check();
  }

  public boolean delete(int surr1, int surr2) {
    boolean wasThere = table1.delete(surr1, surr2);
    if (wasThere & table2.count > 0)
      table2.delete(surr2, surr1);
    check();
    return wasThere;
  }

  public void delete1(int surr1, int[] surrs2) {
    int count = table1.count(surr1);
    table1.deleteByKey(surr1, surrs2);
    if (table2.count != 0)
      for (int i=0 ; i < count ; i++)
        table2.delete(surrs2[i], surr1);
  }

  public void delete2(int surr2, int[] surrs1) {
    if (table2.count == 0 & table1.count > 0)
      table2.initReverse(table1);
    int count = table2.count(surr2);
    table2.deleteByKey(surr2, surrs1);
    for (int i=0 ; i < count ; i++)
      table1.delete(surrs1[i], surr2);
  }

  public Obj copy(boolean flipped) {
    return copy(new BinaryTable[] {this}, flipped);
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

    int[] buffer = new int[32];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      BinaryTable table = tables[iT];
      OneWayBinTable oneWayTable = table.table1;

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
          Miscellanea._assert(_count1 == count1);
          for (int i=0 ; i < count1 ; i++) {
            objs1[next] = obj1;
            objs2[next++] = mapper2.surrToObj(buffer[i]);
          }
        }
      }
    }
    Miscellanea._assert(next == count);

    return Builder.createBinRel(flipped ? objs2 : objs1, flipped ? objs1 : objs2); //## THIS COULD BE MADE MORE EFFICIENT
  }

  //////////////////////////////////////////////////////////////////////////////

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
}
