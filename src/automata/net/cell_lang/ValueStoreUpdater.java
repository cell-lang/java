package net.cell_lang;


abstract class ValueStoreUpdater {
  ValueStore store;

  protected ValueStoreUpdater(ValueStore store) {
    this.store = store;
  }

  public void addRef(int index) {
    store.addRef(index);
  }

  public void release(int index) {
    store.release(index);
  }

  public abstract Obj surrToValue(int surr);
}
