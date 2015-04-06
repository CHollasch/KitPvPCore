import javax.swing.*;

/**
 * @author Connor Hollasch
 * @since 3/30/2015
 */
public class KitPvPTest {

    public static void main(String[] args) throws Exception {
        String test = "Unlocke this kit at rank &75";
        System.out.println(test.substring(test.lastIndexOf(" ")+3));
    }
}
