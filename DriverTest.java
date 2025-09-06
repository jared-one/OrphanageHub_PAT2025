public class DriverTest {
    public static void main(String[] args) {
        try {
            Class.forName("net.sf.ucanaccess.jdbc.UcanaccessDriver");
            System.out.println("✓ UCanAccess driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
