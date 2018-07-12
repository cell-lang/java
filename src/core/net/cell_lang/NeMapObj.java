package net.cell_lang;

import java.io.Writer;


final class NeMapObj extends NeBinRelObj {
  public NeMapObj(Obj[] keys, Obj[] values, int[] keysHashes) {
    super(keys, values, keysHashes);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////

  public boolean hasKey(Obj key) {
    return Objs12.lookup(key, col1Hashes, col1) != -1;
  }

  public boolean hasPair(Obj key, Obj value) {
    int idx = Objs12.lookup(key, col1Hashes, col1);
    return idx != -1 && value.isEq(col2[idx]);
  }

  public BinRelIter getBinRelIterByCol1(Obj key) {
    int idx = Objs12.lookup(key, col1Hashes, col1);
    return idx != -1 ? new BinRelIter(col1, col2, idx, idx) : nullIter;
  }

  public Obj lookup(Obj key) {
    int idx = Objs12.lookup(key, col1Hashes, col1);
    if (idx != -1)
      return col2[idx];
    else
      throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }
}
