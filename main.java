import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class main {
    public static void main(String [] arg) throws IOException {

        System.out.println("Hello World!");

    }

    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = main.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }
}