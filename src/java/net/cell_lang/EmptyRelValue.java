package net.cell_lang;


class EmptyRelValue extends ValueBase {
  public boolean IsSet() {
    return true;
  }

  public boolean IsBinRel() {
    return true;
  }

  public boolean IsTernRel() {
    return true;
  }

  public int Size() {
    return 0;
  }

  public Value Item(int index) {
    throw new IndexOutOfBoundsException();
  }

  public Value Arg1(int index) {
    throw new IndexOutOfBoundsException();
  }

  public Value Arg2(int index) {
    throw new IndexOutOfBoundsException();
  }

  public boolean IsRecord() {
    return true;
  }

  public Obj AsObj() {
    return EmptyRelObj.Singleton();
  }
}
