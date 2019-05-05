package net.cell_lang;

import java.util.Random;
import java.io.Writer;


class NeTreeSetObj extends Obj {
  Node rootNode;
  NeSetObj packed;

  //////////////////////////////////////////////////////////////////////////////

  private NeTreeSetObj(Node rootNode) {
    data = binRelObjData(rootNode.size());
    extraData = neSetObjExtraData();
    this.rootNode = rootNode;
  }

  public NeTreeSetObj(Obj elt) {
    data = setObjData(1);
    extraData = neSetObjExtraData();
    rootNode = new StdNode(elt, elt.hashcode());
  }

  public NeTreeSetObj(Obj[] elts, int[] hashcodes, int first, int count) {
    data = setObjData(count);
    extraData = neSetObjExtraData();
    rootNode = newNode(elts, hashcodes, first, count);
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj insert(Obj obj) {
    if (rootNode != null) {
      Node newRoot = rootNode.insert(obj, obj.hashcode());
      return newRoot != rootNode ? new NeTreeSetObj(newRoot) : this;
    }
    else
      return packed.insert(obj);
  }

  public Obj remove(Obj obj) {
    if (rootNode != null) {
      Node newRoot = rootNode.remove(obj, obj.hashcode());
      if (newRoot == null)
        return EmptyRelObj.singleton;
      else if (newRoot == rootNode)
        return this;
      else
        return new NeTreeSetObj(newRoot);
    }
    else
      return packed.remove(obj);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasElem(Obj obj) {
    return rootNode != null ? rootNode.hasElem(obj, obj.hashcode()) : packed.hasElem(obj);
  }

  public SetIter getSetIter() {
    return packed().getSetIter();
  }

  public Obj[] getArray(Obj[] buffer) {
    return packed().getArray(buffer);
  }

  public SeqObj internalSort() {
    return packed().internalSort();
  }

  public Obj randElem() {
    return rootNode != null ? rootNode.leftMostElt() : packed.randElem();
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    return packed().internalOrder(other);
  }

  public TypeCode getTypeCode() {
    return TypeCode.NE_SET;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    packed().print(writer, maxLineLen, newLine, indentLevel);
  }

  public int minPrintedSize() {
    return packed().minPrintedSize();
  }

  public ValueBase getValue() {
    return packed().getValue();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private NeSetObj packed() {
    if (rootNode != null) {
      Miscellanea._assert(packed == null);
      int size = getSize();
      Obj[] elts = new Obj[size];
      int count = rootNode.traverse(elts, 0);
      Miscellanea._assert(count == size);
      // packed = new NeSetObj(elts);
      packed = (NeSetObj) Builder.createSet(elts); //## BAD BAD BAD
      rootNode = null;
    }
    return packed;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private interface Node {
    int size();
    boolean hasElem(Obj obj, int hashcode);

    StdNode insert(Obj obj, int hashcode);
    Node remove(Obj obj, int hashcode);
    Obj leftMostElt();

    Node merge(Node right);
    int traverse(Obj[] elts, int offset);
    String[] toStrings();
  }


  private static Node newNode(Obj[] elts, int[] hashcodes, int first, int count) {
    Miscellanea._assert(count > 0);
    if (count > 1)
      return new ArraysNode(elts, hashcodes, first, count);
    else
      return new StdNode(elts[first], hashcodes[first]);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private static final class StdNode implements Node {
    Obj elt;
    Node left, right;
    int size;
    int hashcode;


    private StdNode(Obj elt, int hashcode, Node left, Node right) {
      this.elt = elt;
      this.hashcode = hashcode;
      this.size = 1 + (left != null ? left.size() : 0) + (right != null ? right.size() : 0);
      this.left = left;
      this.right = right;

      if (left != null) {
        if (left instanceof StdNode) {
          StdNode node = (StdNode) left;
          if (!(node.hashcode < hashcode || (node.hashcode == hashcode && node.elt.quickOrder(elt) < 0))) {
            throw new RuntimeException();
          }
        }
        else {
          ArraysNode node = (ArraysNode) left;
          int last = node.first + node.count - 1;
          if (!(node.hashcodes[last] < hashcode ||
               (node.hashcodes[last] == hashcode && node.elts[last].quickOrder(elt) < 0))) {
            throw new RuntimeException();
          }
        }
      }

      if (right != null) {
        if (right instanceof StdNode) {
          StdNode node = (StdNode) right;
          if (!(hashcode < node.hashcode || (node.hashcode == hashcode && elt.quickOrder(node.elt) < 0))) {
            throw new RuntimeException();
          }
        }
        else {
          ArraysNode node = (ArraysNode) right;
          int first = node.first;
          if (!(hashcode < node.hashcodes[first] ||
               (node.hashcodes[first] == hashcode && elt.quickOrder(node.elts[first]) < 0))) {
            throw new RuntimeException();
          }
        }
      }
    }

    private StdNode(Obj elt, int hashcode) {
      this.elt = elt;
      this.size = 1;
      this.hashcode = hashcode;
    }

    ////////////////////////////////////////////////////////////////////////////

    public int size() {
      return size;
    }

    public boolean hasElem(Obj obj, int objHash) {
      int ord = order(obj, objHash);

      if (ord > 0) // search elt < node elt, searching the left node
        return left != null && left.hasElem(obj, objHash);
      else if (ord < 0) // node elt < search elt, searching the right node
        return right != null && right.hasElem(obj, objHash);
      else
        return true;
    }

    public Obj leftMostElt() {
      return left != null ? left.leftMostElt() : elt;
    }

    ////////////////////////////////////////////////////////////////////////////

    public StdNode insert(Obj obj, int objHash) {
      int ord = order(obj, objHash);

      if (ord > 0) {
        Node node = left != null ? left.insert(obj, objHash) : new StdNode(obj, objHash);
        return new StdNode(elt, hashcode, node, right);
      }
      else if (ord < 0) {
        Node node = right != null ? right.insert(obj, objHash) : new StdNode(obj, objHash);
        return new StdNode(elt, hashcode, left, node);
      }
      else {
        return new StdNode(elt, hashcode, left, right);
      }
    }

    public Node remove(Obj obj, int objHash) {
      int ord = order(obj, objHash);

      if (ord > 0) {
        // search elt < node elt
        Node node = left != null ? left.remove(obj, objHash) : null;
        return node != left ? new StdNode(elt, hashcode, node, right) : this;
      }
      else if (ord < 0) {
        // node elt < search elt
        Node node = right != null ? right.remove(obj, objHash) : null;
        return node != right ? new StdNode(elt, hashcode, left, node) : this;
      }
      else {
        if (left == null)
          return right;
        else if (right == null)
          return left;
        else
          return left.merge(right);
      }
    }

    ////////////////////////////////////////////////////////////////////////////

    private int order(Obj obj, int objHash) {
      return objHash < hashcode ? 1 : (objHash > hashcode ? -1 : elt.quickOrder(obj));
    }

    public Node merge(Node other) {
      return new StdNode(elt, hashcode, left, right != null ? right.merge(other) : other);
    }

    ////////////////////////////////////////////////////////////////////////////

    public int traverse(Obj[] elts, int offset) {
      if (left != null)
        offset = left.traverse(elts, offset);
      elts[offset] = elt;
      if (right != null)
        offset = right.traverse(elts, offset);
      return offset;
    }

    ////////////////////////////////////////////////////////////////////////////

    public String toString() {
      String[] strs = toStrings();
      String str = "";
      for (int i=0 ; i < strs.length ; i++) {
        if (i > 0)
          str += "\n";
        str += strs[i];
      }
      return str;
    }

    public String[] toStrings() {
      String[] leftStrs = left != null ? left.toStrings() : new String[] {"null"};
      String[] rightStrs = right != null ? right.toStrings() : new String[] {"null"};
      int count = 1 + leftStrs.length + rightStrs.length;
      String[] strs = new String[count];
      strs[0] = String.format("StdNode: elt = %s, hashcode = %d", elt, hashcode);
      for (int i=0 ; i < leftStrs.length ; i++)
        strs[i+1] = "  " + leftStrs[i];
      for (int i=0 ; i < rightStrs.length ; i++)
        strs[i+1+leftStrs.length] = "  " + rightStrs[i];
      return strs;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private static final class ArraysNode implements Node {
    Obj[] elts;
    int[] hashcodes;
    int first;
    int count;


    ArraysNode(Obj[] elts, int[] hashcodes, int first, int count) {
      Miscellanea._assert(count >= 2);
      this.elts = elts;
      this.hashcodes = hashcodes;
      this.first = first;
      this.count = count;
    }

    ////////////////////////////////////////////////////////////////////////////

    public int size() {
      return count;
    }

    public boolean hasElem(Obj obj, int hashcode) {
      return eltIdx(obj, hashcode) >= 0;
    }

    public Obj leftMostElt() {
      return elts[first];
    }

    ////////////////////////////////////////////////////////////////////////////

    public int traverse(Obj[] elts, int offset) {
      for (int i=0 ; i < count ; i++)
        elts[offset] = this.elts[first+i];
      return offset;
    }

    ////////////////////////////////////////////////////////////////////////////

    public StdNode insert(Obj elt, int hashcode) {
      Node right = null, left = null;

      int lastIdx = first + count - 1;
      int idx = eltIdx(elt, hashcode);

      if (idx >= 0) {
        // <elt> was found
        int leftCount = idx - first;
        if (leftCount > 1)
          left = newNode(elts, hashcodes, first, leftCount);
        else if (leftCount == 1)
          left = new StdNode(elts[first], hashcodes[first]);

        int rightCount = lastIdx - idx;
        if (rightCount > 1)
          right = newNode(elts, hashcodes, idx + 1, rightCount);
        else if (rightCount == 1)
          right = new StdNode(elts[lastIdx], hashcodes[lastIdx]);
      }
      else {
        // <elt> was not found
        int insIdx = -idx - 1;

        int leftCount = insIdx - first;
        if (leftCount > 1)
          left = newNode(elts, hashcodes, first, leftCount);
        else if (leftCount == 1)
          left = new StdNode(elts[first], hashcodes[first]);

        int rightCount = lastIdx - insIdx + 1;
        if (rightCount > 1)
          right = newNode(elts, hashcodes, insIdx, rightCount);
        else if (rightCount == 1)
          right = new StdNode(elts[insIdx], hashcodes[insIdx]);
      }

      return new StdNode(elt, hashcode, left, right);
    }

    ////////////////////////////////////////////////////////////////////////////

    public Node remove(Obj elt, int hashcode) {
      int idx = eltIdx(elt, hashcode);
      if (idx < 0)
        return this;

      int lastIdx = first + count - 1;

      int countl = idx - first;
      int countr = lastIdx - idx;

      if (countl > 1) {
        if (countr > 1) {
          if (countl > countr) { // countl > countr >= 2  =>  countl >= 3
            Node left = newNode(elts, hashcodes, first, countl-1);
            Node right = newNode(elts, hashcodes, idx+1, countr);
            return new StdNode(elts[idx-1], hashcodes[idx-1], left, right);
          }
          else { // countr >= countl >= 2
            Node left = newNode(elts, hashcodes, first, countl);
            Node right = newNode(elts, hashcodes, idx+2, countr-1);
            return new StdNode(elts[idx+1], hashcodes[idx+1], left, right);
          }
        }
        else if (countr == 1) {
          Node left = newNode(elts, hashcodes, first, countl);
          return new StdNode(elts[lastIdx], hashcodes[lastIdx], left, null);
        }
        else {
          return newNode(elts, hashcodes, first, countl);
        }
      }
      else if (countl == 1) {
        if (countr > 1) {
          Node right = newNode(elts, hashcodes, idx+1, countr);
          return new StdNode(elts[first], hashcodes[first], null, right);
        }
        else if (countr == 1) {
          Obj[] remElts = new Obj[2];
          int[] remHashcodes = new int[2];
          remElts[0] = elts[first];
          remElts[1] = elts[lastIdx];
          remHashcodes[0] = hashcodes[first];
          remHashcodes[1] = hashcodes[lastIdx];
          return newNode(remElts, remHashcodes, 0, 2);
        }
        else {
          return new StdNode(elts[first], hashcodes[first]);
        }
      }
      else {
        if (countr > 1) {
          return newNode(elts, hashcodes, idx+1, countr);
        }
        else if (countr == 1) {
          return new StdNode(elts[lastIdx], hashcodes[lastIdx]);
        }
        else {
          return null;
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////

    public Node merge(Node aNode) {
      if (aNode instanceof StdNode) {
        StdNode node = (StdNode) aNode;
        Node newLeft = node.left != null ? merge(node.left) : this;
        return new StdNode(node.elt, node.hashcode, newLeft, node.right);
      }
      else {
        ArraysNode node = (ArraysNode) aNode;
        if (count > node.count) { // count > node.count >= 2  =>  count >= 3
          Node left = newNode(elts, hashcodes, first, count-1);
          int idx = first + count - 1;
          return new StdNode(elts[idx], hashcodes[idx], left, node);

        }
        else { // node.count >= count >= 2  =>  count >= 2
          Node right = newNode(node.elts, node.hashcodes, node.first+1, node.count-1);
          return new StdNode(node.elts[node.first], node.hashcodes[node.first], this, right);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////

    public String toString() {
      String[] strs = toStrings();
      String str = "";
      for (int i=0 ; i < strs.length ; i++) {
        if (i > 0)
          str += "\n";
        str += strs[i];
      }
      return str;
    }

    public String[] toStrings() {
      String[] strs = new String[count+1];
      strs[0] = "ArraysNode";
      for (int i=0 ; i < count ; i++)
        strs[i+1] = String.format("%13d:  %-40s", hashcodes[first+i], elts[first+i]);
      return strs;
    }

    ////////////////////////////////////////////////////////////////////////

    private boolean lowerThan(Obj elt, int hashcode, int idx) {
      return hashcode < hashcodes[idx] || (hashcode == hashcodes[idx] && elt.quickOrder(elts[idx]) < 0);
    }

    private boolean greaterThan(Obj elt, int hashcode, int idx) {
      return hashcode > hashcodes[idx] || (hashcode == hashcodes[idx] && elt.quickOrder(elts[idx]) > 0);
    }

    private int eltIdx(Obj elt, int hashcode) {
      int res = _eltIdx(elt, hashcode);
      int end = first + count;
      int last = end - 1;
      if (res >= 0) {
        Miscellanea._assert(res >= first & res <= last);
        Miscellanea._assert(elt.isEq(elts[res]));
        Miscellanea._assert(res == first || greaterThan(elt, hashcode, res-1)); // elts[res-1] < elt
        Miscellanea._assert(res == last  || lowerThan(elt, hashcode, res+1));   // elt < elts[res+1]
      }
      else {
        int insIdx = -res - 1;
        if (!(insIdx >= first & insIdx <= end)) {
          res = _eltIdx(elt, hashcode);
          System.out.printf("res = %d\n", res);
          System.out.printf("first = %d, count = %d, end = %d, insIdx = %d\n", first, count, end, insIdx);
          System.out.println(toString());
        }
        Miscellanea._assert(insIdx >= first & insIdx <= end);
        Miscellanea._assert(insIdx == first || greaterThan(elt, hashcode, insIdx-1)); // elts[insIdx-1] < elt
        Miscellanea._assert(insIdx == end   || lowerThan(elt, hashcode, insIdx));     // elt < elts[insIdx]
      }
      return res;
    }

    private int _eltIdx(Obj elt, int hashcode) {
      int end = first + count;
      int idx = Miscellanea.anyIndexOrEncodeInsertionPointIntoSortedArray(hashcodes, first, end, hashcode);
      if (idx < 0)
        return idx;

      int ord = elt.quickOrder(elts[idx]);
      if (ord == 0)
        return idx;

      while (idx > first && hashcodes[idx-1] == hashcode)
        idx--;

      while (idx < end && hashcodes[idx] == hashcode) {
        ord = elt.quickOrder(elts[idx]);
        if (ord == 0)
          return idx;
        else if (ord > 0)
          idx++;
        else
          break;
      }

      return -idx - 1;
    }
  }
}
