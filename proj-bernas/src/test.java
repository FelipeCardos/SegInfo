public class test {
    public static void main(String[] args) {
        String s = "1.23.00.0:5000";
        String[] ss = s.split(":");
        for (String x : ss) {
            System.out.println(x);
        }
    }
}
