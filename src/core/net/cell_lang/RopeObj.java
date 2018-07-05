package net.cell_lang;


class RopeObj extends NeSeqObj {
  NeSeqObj left;
  NeSeqObj right;

  public static SeqObj create(SeqObj left, SeqObj right) {
    throw Miscellanea.internalFail();
  }
}