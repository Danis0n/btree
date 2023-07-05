package rd.com.tree;


import java.util.function.Consumer;
import java.util.stream.IntStream;

public class BTreeTest {

    private static class BTreeConsumerSOUT implements Consumer<BTree.Entry<?,?>> {
        @Override
        public void accept(BTree.Entry<?,?> entry) {
            System.out.println(entry);
        }
    }

    public static void testBtreeLoadFile(String filePath) {

        long time = System.nanoTime();
        BTree<String, String> bTree = new BTree<>(filePath,7);
        System.out.println("File loading + constructor time elapsed: " + (System.nanoTime() - time) + "\n");

        time = System.nanoTime();
        bTree.iterator().forEachRemaining(new BTreeConsumerSOUT());
        System.out.println("Iterator time elapsed: " + (System.nanoTime() - time) + "\n");
        System.out.println("=====================");

        System.out.println("First element: " + bTree.first());
        System.out.println("Last element: " + bTree.last());
        System.out.println("=====================");

        System.out.println("Less than 'WYyMugDwOk' elements: " + bTree.lessThan("WYyMugDwOk"));
        System.out.println("More than 'nmMADRnKXx' elements: " + bTree.moreThan("nmMADRnKXx"));
        System.out.println("In range elements: " + bTree.inRange("dsag", "432kffds", false));
        System.out.println("=====================");

        time = System.nanoTime();
        System.out.println();
        System.out.println(bTree.get("to8FmxljQF") + " Search time elapsed: " + (System.nanoTime() - time) + "\n");
        System.out.println("<=====================>\n\n\n");

    }

    public static void testBTreeLoadIntegers(int max) {
        long time = System.nanoTime();
        BTree<Integer, Integer> bTree = new BTree<>(5);
        System.out.println("Constructor time elapsed: " + (System.nanoTime() - time) + "\n");

        time = System.nanoTime();
        IntStream.range(1, max).forEach(value -> bTree.set(value, value));
        System.out.println("BTree filled in (" + max + ") : " + (System.nanoTime() - time) + "\n");

        time = System.nanoTime();
        bTree.iterator().forEachRemaining(new BTreeConsumerSOUT());
        System.out.println("Iterator time elapsed: " + (System.nanoTime() - time) + "\n");
        System.out.println("=====================");

        System.out.println("First element: " + bTree.first());
        System.out.println("Last element: " + bTree.last());
        System.out.println("=====================");

        System.out.println("Less than '99' elements: " + bTree.lessThan(99));
        System.out.println("More than '190' elements: " + bTree.moreThan(190));
        System.out.println("In range elements: " + bTree.inRange(20, 100, true));
        System.out.println("=====================");

        time = System.nanoTime();
        System.out.println(bTree.get(55) + " Search time elapsed: " + (System.nanoTime() - time) + "\n");
        System.out.println("<=====================>\n\n\n");

    }

    public static void main(String[] args) {

        testBtreeLoadFile("files/strings.txt");

        testBTreeLoadIntegers(200);
//        testBTreeLoadIntegers(10_000);
    }

}
