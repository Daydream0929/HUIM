package src.test;
import src.data.main;
import src.algorithms.alg1.Alg;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class MainTestAlg1 {
    public static void main(String [] arg) throws IOException {

        String input = dataToPath("chainstore.txt");
        String output = fileToPath(".//output.txt");

        int min_utility = 5000000;  //

        // Applying the HUIMiner algorithm
        Alg alg1 = new Alg();
        alg1.runAlgorithm(input, output, min_utility);
        alg1.printStats();

    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestAlg1.class.getResource(filename);
        assert url != null;
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }

    public static String dataToPath(String filename) throws UnsupportedEncodingException {
        URL url = main.class.getResource(filename);
        assert url != null;
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}
