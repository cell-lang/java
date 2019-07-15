package net.cell_lang;


class EmptyRelValue extends ValueBase {
  public boolean isSet() {
    return true;
  }

  public boolean isBinRel() {
    return true;
  }

  public boolean isTernRel() {
    return true;
  }

  public int size() {
    return 0;
  }

  public Value item(int index) {
    throw new IndexOutOfBoundsException();
  }

  public Value arg1(int index) {
    throw new IndexOutOfBoundsException();
  }

  public Value arg2(int index) {
    throw new IndexOutOfBoundsException();
  }

  public boolean isRecord() {
    return true;
  }

  public Obj asObj() {
    return EmptyRelObj.singleton;
  }
}
