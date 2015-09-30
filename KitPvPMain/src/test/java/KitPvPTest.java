import java.util.Arrays;

/**
 * @author Connor Hollasch
 * @since 3/30/2015
 */
public class KitPvPTest {

    public static void main(String[] args) throws Exception {
        String test = "Unkock this kit at rank &75";
        String[] src = test.split(" ");
        String[] dest = new String[src.length - 2];

        System.arraycopy(src, 2, dest, 0, dest.length);

        System.out.println(Arrays.toString(dest));
    }
}
