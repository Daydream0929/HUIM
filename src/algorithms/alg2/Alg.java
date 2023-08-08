package src.algorithms.alg2;

import src.tools.MemoryLogger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

/**
 * This is an implementation of My Algorithm
 *
 * @author zhenjun feng
 */
public class Alg {

    /**
     * buffer for storing the current itemset that is mined when performing mining
     * the idea is to always reuse the same buffer to reduce memory usage.
     */
    final int BUFFERS_SIZE = 200;
    /**
     * The Cycle TWU-prune strategy
     */
    final boolean ENABLE_CTWU_PRUNE = true;
    /**
     * enable LA-prune strategy
     */
    final boolean ENABLE_LA_PRUNE = true;
    /**
     * variable for debug mode
     */
    final boolean DEBUG = false;
    /**
     * the time at which the algorithm started
     */
    public long startTimestamp = 0;
    /**
     * the time at which the algorithm ended
     */
    public long endTimestamp = 0;
    /**
     * the number of high-utility itemsets generated
     */
    public int huiCount = 0;
    /**
     * the number of candidate high-utility itemsets
     */
    public int candidateCount = 0;
    /**
     * the posID of transactions
     */
    public int transactionId = 0;
    /**
     * the number of items in the database
     */
    public int maxItem = 0;

    public int minUtility = 0;

    public int pSize = 0;

    /**
     * Map to remember the TWU of each item
     */
    Map<Integer, Long> mapItemToTWU;
    /**
     * Use List<Map> to save the databse
     */
    List<Map<Integer, Integer>> listOfMapTransactionToIAndU;
    /**
     * ReConstruct the database for CTWU-prune
     */
    List<Map<Integer, Integer>> listOfMapItemToTAndU;
    /**
     * writer to write the output file
     */
    BufferedWriter writer = null;
    /**
     * The eucs structure:  key: item   key: another item   value: twu
     */
    Map<Integer, Map<Integer, Long>> mapFMAP;
    /**
     * The Merge Transaction strategy
     */
    boolean ENABLE_Tran_Merge = true;
    private int[] itemsetBuffer = null;

    /**
     * Default constructor
     */
    public Alg() {

    }

    static <K, V extends Comparable<? super V>>
    void entriesSortedByValues(Map<K, V> map) {

        List<Entry<K, V>> sortedEntries = new ArrayList<>(map.entrySet());

        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
    }

