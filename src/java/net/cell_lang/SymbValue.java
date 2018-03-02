package net.cell_lang;


class SymbValue extends ValueBase {
  int id;

  public SymbValue(int id) {
    this.id = id;
  }

  public boolean IsSymb() {
    return true;
  }

  public String AsSymb() {
    return SymbTable.IdxToStr(id);
  }

  public Obj AsObj() {
    return SymbObj.Get(id);
  }
}
