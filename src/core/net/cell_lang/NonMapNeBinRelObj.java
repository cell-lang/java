package net.cell_lang;

import java.io.Writer;


final class NonMapNeBinRelObj extends NeBinRelObj {
  public NonMapNeBinRelObj(Obj[] col1, Obj[] col2, int[] col1Hashes) {
    super(col1, col2, col1Hashes);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap() {
    return false;
  }

  public boolean isNeRecord() {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  // public boolean hasKey(Obj obj) {
  //
  // }

  public boolean hasPair(Obj obj1, Obj obj2) {
    return Objs12.contains(obj1, obj2, col1Hashes, col1, col2);
  }

  public BinRelIter getBinRelIterByCol1(Obj obj) {
    int first = Objs12.lookupFirst(obj, col1Hashes, col1);
    if (first == -1)
      return nullIter;
    int count = 1 + Objs12.countEqUpward(first + 1, obj, col1Hashes, col1);
    return new BinRelIter(col1, col2, first, first+count-1);
  }

  // public Obj lookup(Obj key) {
  //
  // }
}
