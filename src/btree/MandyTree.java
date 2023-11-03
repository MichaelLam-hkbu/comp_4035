package btree;
import java.util.*;

import Utils.Clock;
import Utils.Utils;
import Utils.Utils.KeyNotFoundException;
import Utils.Utils.TreeIsEmptyException;
import Utils.CLI;
import Utils.readFile;
import Utils.Config;


public class MandyTree implements BTree {
    //Tree specific parameters here
    private double MIN_FILL_FACTOR = 0.5;
    private static int DEGREE = 8;
    private Node root = null;
    private LeafNode firstLeaf;

    //some internal statistics for debugging
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

    //my constructor
    public MandyTree(double MIN_FILL_FACTOR, int DEGREE) {
        root = null;
        firstLeaf = null;
        this.MIN_FILL_FACTOR = MIN_FILL_FACTOR;
        this.DEGREE = DEGREE;
    }

    private boolean isEmpty(){
        return firstLeaf == null;
    }

    private static abstract class Node {
        //fill in your implementation about Node in common here
        int maxDegree = DEGREE;
        int minDegree = (int)Math.ceil(maxDegree/2.0);


    }

    /**
     * This class represents a dictionary pair that is to be contained within the
     * leaf nodes of the B+ tree. The class implements the Comparable interface
     * so that the DictionaryPair objects can be sorted later on.
     */
    public class DictionaryPair implements Comparable<DictionaryPair> {
        int key;
        double value;

        /**
         * Constructor
         * @param key: the key of the key-value pair
         * @param value: the value of the key-value pair
         */
        public DictionaryPair(int key, double value) {
            this.key = key;
            this.value = value;
        }

        /**
         * This is a method that allows comparisons to take place between
         * DictionaryPair objects in order to sort them later on
         * @param o
         * @return
         */
        @Override
        public int compareTo(DictionaryPair o) {
            if (key == o.key) { return 0; }
            else if (key > o.key) { return 1; }
            else { return -1; }
        }
    }

    /**
     * LeafNode class
     */
    private static class LeafNode extends Node {
        //fill in your implementation specific about LeafNode here
        int maxNumPairs;
        int minNumPairs;
        int numPairs;
        LeafNode leftSibling;
        LeafNode rightSibling;
        DictionaryPair[] dictionary;

        /**
         * Given an index, this method sets the dictionary pair at that index
         * within the dictionary to null.
         * @param index: the location within the dictionary to be set to null
         */
        public void delete(int index) {

            // Delete dictionary pair from leaf
            this.dictionary[index] = null;

            // Decrement numPairs
            numPairs--;
        }

        /**
         * This method attempts to insert a dictionary pair within the dictionary
         * of the LeafNode object. If it succeeds, numPairs increments, the
         * dictionary is sorted, and the boolean true is returned. If the method
         * fails, the boolean false is returned.
         * @param dp: the dictionary pair to be inserted
         * @return a boolean indicating whether or not the insert was successful
         */
        public boolean insert(DictionaryPair dp) {
            if (this.isFull()) {

                /* Flow of execution goes here when numPairs == maxNumPairs */

                return false;
            } else {

                // Insert dictionary pair, increment numPairs, sort dictionary
                this.dictionary[numPairs] = dp;
                numPairs++;
                Arrays.sort(this.dictionary, 0, numPairs);

                return true;
            }
        }

        /**
         * This simple method determines if the LeafNode is full, i.e. the
         * numPairs within the LeafNode is equal to the maximum number of pairs.
         * @return a boolean indicating whether or not the LeafNode is full
         */
        public boolean isFull() { return numPairs == maxNumPairs; }

        /**
         * This method is to check the leaf node should split ir not.
         * @return a boolean indicating whether or not the LeafNode object can
         * give a dictionary pair to a deficient leaf node
         */
        public boolean isSplit() { return numPairs > minNumPairs; }

        /**
         * This simple method determines if the LeafNode object is capable of
         * being merged with, which occurs when the number of pairs within the
         * LeafNode object is equal to the minimum number of pairs it can hold.
         * @return a boolean indicating whether or not the LeafNode object can
         * be merged with
         */
        public boolean isMergeable() {
            return numPairs == minNumPairs;
        }

        /**
         * Constructor
         * @param dp: first dictionary pair insert into new node
         */
        public LeafNode(DictionaryPair dp) {
            this.maxNumPairs = DEGREE;
            this.minNumPairs = (int)(Math.ceil(DEGREE/2) - 1);
            this.dictionary = new DictionaryPair[DEGREE];
            this.numPairs = 0;
            this.insert(dp);
        }
    }

    /**
     * IndexNode class
     */
    private static class IndexNode extends Node {
        //fill in your implementation specific about IndexNode here
        IndexNode left;
        IndexNode right;

        int degree;

        Integer[] keys;

        Node[] childPointers;


        /**
         * This method is to append the node pointers when new one data entry
         */
        private void appendChildPointer(Node pointer){
            this.childPointers[degree] = pointer;
            this.degree++;
        }

        /**
         * This method is find correct pointer to index node
         * @return if can find the correct pointer retrun i, else return -1
         */
        private int findIndexOfPointer(Node pointer){
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i] == pointer)
                    return i;
            }
            return -1;
        }


        private void insertChildPointer(Node pointer, int index){

        }

    }
    /**
     * Insert key to tree
     * @param key
     */
    public void insert(Integer key) {
        if (root == null){

        }
    }

    /**
     * Delete a key from the tree starting from root
     * @param key key to be deleted
     */
    public void delete(Integer key) {
    }

    /**
     * Search tree by range
     * @param key1 First key
     * @param key2 Second key
     * @return List of keys
     */
    public List<Integer> search(Integer key1, Integer key2) {
        return (new ArrayList<Integer>());
    }

    
    /**
     * Print statistics of the current tree
     */
    @Override
    public void dumpStatistics() {
        System.out.println("Statistics of the B+ Tree:");
        System.out.println("Total number of nodes: ");
        System.out.println("Total number of data entries: ");
        System.out.println("Total number of index entries: ");
        System.out.print("Average fill factor: ");
        System.out.println("%");
        System.out.println("Height of tree: ");
    }
    /**
     * Print tree from root
     */
    public void printTree() {
    }

    /**
     * print tree from node
     * @param n starting node to print
     */
    public void printTree(Node n) {

    }

    @Override
    public void load(String datafilename) {
        String[] readLines = readFile.readData(datafilename);
        //Fill in you work here

    }
    
    public static void main(String[] args) {
        //we hardcode the fill factor and degree for this project
        BTree mandyTree = new MandyTree(0.5, 4);
        //the value is stored in Config.java
        //build a mandyTree from the data file
        mandyTree.load(Config.dataFileName);

        //interact with the tree via a text interface.
        CLI.shell(mandyTree);

    }
}
