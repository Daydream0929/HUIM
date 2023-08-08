package src.test;

import src.algorithms.alg2.Alg;
import src.data.main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class MainTestAlg2 {
    public static void main(String [] arg) throws IOException {

        String input = dataToPath("chainstore.txt");
        String output = fileToPath(".//output.txt");

        int min_utility = 5000000;  //
        int p_size = 63;

        // Applying the HUIMiner algorithm
        Alg alg2 = new Alg();
        alg2.runAlgorithm(input, output, p_size, min_utility);
        alg2.printStats();

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
