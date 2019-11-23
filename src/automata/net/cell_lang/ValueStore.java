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

  public void release(int index, int amount) {
    int refs = Byte.toUnsignedInt(references[index]);
    Miscellanea._assert(refs > 0);

    if (refs < 128) {
      refs -= amount;
    }
    else {
      refs -= amount;
      while (refs < 128 && extraRefs.tryDecrement(index))
        refs += 64;
    }

    Miscellanea._assert(refs >= 0 & refs <= 255);

    references[index] = (byte) refs;

    if (refs == 0)
      free(index);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean tryRelease(int index) {
    return tryRelease(index, 1);
  }

  public boolean tryRelease(int index, int amount) {
    int refs = Byte.toUnsignedInt(references[index]);
    Miscellanea._assert(refs >= 0);

    if (refs < 128) {
      if (amount >= refs)
        return false;
      refs -= amount;
      references[index] = (byte) refs;
      return true;
    }
    else if (refs + 64 * extraRefs.get(index) > amount) {
      release(index, amount);
      return true;
    }
    else
      return false;
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
