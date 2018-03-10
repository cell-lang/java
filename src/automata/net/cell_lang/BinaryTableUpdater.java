package net.cell_lang;

import java.util.ArrayList;
import java.util.ListIterator;


class BinaryTableUpdater {
  static class Tuple {
    public int field1;
    public int field2;

    public Tuple(int field1, int field2) {
      this.field1 = field1;
      this.field2 = field2;
    }

    public Tuple(long field1, long field2) {
      this((int) field1, (int) field2);
    }

    @Override
    public String toString() {
      return "(" + Integer.toString(field1) + ", " + Integer.toString(field2) + ")";
    }

    public static int compareLeftToRigth(Tuple t1, Tuple t2) {
      return (t1.field1 != t2.field1 ? t1.field1 - t2.field1 : t1.field2 - t2.field2);
    };

    public static int compareRightToLeft(Tuple t1, Tuple t2) {
      return (t1.field2 != t2.field2 ? t1.field2 - t2.field2 : t1.field1 - t2.field1);
    };
  }

  ArrayList<Tuple> deleteList = new ArrayList<Tuple>();
  ArrayList<Tuple> insertList = new ArrayList<Tuple>();

  BinaryTable table;
  ValueStoreUpdater store1;
  ValueStoreUpdater store2;

  public BinaryTableUpdater(BinaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
  }

  public void clear() {
    int[][] columns = table.rawCopy();
    int len = columns[0].length;
    deleteList.clear();
    for (int i=0 ; i < len ; i++)
      deleteList.add(new Tuple(columns[0][i], columns[1][i]));
  }

  public void set(Obj value, boolean flipped) {
    clear();
    Miscellanea._assert(insertList.size() == 0);
    BinRelIter it = value.getBinRelIter();
    while (!it.done()) {
      Obj val1 = flipped ? it.get2() : it.get1();
      Obj val2 = flipped ? it.get1() : it.get2();
      int surr1 = store1.lookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.insert(val1);
      int surr2 = store2.lookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.insert(val2);
      insertList.add(new Tuple(surr1, surr2));
      it.next();
    }
  }

  public void delete(int value1, int value2) {
    if (table.contains(value1, value2))
      deleteList.add(new Tuple(value1, value2));
  }

  public void delete1(int value) {
    int[] assocs = table.lookupByCol1((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList.add(new Tuple(value, assocs[i]));
  }

  public void delete2(int value) {
    int[] assocs = table.lookupByCol2((int) value);
    for (int i=0 ; i < assocs.length ; i++)
      deleteList.add(new Tuple(assocs[i], value));
  }

  public void insert(int value1, int value2) {
    insertList.add(new Tuple(value1, value2));
  }

  public boolean checkUpdates_1() {
    deleteList.sort(Tuple::compareLeftToRigth);
    insertList.sort(Tuple::compareLeftToRigth);

    int count = insertList.size();
    if (count == 0)
      return true;

    Tuple prev = insertList.get(0);
    if (!containsField1(deleteList, prev.field1))
      if (table.containsField1(prev.field1))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList.get(i);
      if (curr.field1 == prev.field1 & curr.field2 != prev.field2)
        return false;
      if (!containsField1(deleteList, curr.field1))
        if (table.containsField1(curr.field1))
          return false;
      prev = curr;
    }

    return true;
  }

  public boolean checkUpdates_1_2() {
    if (!checkUpdates_1())
      return false;

    deleteList.sort(Tuple::compareRightToLeft);
    insertList.sort(Tuple::compareRightToLeft);

    int count = insertList.size();
    if (count == 0)
      return true;

    Tuple prev = insertList.get(0);
    if (!containsField2(deleteList, prev.field2))
      if (table.containsField2(prev.field2))
        return false;

    for (int i=1 ; i < count ; i++) {
      Tuple curr = insertList.get(i);
      if (curr.field2 == prev.field2 & curr.field1 != prev.field1)
        return false;
      if (!containsField2(deleteList, curr.field2))
        if (table.containsField2(curr.field2))
          return false;
      prev = curr;
    }

    return true;
  }

  public void apply() {
    for (int i=0 ; i < deleteList.size() ; i++) {
      Tuple tuple = deleteList.get(i);
      if (table.contains(tuple.field1, tuple.field2)) {
        table.delete(tuple.field1, tuple.field2);
      }
      else
        deleteList.set(i, new Tuple(0xFFFFFFFF, 0xFFFFFFFF));
    }

    ListIterator<Tuple> it = insertList.listIterator();
    while (it.hasNext()) {
      Tuple curr = it.next();
      if (!table.contains(curr.field1, curr.field2)) {
        table.insert(curr.field1, curr.field2);
        table.store1.addRef(curr.field1);
        table.store2.addRef(curr.field2);
      }
    }
  }

  public void finish() {
    ListIterator<Tuple> it = deleteList.listIterator();
    while (it.hasNext()) {
      Tuple tuple = it.next();
      if (tuple.field1 != 0xFFFFFFFF) {
        Miscellanea._assert(table.store1.lookupSurrogate(tuple.field1) != null);
        Miscellanea._assert(table.store2.lookupSurrogate(tuple.field2) != null);
        table.store1.release(tuple.field1);
        table.store2.release(tuple.field2);
      }
    }
    reset();
  }

  public void reset() {
    deleteList.clear();
    insertList.clear();
  }

  public void dump() {
    System.out.print("deleteList =");
    for (int i=0 ; i < deleteList.size() ; i++)
      System.out.print(" " + deleteList.get(i).toString());
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertList.size() ; i++)
      System.out.print(" " + insertList.get(i).toString());
    System.out.println("\n");

    System.out.print("deleteList =");
    for (int i=0 ; i < deleteList.size() ; i++) {
      Tuple tuple = deleteList.get(i);
      Obj obj1 = store1.lookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.lookupSurrogateEx(tuple.field2);
      System.out.printf(" (%s, %s)", obj1.toString(), obj2.toString());
    }
    System.out.println("");

    System.out.print("insertList =");
    for (int i=0 ; i < insertList.size() ; i++) {
      Tuple tuple = insertList.get(i);
      Obj obj1 = store1.lookupSurrogateEx(tuple.field1);
      Obj obj2 = store2.lookupSurrogateEx(tuple.field2);
      System.out.printf(" (%s, %s)", obj1.toString(), obj2.toString());
    }
    //## WHY THE ARGUMENT TO BinaryTable.copy(boolean flipped) IS SET TO true?
    System.out.printf("\n\n%s\n\n", table.copy(true).toString());

    store1.dump();
    store2.dump();
  }

  static boolean containsField1(ArrayList<Tuple> tuples, int field1) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField1 = tuples.get(mid).field1;
      if (midField1 > field1)
        high = mid - 1;
      else if (midField1 < field1)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static boolean containsField2(ArrayList<Tuple> tuples, int field2) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField2 = tuples.get(mid).field2;
      if (midField2 > field2)
        high = mid - 1;
      else if (midField2 < field2)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }
}
