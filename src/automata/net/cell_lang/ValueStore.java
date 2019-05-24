package net.cell_lang;


abstract class ValueStore {
  private byte[] references;
  private IntCtrs extraRefs;

  //////////////////////////////////////////////////////////////////////////////

  public ValueStore(int capacity) {
    references = new byte[capacity];
    extraRefs  = new IntCtrs();
  }

  //////////////////////////////////////////////////////////////////////////////

  public abstract Obj surrToObjValue(int surr);
  protected abstract void free(int index);

  //////////////////////////////////////////////////////////////////////////////

  public void addRef(int index) {
    int refs = Byte.toUnsignedInt(references[index]) + 1;
    if (refs == 256) {
      extraRefs.increment(index);
      refs -= 64;
    }
    references[index] = (byte) refs;
  }

  public void release(int index) {
    int refs = Byte.toUnsignedInt(references[index]) - 1;
    Miscellanea._assert(refs >= 0);
    if (refs == 127) {
      if (extraRefs.tryDecrement(index))
        refs += 64;
    }
    else if (refs == 0) {
      free(index);
    }
    references[index] = (byte) refs;
  }

  //////////////////////////////////////////////////////////////////////////////

  protected int refCount(int index) {
    return Byte.toUnsignedInt(references[index]) + 64 * extraRefs.get(index);
  }

  protected void resizeRefsArray(int newCapacity) {
    byte[] currReferences = references;
    references = new byte[newCapacity];
    Array.copy(currReferences, references, currReferences.length);
  }
}
