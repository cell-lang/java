package net.cell_lang;

import java.util.Arrays;


// May have a key on the first two columns, and another one on the third one, but nothing else
// It must also have a foreign key from the first two columns to an AssocTable
class SlaveTernTable {
  private OneWayBinTable table = new OneWayBinTable();
  private OneWayBinTable revTable;
  private AssocTable master;
  public  ValueStore store1, store2, store3;

  //////////////////////////////////////////////////////////////////////////////

  public SlaveTernTable(AssocTable master, ValueStore store1, ValueStore store2, ValueStore store3) {
    Miscellanea._assert(master != null);
    this.master = master;
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void insert(int arg1, int arg2, int arg3) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    table.insert(masterIdx, arg3);
    if (revTable != null)
      revTable.insert(arg3, masterIdx);
  }

  public void clear() {
    table = new OneWayBinTable();
    revTable = null;
  }

  public void delete(int arg1, int arg2, int arg3) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    if (masterIdx != -1) {
      table.delete(masterIdx, arg3);
      if (revTable != null)
        revTable.delete(arg3, masterIdx);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public int size() {
    return table.count;
  }

  public boolean contains(int arg1, int arg2, int arg3) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    return masterIdx != -1 && table.contains(masterIdx, arg3);
  }

  public boolean contains12(int arg1, int arg2) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    return masterIdx != -1 && table.containsKey(masterIdx);
  }

  // public boolean contains1(int arg1) {
  // }

  // public boolean contains2(int arg2) {
  // }

  public boolean contains3(int arg3) {
    if (revTable == null)
      buildRevTable();
    return revTable.containsKey(arg3);
  }

  public int lookup12(int arg1, int arg2) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    return masterIdx != -1 ? table.lookup(masterIdx) : -1;
  }

  public int count12(int arg1, int arg2) {
    int masterIdx = master.tupleIdx(arg1, arg2);
    return masterIdx != -1 ? table.count(masterIdx) : 0;
  }

  public int count3(int arg3) {
    if (revTable == null)
      buildRevTable();
    return revTable.count(arg3);
  }

  public Iter getIter() {
    return new Iter();
  }

  public ArrayIter getIter12(int arg1, int arg2) {
    int idx = master.tupleIdx(arg1, arg2);
    return new ArrayIter(idx != -1 ? table.restrict(idx) : Miscellanea.emptyIntArray);
  }

  public Iter3 getIter3(int arg3) {
    if (revTable == null)
      buildRevTable();
    return new Iter3(revTable.restrict(arg3));
  }

  public Obj copy() {
    return copy(new SlaveTernTable[] {this});
  }

  ////////////////////////////////////////////////////////////////////////////

  // public boolean col3IsKey() {
  // }

  // public boolean cols12AreKey() {
  // }

  //////////////////////////////////////////////////////////////////////////////

  private void buildRevTable() {
    Miscellanea._assert(revTable == null);
    revTable = new OneWayBinTable();
    revTable.initReverse(table);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(SlaveTernTable[] tables) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].table.count;

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      SlaveTernTable table = tables[iT];
      ValueStore store1 = table.store1;
      ValueStore store2 = table.store2;
      ValueStore store3 = table.store3;

      for (AssocTable.Iter masterIt = table.master.getIter() ; !masterIt.done() ; masterIt.next()) {
        int surr1 = masterIt.get1();
        int surr2 = masterIt.get2();
        for (ArrayIter it = table.getIter12(surr1, surr2) ; it.done() ; it.next()) {
          int surr3 = it.get();
          objs1[next] = store1.surrToObjValue(surr1);
          objs2[next] = store2.surrToObjValue(surr2);
          objs3[next++] = store3.surrToObjValue(surr3);
        }
      }
    }
    Miscellanea._assert(next == count);

    return Builder.createTernRel(objs1, objs2, objs3, count);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final class Iter {
    int range12;
    int idx12;
    int[] values3 = new int[1];
    int count3;
    int idx3;

    public Iter() {
      range12 = table.column.length;
      idx12 = -1;
      next();
    }

    public void next() {
      Miscellanea._assert(idx12 < range12);

      if (++idx3 < count3)
        return;

      while (++idx12 < range12) {
        int count = table.count(idx12);
        if (count > 0) {
          int size = values3.length;
          if (size < count) {
            size = Miscellanea.extend(size, count);
            values3 = new int[size];
          }
          count3 = table.restrict(idx12, values3);
          idx3 = 0;
          return;
        }
      }
    }

    public boolean done() {
      return idx12 >= range12;
    }

    public int get1() {
      Miscellanea._assert(!done());
      return master.arg1At(idx12);
    }

    public int get2() {
      Miscellanea._assert(!done());
      return master.arg2At(idx12);
    }

    public int get3() {
      Miscellanea._assert(!done());

if (idx3 >= values3.length) {
  System.out.printf("idx3 = %d, count3 = %d, values3.length = %d\n", idx3, count3, values3.length);
}

      return values3[idx3];
    }
  }

  public final class Iter3 {
    int[] idxs;
    int idx;

    public Iter3(int[] idxs) {
      this.idxs = idxs;
    }

    public boolean done() {
      return idx >= idxs.length;
    }

    public void next() {
      Miscellanea._assert(!done());
      idx++;
    }

    public int get1() {
      return master.arg1At(idxs[idx]);
    }

    public int get2() {
      return master.arg2At(idxs[idx]);
    }
  }
}
