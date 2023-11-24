package btree;
import java.util.*;
import java.util.stream.Collectors;

import Utils.CLI;
import Utils.readFile;
import Utils.Config;


public class MandyTree implements BTree {
    //Tree specific parameters here
    private double MIN_FILL_FACTOR = 0.5;
    private static int DEGREE = 8;
    private InternalNode root = null;

    private LeafNode firstLeaf = null;


    //some internal statistics for debugging
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

    //my constructor
    public MandyTree(double MIN_FILL_FACTOR, int DEGREE) {
        root = null;
        this.MIN_FILL_FACTOR = MIN_FILL_FACTOR;
        this.DEGREE = DEGREE;
    }

    private static abstract class Node {
        //fill in your implementation about Node in common here
        protected InternalNode parent;
        protected Node [] childPointers;

        protected Node left;

        protected Node right;

        int maxDegree;

        int minDegree;

        int degree;

        Integer[] keys;
    }

    // Method to find the first null element in an array of Node pointers
    private static int linearNullSearch(Node[] pointers) {
        // Iterate through each element in the array
        for (int i = 0; i < pointers.length; i++) {
            // Check if the current element is null
            if (pointers[i] == null) {
                // If null, return the current index as the first null position
                return i;
            }
        }
        // If no null elements were found, return -1 indicating a full array
        return -1;
    }

    // Method to find the first null element in an array of DictionaryPair
    private int linearNullSearch(DictionaryPair[] dps) {
        // Iterate over each element in the DictionaryPair array
        for (int i = 0; i < dps.length; i++) {
            // Check if the current element is null
            if (dps[i] == null) {
                // Return the index of the first null element found
                return i;
            }
        }
        // If no null elements are found, return -1
        return -1;
    }



    //LeafNode
    private static class InternalNode extends Node {
        // Method to append a child pointer to the node.
        private void appendChildPointer(Node pointer) {
            this.childPointers[degree] = pointer;
            this.degree++;
        }

