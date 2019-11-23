package net.cell_lang;


abstract class ValueStoreUpdater {
  ValueStore store;

  int deferredCount = 0;
  int[] deferredReleases = Array.emptyIntArray;

  int batchDeferredCount = 0;
  long[] batchDeferredReleases = Array.emptyLongArray;

  //////////////////////////////////////////////////////////////////////////////

  private static long entry(int index, int count) {
    return Miscellanea.pack(index, count);
  }

  private static int index(long entry) {
    return Miscellanea.low(entry);
  }

  private static int count(long entry) {
    return Miscellanea.high(entry);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected ValueStoreUpdater(ValueStore store) {
    this.store = store;
  }

  public final void addRef(int index) {
    store.addRef(index);
  }

  public final void release(int index) {
    store.release(index);
  }

  public final void release(int index, int count) {
    store.release(index, count);
  }

  public final void markForDelayedRelease(int index) {
    if (!store.tryRelease(index))
      deferredReleases = Array.append(deferredReleases, deferredCount++, index);
  }

  public final void markForDelayedRelease(int index, int count) {
    if (!store.tryRelease(index, count))
      batchDeferredReleases = Array.append(batchDeferredReleases, batchDeferredCount++, entry(index, count));
  }

  public final void applyDelayedReleases() {
    if (deferredCount > 0) {
      for (int i=0 ; i < deferredCount ; i++)
        release(deferredReleases[i]);
      deferredCount = 0;
      if (deferredReleases.length > 1024)
        deferredReleases = Array.emptyIntArray;
    }

    if (batchDeferredCount > 0) {
      for (int i=0 ; i < batchDeferredCount ; i++) {
        long entry = batchDeferredReleases[i];
        release(index(entry), count(entry));
      }
      batchDeferredCount = 0;
      if (batchDeferredReleases.length > 1024)
        batchDeferredReleases = Array.emptyLongArray;
    }
  }

  public abstract Obj surrToValue(int surr);
}