    /**
     * Run the algorithm
     *
     * @param input      the input file path
     * @param output     the output file path
     * @param minUtility the minimum utility threshold
     * @param pSize      the size of the patition，1 - 63
     * @throws IOException exception if error while writing the file
     */
    public void runAlgorithm(String input, String output, int pSize, int minUtility) throws IOException {

        // reset maximum
        MemoryLogger.getInstance().reset();

        // start the algorithm
        startTimestamp = System.currentTimeMillis();

        this.minUtility = minUtility;
        this.pSize = pSize;

        // initialize the buffer for storing the current itemset
        itemsetBuffer = new int[BUFFERS_SIZE];

        mapFMAP = new HashMap<>();

        writer = new BufferedWriter(new FileWriter(output));

        //  We create a  map to store the TWU of each item
        mapItemToTWU = new HashMap<>();

        // We scan the database a first time to calculate the TWU of each item.
        BufferedReader myInput = null;
        String thisLine;
        try {
            // prepare the object for reading the file
            myInput = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(input).toPath())));
            // for each line (transaction) until the end of file
            while ((thisLine = myInput.readLine()) != null) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (thisLine.isEmpty() ||
                        thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }
                // transactionID + 1
                transactionId++;
                // split the transaction according to the : separator
                String[] split = thisLine.split(":");
                // the first part is the list of items
                String[] items = split[0].split(" ");
                // the second part is the transaction utility
                int transactionUtility = Integer.parseInt(split[1]);

                // for each item, we add the transaction utility to its TWU
                for (String s : items) {
                    // convert item to integer
                    Integer item = Integer.parseInt(s);
                    // update the maxItem
                    maxItem = Math.max(maxItem, item);
                    // get the current TWU of that item
                    Long twu = mapItemToTWU.get(item);
                    // add the utility of the item in the current transaction to its twu
                    twu = (twu == null) ?
                            transactionUtility : twu + transactionUtility;
                    mapItemToTWU.put(item, twu);
                }
            }
        } catch (Exception e) {
            // catches exception if error while reading the input file
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }

        // sort the mapItemToTWU by descending order of TWU
        entriesSortedByValues(mapItemToTWU);

        // 使用我的HashMapUtilityList
        List<HashMapUtilityList> listOfHashMapUtilityLists = new ArrayList<>();
        Map<Integer, HashMapUtilityList> mapItemToHashMapUtilityList = new HashMap<>();
        // for each item
        for (Integer item : mapItemToTWU.keySet()) {
            if (mapItemToTWU.get(item) >= minUtility) {
                HashMapUtilityList hashMapUtilityListList = new HashMapUtilityList(item);
                mapItemToHashMapUtilityList.put(item, hashMapUtilityListList);
                listOfHashMapUtilityLists.add(hashMapUtilityListList);
            }
        }
        // sort the list of high TWU items in ascending order
        listOfHashMapUtilityLists.sort(((o1, o2) -> compareItems(o1.item, o2.item)));

        // second database pass to construct the HashMapUtilityList
        try {
            // prepare object for reading the file
            myInput = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(input).toPath())));
            // variable to count the number of transaction
            int tid = 0;
            // for each line (transaction) until the end of file
            while ((thisLine = myInput.readLine()) != null) {
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (thisLine.isEmpty() ||
                        thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
                        || thisLine.charAt(0) == '@') {
                    continue;
                }

                // split the line according to the separator
                String[] split = thisLine.split(":");
                // get the list of items
                String[] items = split[0].split(" ");
                // get the list of utility values corresponding to each item
                // for that transaction
                String[] utilityValues = split[2].split(" ");

                // Copy the transaction into lists but
                // without items with TWU < minutility

                int remainingUtility = 0;

                long newTWU = 0;  // NEW OPTIMIZATION

                // Create a list to store items
                List<Pair> revisedTransaction = new ArrayList<>();
                // for each item
                for (int i = 0; i < items.length; i++) {
                    /// convert values to integers
                    Pair pair = new Pair();
                    pair.item = Integer.parseInt(items[i]);
                    pair.utility = Integer.parseInt(utilityValues[i]);
                    // if the item has enough utility
                    if (mapItemToTWU.containsKey(pair.item) && mapItemToTWU.get(pair.item) >= minUtility) {
                        // add it
                        revisedTransaction.add(pair);
                        remainingUtility += pair.utility;
                        newTWU += pair.utility; // NEW OPTIMIZATION
                    }
                }

                // sort the transaction
                revisedTransaction.sort((o1, o2) -> compareItems(o1.item, o2.item));

                // for each item left in the transaction
                for (int i = 0; i < revisedTransaction.size(); i++) {
                    Pair pair = revisedTransaction.get(i);

//					int remain = remainingUtility; // FOR OPTIMIZATION

                    // subtract the utility of this item from the remaining utility
                    remainingUtility = remainingUtility - pair.utility;

                    HashMapUtilityList hashMapUtilityListOfItem = mapItemToHashMapUtilityList.get(pair.item);
                    // get the utility list of this item
                    // UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
                    PairIandR pairIandR = new PairIandR(pair.utility, remainingUtility);
                    hashMapUtilityListOfItem.addElement2(tid, pairIandR);

                    // BEGIN NEW OPTIMIZATION for FHM
                    Map<Integer, Long> mapFMAPItem = mapFMAP.computeIfAbsent(pair.item, k -> new HashMap<>());

                    for (int j = i + 1; j < revisedTransaction.size(); j++) {
                        Pair pairAfter = revisedTransaction.get(j);
                        Long twuSum = mapFMAPItem.get(pairAfter.item);
                        if (twuSum == null) {
                            mapFMAPItem.put(pairAfter.item, newTWU);
                        } else {
                            mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
                        }
                    }
                    // END OPTIMIZATION of FHM
                }
                tid++; // increase tid number for next transaction
            }

            // init hashMapUtilityLists
            listOfHashMapUtilityLists.forEach((k) -> k.setElement1(pSize));

        } catch (Exception e) {
            // to catch error while reading the input file
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }

        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        search(itemsetBuffer, 0, null, listOfHashMapUtilityLists);

        // check the memory usage again and close the file.
        MemoryLogger.getInstance().checkMemory();
        // close output file
        writer.close();
        // record end time
        endTimestamp = System.currentTimeMillis();
    }

    /**
     * Method to compare items by their TWU
     *
     * @param item1 an item
     * @param item2 another item
     * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
     */
    private int compareItems(int item1, int item2) {
        int compare = (int) (mapItemToTWU.get(item1) - mapItemToTWU.get(item2));
        // if the same, use the lexical order otherwise use the TWU
        return (compare == 0) ? item1 - item2 : compare;
    }

    private void search(int[] prefix,
                        int prefixLength, HashMapUtilityList pUL, List<HashMapUtilityList> ULs)
            throws IOException {

        // for each extension X of prefix P
        for (int i = 0; i < ULs.size(); i++) {
            HashMapUtilityList X = ULs.get(i);
            if (X == null) {
                continue;
            }
            // writeOut(prefix, prefixLength, X.item, X.sumIutils);
            // If pX is a high utility itemset.
            // we save the itemset: prefix + X
            if (X.sumIutils >= minUtility) {
                writeOut(prefix, prefixLength, X.item, X.sumIutils);
            }

            // If the sum of the remaining utility of pX is greater than minUtility
            // we explore the extension of pX
            if (X.sumIutils + X.sumRutils >= minUtility) {
                List<HashMapUtilityList> exULs = new ArrayList<>();
                // For each extension of p appearing after X
                for (int j = i + 1; j < ULs.size(); j++) {
                    HashMapUtilityList Y = ULs.get(j);

                    if (Y == null) {
                        continue;
                    }

                    // ======================== NEW OPTIMIZATION USED IN FHM
                    if (mapFMAP != null && !mapFMAP.isEmpty() && mapFMAP.containsKey(X.item)) {
                        Map<Integer, Long> mapTWUF = mapFMAP.computeIfAbsent(X.item, k -> new HashMap<>());
                        Long twuF = 0L;
                        if (mapTWUF.containsKey(Y.item)) {
                            twuF = mapTWUF.get(Y.item);
                        }
                        if (twuF == null || twuF < minUtility) {
                            continue;
                        }
                    }
                    candidateCount++;
                    // =========================== END OF NEW OPTIMIZATION

                    //we construct the HashMapUtilityList of the extension
                    HashMapUtilityList temp = construct(pUL, X, Y, minUtility, pSize);
                    // temp.setElement1(pSize);
                    exULs.add(temp);
                }
                // we create new prefix pX
                itemsetBuffer[prefixLength] = X.item;
                // We make a recursive call to discover all itemsets with the prefix pXY
                search(itemsetBuffer, prefixLength + 1, X, exULs);
            }
        }
    }
    private HashMapUtilityList construct(HashMapUtilityList P, HashMapUtilityList px, HashMapUtilityList py, int minUtility, int pSize) {
        HashMapUtilityList pxyUL = new HashMapUtilityList(py.item);
        long totalUtility = px.sumIutils + px.sumRutils;

        // for each partition of x
        for (Entry<Integer, Long> entry : px.element1.entrySet()) {
            int partition = entry.getKey();
            long X = entry.getValue();
            if (py.element1.containsKey(partition)) {
                long Y = py.element1.get(partition);
                long XandY = X & Y;

                if (XandY == 0 && ENABLE_LA_PRUNE) {
                    totalUtility -= px.element3.get(partition).SumIutils + px.element3.get(partition).SumRutils;
                    if (totalUtility < minUtility) {
                        return null;
                    }
                }

                while (XandY > 0) {
                    int bitIndex = Long.numberOfTrailingZeros(XandY);
                    XandY &= (XandY - 1); // Clear the lowest set bit
                    int tid = partition * pSize + bitIndex;
                    PairIandR ex = px.element2.get(tid);
                    PairIandR ey = py.element2.get(tid);
                    if (ex != null && ey != null) {
                        PairIandR ep = (P != null) ? P.element2.get(tid) : null;
                        if (P == null || ep != null) {
                            pxyUL.addElement2(tid, new PairIandR(ex.iutils + ey.iutils - ((ep != null) ? ep.iutils : 0), ey.rutils));
                        }
                    }
                }
            } else if (ENABLE_LA_PRUNE) {
                totalUtility -= px.element3.get(partition).SumIutils + px.element3.get(partition).SumRutils;
                if (totalUtility < minUtility) {
                    return null;
                }
            }
        }
        pxyUL.setElement1(pSize);
        return pxyUL;
    }

    /**
     * Method to write a high utility itemset to the output file.
     *
     * @param prefix       to be writent o the output file
     * @param item         to be appended to the prefix
     * @param utility      the utility of the prefix concatenated with the item
     * @param prefixLength the prefix length
     */
    private void writeOut(int[] prefix, int prefixLength, int item, long utility) throws IOException {
        huiCount++; // Increase the number of high utility itemsets found

        // Calculate the maximum expected length of the output string
        int maxOutputLength = (prefixLength + 1) * 2 + 10 + String.valueOf(utility).length();
        StringBuilder buffer = new StringBuilder(maxOutputLength);

        // Append the prefix
        for (int i = 0; i < prefixLength; i++) {
            buffer.append(prefix[i]).append(' ');
        }

        // Append the last item and the utility value
        buffer.append(item).append(" #UTIL: ").append(utility);

        // Write to file
        writer.write(buffer.toString());
        writer.newLine();
    }


    /**
     * Print statistics about the latest execution to System.out.
     */
    public void printStats() throws IOException {
        System.out.println("=============  My ALGORITHM - SPMF 0.97e - STATS =============");
        System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" High-utility itemsets count : " + huiCount);
        System.out.println(" Candidate count : " + candidateCount);
        System.out.println("===================================================");
    }


    /**
     * This class is used for judge that need or not to CycleTWUPrune.
     *
     * @param mapTWU     the map of TWU
     * @param minUtility the minimal Utility
     * @return true or false
     */
    private boolean needCycleTWUPrune(Map<Integer, Long> mapTWU, long minUtility) {
        for (long twu : mapTWU.values()) {
            if (twu < minUtility) {
                return true;
            }
        }
        return false;
    }

    /**
     * this class represent an item and its utility in a transaction
     */
    static class Pair {
        int item = 0;
        int utility = 0;
    }
}

