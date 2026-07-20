import java.util.Locale;
import java.util.ResourceBundle;
import java.io.File;

public class TestI18n {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing i18n...");
        try {
            ResourceBundle mrBundle = ResourceBundle.getBundle("i18n.messages", new Locale("mr"));
            System.out.println("Marathi Dashboard: " + mrBundle.getString("dashboard"));
            System.out.println("Marathi Masters: " + mrBundle.getString("menu.mastersHub"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
