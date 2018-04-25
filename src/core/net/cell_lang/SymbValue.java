package net.cell_lang;


class SymbValue extends ValueBase {
  int id;

  public SymbValue(int id) {
    this.id = id;
  }

  public boolean isSymb() {
    return true;
  }

  public String asSymb() {
    return SymbTable.idxToStr(id);
  }

  public Obj asObj() {
    return SymbObj.get(id);
  }
}