        // Find the index of a given pointer in the child pointers array.
        private int findIndexOfPointer(Node pointer) {
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i] == pointer) {
                    return i;
                }
            }
            return -1;
        }

        // Insert a child pointer at a specified index.
        private void insertChildPointer(Node pointer, int index) {
            for (int i = degree - 1; i >= index; i--) {
                childPointers[i + 1] = childPointers[i];
            }
            this.childPointers[index] = pointer;
            this.degree++;
        }

        // Check if the node is deficient (i.e., has fewer children than the minimum degree).
        private boolean isDeficient() {
            return this.degree < this.minDegree;
        }

        // Check if the node has more children than the minimum required, hence can lend a child.
        private boolean isLendable() {
            return this.degree > this.minDegree;
        }

        // Check if the node can be merged with another node (i.e., has exactly the minimum number of children).
        private boolean isMergeable() {
            return this.degree == this.minDegree;
        }

        // Check if the node has more children than the maximum allowed, hence needs splitting.
        private boolean isOverfull() {
            return this.degree == maxDegree + 1;
        }

        // Add a child pointer to the beginning of the child pointer array.
        private void prependChildPointer(Node pointer) {
            for (int i = degree - 1; i >= 0; i--) {
                childPointers[i + 1] = childPointers[i];
            }
            this.childPointers[0] = pointer;
            this.degree++;
        }

        // Remove a key at a specific index.
        private void removeKey(int index) {
            this.keys[index] = null;
        }

        // Remove a pointer at a specific index.
        private void removePointer(int index) {
            this.childPointers[index] = null;
            this.degree--;
        }

        // Remove a specific pointer from the array of child pointers.
        private void removePointer(Node pointer) {
            for (int i = 0; i < childPointers.length; i++) {
                if (childPointers[i] == pointer) {
                    this.childPointers[i] = null;
                }
            }
            this.degree--;
        }

        // Constructor to initialize an InternalNode with keys.
        private InternalNode(int m, Integer[] keys) {
            this.maxDegree = m;
            this.minDegree = (int) Math.ceil(m / 2.0);
            this.degree = 0;
            this.keys = keys;
            this.childPointers = new Node[this.maxDegree + 1];
        }

        // Constructor to initialize an InternalNode with keys and child pointers.
        private InternalNode(int m, Integer[] keys, Node[] pointers) {
            this.maxDegree = m;
            this.minDegree = (int) Math.ceil(m / 2.0);
            this.degree = linearNullSearch(pointers);
            this.keys = keys;
            this.childPointers = pointers;
        }

    }


    //IndexNode
    public class LeafNode extends Node {
        int maxNumPairs; // Maximum number of key-value pairs this leaf can hold.
        int minNumPairs; // Minimum number of key-value pairs this leaf should hold after deletion.
        int numPairs; // Current number of key-value pairs in the leaf.
        LeafNode leftSibling; // Reference to the left sibling leaf node.
        LeafNode rightSibling; // Reference to the right sibling leaf node.
        DictionaryPair[] dictionary; // Array of key-value pairs stored in the leaf.

        // Delete a key-value pair at a specified index.
        public void delete(int index) {
            this.dictionary[index] = null;
            numPairs--;
        }

        // Insert a new key-value pair into the leaf.
        public boolean insert(DictionaryPair dp) {
            if (this.isFull()) {
                return false; // Cannot insert if the leaf is full.
            } else {
                this.dictionary[numPairs] = dp;
                numPairs++;
                Arrays.sort(this.dictionary, 0, numPairs); // Keep the dictionary sorted.
                return true;
            }
        }

        // Check if the leaf node is deficient (has fewer pairs than the minimum required).
        public boolean isDeficient() {
            return numPairs < minNumPairs;
        }

        // Check if the leaf node is full (has reached its maximum capacity).
        public boolean isFull() {
            return numPairs == maxNumPairs;
        }

        // Check if the leaf node can lend a key-value pair to a sibling (has more than the minimum pairs).
        public boolean isLendable() {
            return numPairs > minNumPairs;
        }

        // Check if the leaf node can be merged with a sibling (has exactly the minimum number of pairs).
        public boolean isMergeable() {
            return numPairs == minNumPairs;
        }

        // Constructor for initializing a leaf node with a single key-value pair.
        public LeafNode(int m, DictionaryPair dp) {
            this.maxNumPairs = m - 1; // As per B+ tree properties, a leaf node can hold up to m-1 key-value pairs.
            this.minNumPairs = (int) (Math.ceil(m / 2) - 1); // Minimum pairs required after deletion to avoid underflow.
            this.dictionary = new DictionaryPair[m]; // Initialize the dictionary array.
            this.numPairs = 0; // Initially, the number of pairs is zero.
            this.insert(dp); // Insert the provided key-value pair.
        }

        // Constructor for initializing a leaf node with a set of key-value pairs and a parent node.
        public LeafNode(int m, DictionaryPair[] dps, InternalNode parent) {
            this.maxNumPairs = m - 1; // Maximum number of pairs that can be stored.
            this.minNumPairs = (int) (Math.ceil(m / 2) - 1); // Minimum pairs required to maintain the tree properties.
            this.dictionary = dps; // Initialize the dictionary with the provided array.
            this.numPairs = linearNullSearch(dps); // Determine the number of pairs currently in the dictionary.
            this.parent = parent; // Set the parent of the leaf node.
        }


    }

    public class DictionaryPair implements Comparable<DictionaryPair> {
        int key;
        double value;

        public DictionaryPair(int key, int value) {
            this.key = key;
            this.value = value;
        }

        public int compareTo(DictionaryPair o) {
            if (key == o.key) {
                return 0;
            } else if (key > o.key) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * This method shifts elements in an array of Node pointers downwards by a given amount.
     * Purpose: This method is used to shift the child pointers in an internal node downwards, creating space at the beginning of the array. This is often needed during node splitting and redistribution of pointers.
     */
    private void shiftDown(Node[] pointers, int amount) {
        Node[] newPointers = new Node[DEGREE + 1]; // Create a new array of pointers.
        for (int i = amount; i < pointers.length; i++) {
            newPointers[i - amount] = pointers[i]; // Shift each pointer down by 'amount'.
        }
        pointers = newPointers; // Update the original pointers array.
    }

    //This method sorts an array of DictionaryPair objects.
    private void sortDictionary(DictionaryPair[] dictionary) {
        Arrays.sort(dictionary, new Comparator<DictionaryPair>() {
            @Override
            public int compare(DictionaryPair o1, DictionaryPair o2) {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2); // Compare the dictionary pairs based on their keys.
            }
        });
    }

    //This method splits the child pointers of an internal node around a specified split index.
    private Node[] splitChildPointers(InternalNode in, int split) {
        Node[] pointers = in.childPointers; // The original child pointers.
        Node[] halfPointers = new Node[DEGREE+ 1]; // Array to hold the second half of the split pointers.

        for (int i = split + 1; i < pointers.length; i++) {
            halfPointers[i - split - 1] = pointers[i]; // Populate the second half array.
            in.removePointer(i); // Remove the pointer from the original node.
        }

        return halfPointers; // Return the second half pointers.
    }

    //This method splits the dictionary of a leaf node at a given split index.
    private DictionaryPair[] splitDictionary(LeafNode ln, int split) {
        DictionaryPair[] dictionary = ln.dictionary; // The original dictionary.
        DictionaryPair[] halfDict = new DictionaryPair[DEGREE]; // Array to hold the second half of the dictionary.

        for (int i = split; i < dictionary.length; i++) {
            halfDict[i - split] = dictionary[i]; // Populate the second half array.
            ln.delete(i); // Remove the dictionary pair from the original leaf.
        }

        return halfDict; // Return the second half dictionary.
    }

    // Get the midpoint of a node
    private int getMidpoint() {
        // Calculate the midpoint. For a node of order 'm', it is ceil((m+1)/2) - 1
        return (int) Math.ceil((DEGREE + 1) / 2.0) - 1;
    }


    private Integer[] splitKeys(Integer[] keys, int split) {
        Integer[] halfKeys = new Integer[DEGREE]; // Array to hold the second half of the keys.

        keys[split] = null; // Nullify the split key in the original array.

        for (int i = split + 1; i < keys.length; i++) {
            halfKeys[i - split - 1] = keys[i]; // Populate the second half array.
            keys[i] = null; // Nullify the key in the original array.
        }

        return halfKeys; // Return the second half keys.
    }


    private void splitInternalNode(InternalNode in) {

        InternalNode parent = in.parent;

        int midpoint = getMidpoint();
        int newParentKey = in.keys[midpoint];
        Integer[] halfKeys = splitKeys(in.keys, midpoint);
        Node[] halfPointers = splitChildPointers(in, midpoint);

        in.degree = linearNullSearch(in.childPointers);

        InternalNode sibling = new InternalNode(DEGREE, halfKeys, halfPointers);
        for (Node pointer : halfPointers) {
            if (pointer != null) {
                pointer.parent = sibling;
            }
        }

        sibling.right = in.right;
        if (sibling.right != null) {
            sibling.right.left = sibling;
        }
        in.right = sibling;
        sibling.left = in;

        if (parent == null) {

            Integer[] keys = new Integer[DEGREE];
            keys[0] = newParentKey;
            InternalNode newRoot = new InternalNode(DEGREE, keys);
            newRoot.appendChildPointer(in);
            newRoot.appendChildPointer(sibling);
            this.root = newRoot;

            in.parent = newRoot;
            sibling.parent = newRoot;

        } else {

            parent.keys[parent.degree - 1] = newParentKey;
            Arrays.sort(parent.keys, 0, parent.degree);

            int pointerIndex = parent.findIndexOfPointer(in) + 1;
            parent.insertChildPointer(sibling, pointerIndex);
            sibling.parent = parent;
        }
    }

    // Find the leaf node
    private LeafNode findLeafNode(int key) {

        Integer[] keys = this.root.keys;
        int i;

        for (i = 0; i < this.root.degree - 1; i++) {
            if (key < keys[i]) {
                break;
            }
        }

        Node child = this.root.childPointers[i];
        if (child instanceof LeafNode) {
            return (LeafNode) child;
        } else {
            return findLeafNode((InternalNode) child, key);
        }
    }

    // Find the leaf node
    private LeafNode findLeafNode(InternalNode node, int key) {

        Integer[] keys = node.keys;
        int i;

        for (i = 0; i < node.degree - 1; i++) {
            if (key < keys[i]) {
                break;
            }
        }
        Node childNode = node.childPointers[i];
        if (childNode instanceof LeafNode) {
            return (LeafNode) childNode;
        } else {
            return findLeafNode((InternalNode) node.childPointers[i], key);
        }
    }

    private boolean isEmpty() {
        return firstLeaf == null;
    }


    /**
     * Insert key to tree
     * @param key
     */
    public void insert(Integer key) {
        if (isEmpty()) {

            LeafNode ln = new LeafNode(DEGREE, new DictionaryPair(key, key));

            this.firstLeaf = ln;

        } else {
            LeafNode ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

            if (!ln.insert(new DictionaryPair(key, key))) {

                ln.dictionary[ln.numPairs] = new DictionaryPair(key, key);
                ln.numPairs++;
                sortDictionary(ln.dictionary);

                int midpoint = getMidpoint();
                DictionaryPair[] halfDict = splitDictionary(ln, midpoint);

                if (ln.parent == null) {

                    Integer[] parent_keys = new Integer[DEGREE];
                    parent_keys[0] = halfDict[0].key;
                    InternalNode parent = new InternalNode(DEGREE, parent_keys);
                    ln.parent = parent;
                    parent.appendChildPointer(ln);

                } else {
                    int newParentKey = halfDict[0].key;
                    ln.parent.keys[ln.parent.degree - 1] = newParentKey;
                    Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
                }

                LeafNode newLeafNode = new LeafNode(DEGREE, halfDict, ln.parent);

                int pointerIndex = ln.parent.findIndexOfPointer(ln) + 1;
                ln.parent.insertChildPointer(newLeafNode, pointerIndex);

                newLeafNode.rightSibling = ln.rightSibling;
                if (newLeafNode.rightSibling != null) {
                    newLeafNode.rightSibling.leftSibling = newLeafNode;
                }
                ln.rightSibling = newLeafNode;
                newLeafNode.leftSibling = ln;

                if (this.root == null) {

                    this.root = ln.parent;

                } else {
                    InternalNode in = ln.parent;
                    while (in != null) {
                        if (in.isOverfull()) {
                            splitInternalNode(in);
                        } else {
                            break;
                        }
                        in = in.parent;
                    }
                }
            }
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

        ArrayList<Integer> values = new ArrayList<Integer>();

        LeafNode currNode = this.firstLeaf;
        while (currNode != null) {

            DictionaryPair dps[] = currNode.dictionary;
            for (DictionaryPair dp : dps) {

                if (dp == null) {
                    break;
                }

                if (key1 <= dp.key && dp.key <= key2) {
                    values.add((int) dp.value);
                }
            }
            currNode = currNode.rightSibling;

        }

        return values;



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
     * Print the tree starting from the root.
     */
    /**
     * Print the tree starting from the root.
     */
    public void printTree() {
        printNode(root, "", true);
    }

    /**
     * Recursively print the nodes of the tree.
     * @param node The current node to print.
     * @param prefix Prefix used for formatting the tree structure in the output.
     * @param isTail Indicates if the node is the last child of its parent (for formatting).
     */
    private void printNode(Node node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "└── " : "├── ") + nodeToString(node));
        String newPrefix = prefix + (isTail ? "    " : "│   ");

        // Check if the node is an internal node
        if (node instanceof InternalNode) {
            InternalNode internalNode = (InternalNode) node;
            for (int i = 0; i < internalNode.degree; i++) {
                printNode(internalNode.childPointers[i], newPrefix, i == internalNode.degree - 1);
            }
        }
    }

    private String nodeToString(Node node) {
        if (node instanceof InternalNode) {
            return "InternalNode: " + Arrays.toString(((InternalNode) node).keys);
        } else if (node instanceof LeafNode) {
            LeafNode leaf = (LeafNode) node;
            return "LeafNode: " + dictionaryToString(leaf.dictionary);
        }
        return node.toString();
    }


    private String dictionaryToString(DictionaryPair[] dictionary) {
        return Arrays.stream(dictionary)
                .filter(Objects::nonNull) // Filter out null values
                .map(dp -> String.valueOf(dp.value)) // Convert each DictionaryPair to a string representing its value
                .collect(Collectors.joining(", ", "[", "]")); // Join the values in a readable format
    }





    /**
     * print tree from node
     * @param n starting node to print
     */
    /**
     * Print the tree from a specific node.
     * @param n The starting node to print from.
     */
    public void printTree(Node n) {
        printNode(n, "", true);
    }


    @Override
    public void load(String datafilename) {
        String[] readLines = readFile.readData(datafilename);
        //Fill in you work here

    }
    
    public static void main(String[] args) {
        //we hardcode the fill factor and degree for this project
        BTree mandyTree = new MandyTree(0.5, 8);
        //the value is stored in Config.java
        //build a mandyTree from the data file
        mandyTree.load(Config.dataFileName);

        //interact with the tree via a text interface.
        CLI.shell(mandyTree);


    }
}
