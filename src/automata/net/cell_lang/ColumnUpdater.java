package net.cell_lang;

import java.util.function.IntPredicate;


//      Column      Update map    Flagged   Description             Deletions   Insertions
//
// A    null        -             -         Untouched               -           -> B
// B    null        not null      -         Value set               -           ERROR!
//
// C    not null    -             -         Untouched               -> D        -> E
// D    not null    -             Yes       Value deleted           -           -> F
// E    not null    not null      Yes       Waiting for delete      -> F        ERROR!
// F    not null    not null      -         Value set               -           ERROR!


class ColumnUpdater {
  IntObjMap updateMap = new IntObjMap();
  SurrSet flagged = new SurrSet();
  int waitingForDelete = 0;
  int maxIndex = -1;

  Obj[] column;
  ColSetter colSetter;

  public ColumnUpdater(Obj[] column, ColSetter colSetter) {
    this.column = column;
    this.colSetter = colSetter;
  }

  public void clear() {
    throw new RuntimeException();
  }

  public void delete1(int index) {
    if (index < column.length && column[index] != null) {
      if (updateMap.hasKey(index)) {
        if (flagged.includes(index)) {
          // E -> F
          flagged.remove(index);
          waitingForDelete--;
        }
      }
      else
        // C -> D
        flagged.insert(index);
    }
  }

  public void insert(int index, Obj value) {
    if (updateMap.hasKey(index))
      Miscellanea.softFail();

    updateMap.insert(index, value);
    if (index > maxIndex)
      maxIndex = index;

    if (index < column.length && column[index] != null) {
      if (flagged.includes(index))
        // D -> F
        flagged.remove(index);
      else {
        // C -> E
        flagged.insert(index);
        waitingForDelete++;
      }
    }
    else {
      // A -> B
      Miscellanea._assert(!flagged.includes(index));
    }
  }

  public void apply() {
    if (maxIndex >= column.length) {
      int newSize = Miscellanea.extend(column.length, maxIndex + 1);
      Obj[] newCol = new Obj[newSize];
      Array.copy(column, newCol, column.length);
      column = newCol;
      colSetter.set(newCol);
    }

    for (int it = flagged.first() ; !flagged.done(it) ; it = flagged.next(it)) {
      int idx = flagged.value(it);
      column[idx] = null;
    }

    int count = updateMap.count();
    for (int i=0 ; i < count ; i++) {
      int index = updateMap.keyAt(i);
      Obj value = updateMap.valueAt(i);
      column[index] = value;
    }
  }

  public void finish() {

  }

  //////////////////////////////////////////////////////////////////////////////

  public void reset() {
    updateMap.clear();
    flagged.clear();
    waitingForDelete = 0;
    maxIndex = -1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean contains1(int index) {
    if (index < column.length && column[index] != null)
      if (updateMap.hasKey(index)) {
        Miscellanea._assert(!flagged.includes(index)); //## KEYS MUST BE CHECKED BEFORE FOREIGN KEYS
        return true;
      }
      else
        return !flagged.includes(index);
    else
      return updateMap.hasKey(index);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean checkKey_1() {
    return waitingForDelete == 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  // bin_rel(a, _) -> unary_rel(a);
  public boolean checkForeignKeys_1(UnaryTableUpdater target) {
    // Checking that every new entry satisfies the foreign key
    int count = updateMap.count();
    for (int i=0 ; i < count ; i++)
      if (!target.contains(updateMap.keyAt(i)))
        return false;

    // Checking that no entries were invalidated by a deletion on the target table
    return target.checkDeletedKeys(this::contains1);
  }

  //////////////////////////////////////////////////////////////////////////////

  public interface ColSetter {
    public void set(Obj[] newCol);
  }
}
