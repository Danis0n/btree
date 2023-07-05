package rd.com.tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class BTree<K extends Comparable<K>, V> implements Iterable<BTree.Entry<K,V>> {

    private static final int T = 10;
    private Node root;

    private class EntryComparator implements Comparator<Entry<K,V>> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return o1.key.compareTo(o2.key);
        }
    }

    @Override
    public Iterator<Entry<K,V>> iterator() {
        return new BTreeIterator();
    }

    private class BTreeIterator implements Iterator<Entry<K,V>> {
        Queue<Entry<K,V>> entriesToVisit = new PriorityQueue<>();

        public BTreeIterator() {
            if (root != null) {
                entriesToVisit = collect(root);
            };
        }

        private Queue<Entry<K, V>> collect(Node x) {
            Queue<Entry<K, V>> entries =
                    new PriorityQueue<>(new EntryComparator());

            for (int i = 0; i < x.n; i++) {
                entries.add(x.entries[i]);
            }

            if (!x.leaf) {
                for (int i = 0; i < x.n + 1; i++) {
                    if (x.children[i] != null)
                        entries.addAll(collect(x.children[i]));
                }
            }

            return entries;
        }


        @Override
        public boolean hasNext() {
            return !entriesToVisit.isEmpty();
        }

        @Override
        public Entry next() {
            return entriesToVisit.poll();
        }

    }

    public static class Node {
        int n;
        Entry[] entries = new Entry[2 * T - 1];
        Node[] children = new Node[2 * T];
        boolean leaf = true;

        @Override
        public String toString() {
            return "Node{" +
                    "n=" + n +
                    ", entries=" + Arrays.toString(entries) +
                    ", children=" + Arrays.toString(children) +
                    ", leaf=" + leaf +
                    '}';
        }
    }

    public static class Entry<K extends Comparable<K>, V> implements Comparable<Entry<K,V>> {
        Comparable<K> key;
        V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

        @Override
        public int compareTo(Entry o) {
            return this.key.compareTo((K) o.key);
        }
    }

    public BTree() {
        root = new Node();
        root.n = 0;
        root.leaf = true;
    }

    public BTree(String filePath) {
        root = new Node();
        root.n = 0;
        root.leaf = true;
        loadFromFile(filePath);
    }

    private void loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("-");

                K key = (K) parts[0];
                V value = (V) parts[1];

                set(key, value);
            }

            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void set(final K key, final V value) {
        Node r = root;
        if (r.n == 2 * T - 1) {
            Node s = new Node();
            root = s;
            s.leaf = false;
            s.n = 0;
            s.children[0] = r;
            split(s, 0, r);
            insertValue(s, key, value);
        } else {
            insertValue(r, key, value);
        }
    }

    private void insertValue(Node x, final K k, final V v) {
        if (x.leaf) {
            int i = 0;
            for (i = x.n - 1; i >= 0 && less(k, x.entries[i].key); i--) {
                x.entries[i + 1] = x.entries[i];
            }

            Entry<K, V> entry = new Entry<>(k, v);
            x.entries[i + 1] = entry;
            x.n = x.n + 1;
        } else {
            int i = 0;
            for (i = x.n - 1; i >= 0 && less(k, x.entries[i].key); i--) {}

            i++;
            Node tmp = x.children[i];
            if (tmp.n == 2 * T - 1) {
                split(x, i, tmp);
                if (less(k, x.entries[i].key)) {
                    i++;
                }
            }
            insertValue(x.children[i], k, v);
        }

    }

    public void print() {
        print(root);
    }

    private void print(Node x) {
        if (x == null) return;

        for (int i = 0; i < x.n; i++) {
            System.out.print(x.entries[i].key + " -> "  + x.entries[i].value + "\n");
        }
        if (!x.leaf) {
            for (int i = 0; i < x.n + 1; i++) {
                print(x.children[i]);
            }
        }
    }

    public List<Entry<K,V>> lessThan(K key) {
        if (key == null) return null;
        return searchLess(root, key);
    }

    private List<Entry<K,V>> searchLess(Node node, K key) {
        List<Entry<K,V>> entries = new ArrayList<>();

        for (int j = 0; j < node.n; j++) {
            if (less(node.entries[j].key, key))
                entries.add(node.entries[j]);
        }

        for (Node x : node.children) {
            if (x != null) entries.addAll(searchLess(x, key));
        }

        return entries;
    }

    public List<Entry<K,V>> moreThan(K key) {
        if (key == null) return null;
        return searchMore(root, key);
    }

    private List<Entry<K,V>> searchMore(Node node, K key) {
        List<Entry<K,V>> entries = new ArrayList<>();

        for (int j = 0; j < node.n; j++) {
            if (more(node.entries[j].key, key))
                entries.add(node.entries[j]);
        }

        for (Node x : node.children) {
            if (x != null) entries.addAll(searchMore(x, key));
        }

        return entries;
    }

    public List<Entry<K,V>> inRange(K more, K less, boolean isSorted) {
        if (more == null || less == null) return null;
        if (more(more, less)) {
            K tmp = more;
            more = less;
            less = tmp;
        }
        return searchInRange(root, more, less, isSorted);
    }

    private List<Entry<K,V>> searchInRange(Node node, K more, K less, boolean isSorted) {
        List<Entry<K,V>> entries = new ArrayList<>();

        for (int i = 0; i < node.n; i++) {
            if (more(node.entries[i].key, more) &&
                    less(node.entries[i].key, less))
                entries.add(node.entries[i]);
        }

        for (Node x : node.children) {
            if (x != null)
                entries.addAll(searchInRange(x, more, less, isSorted));
        }

        return isSorted ? entries.stream().sorted().toList() : entries;
    }

    public Entry<K,V> last() {
        Node x = root;
        while (!x.leaf) {
            x = Arrays.stream(x.children)
                    .filter(Objects::nonNull)
                    .reduce((first, second) -> second).get();
        }

        return x.entries[x.n - 1];
    }

    public Entry<K,V> first() {
        Node x = root;
        while (!x.leaf) {
            x = x.children[0];
        }

        return x.entries[0];
    }

    private boolean less(Comparable val1, Comparable val2) {
        return val1.compareTo(val2) < 0;
    }

    private boolean more(Comparable val1, Comparable val2) {
        return val1.compareTo(val2) > 0;
    }

    private boolean eq(Comparable val1, Comparable val2) {
        return val1.compareTo(val2) == 0;
    }

    private V search(Node x, K key) {
        int i = 0;
        if (x == null)
            return null;
        for (i = 0; i < x.n; i++) {
            if (less(key, x.entries[i].key)) {
                break;
            }
            if (eq(key, x.entries[i].key)) {
                return (V) x.entries[i].value;
            }
        }
        if (x.leaf) {
            return null;
        } else {
            return search(x.children[i], key);
        }
    }

    private void split(Node x, int pos, Node y) {
        Node z = new Node();
        z.leaf = y.leaf;
        z.n = T - 1;

        for (int j = 0; j < T - 1; j++) {
            z.entries[j] = y.entries[j + T];
            y.entries[j + T] = null;
        }
        if (!y.leaf) {
            for (int j = 0; j < T; j++) {
                z.children[j] = y.children[j + T];
                y.children[j + T] = null;
            }
        }
        y.n = T - 1;
        for (int j = x.n; j >= pos + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[pos + 1] = z;

        for (int j = x.n - 1; j >= pos; j--) {
            x.entries[j + 1] = x.entries[j];
        }
        x.entries[pos] = y.entries[T - 1];
        x.n = x.n + 1;

    }

    public boolean contains(K key) {
        return search(root, key) != null;
    }

    public V get(K key) {
        return key != null ? search(root, key) : null;
    }

}
