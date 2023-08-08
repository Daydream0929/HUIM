package src.algorithms.alg2;

import java.util.HashMap;

public class HashMapUtilityList {
    public final Integer item;  // the item
    public long sumIutils = 0;  // the sum of item utilities
    public long sumRutils = 0;  // the sum of remaining utilities

    // <Pid, Partition--Tids>
    public final HashMap<Integer, Long> element1 = new HashMap<>();  // the element1

    // <Tid, {iutil, rutil}>
    public final HashMap<Integer, PairIandR> element2 = new HashMap<>();  // the element2

    // <Pid, {SumIutil, SumRutil}>
    public final HashMap<Integer, PairSIandSR> element3 = new HashMap<>(); // the element3

    /**
     * Constructor.
     * @param item the item that is used for this utility list
     */
    public HashMapUtilityList(Integer item) {
        this.item = item;
    }

    /**
     * Method to add an element to this utility list and update the sums at the same time.
     */
    public void addElement2(int tid, PairIandR element) {
        sumIutils += element.iutils;
        sumRutils += element.rutils;
        element2.put(tid, element);
    }

    public void setElement1(int psize) {
        // element2
        element2.forEach((k, v) -> {
            int i = k / psize;
            int j = k % psize;
            if (element1.containsKey(i)) {
                element1.put(i, element1.get(i) + (long) (Math.pow(2, j)));
                PairSIandSR pair = new PairSIandSR(element3.get(i).SumIutils + v.iutils, element3.get(i).SumRutils + v.rutils);
                element3.put(i, pair);
            } else {
                element1.put(i, (long) (Math.pow(2, j)));
                PairSIandSR pair = new PairSIandSR(v.iutils, v.rutils);
                element3.put(i, pair);
            }
        });
    }
}


