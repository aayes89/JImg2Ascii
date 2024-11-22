/**
 *
 * @author Slam
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JImg2Text {

    public static void main(String[] args) {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileNameExtensionFilter("Image file", "png", "jpeg", "jpg", "bmp"));
        jfc.showOpenDialog(null);
        File img = jfc.getSelectedFile();
        if (img != null) {
            // Parsing arguments
            String inputPath = img.getAbsolutePath(); // Path to input image
            String outputPath = "output_" + img.getName() + ".txt"; // Path to output text file
            String mode = "full"; // "fast" or "full"
            int numCols = 150; // Number of characters for output width

            try {
                new JImg2Text().convertImageToAscii(inputPath, outputPath, mode, numCols);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void convertImageToAscii(String inputPath, String outputPath, String mode, int numCols) throws IOException {
        // Define character sets
        final String SMALL_LIST = "@%#*+=-:. ";
        final String FULL_LIST = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";
        final String CHAR_LIST = mode.equals("fast") ? SMALL_LIST : FULL_LIST;

        int numChars = CHAR_LIST.length();

        // Load image and convert to grayscale
        BufferedImage image = ImageIO.read(new File(inputPath));
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16 & 0xFF) * 30 / 100 + (rgb >> 8 & 0xFF) * 59 / 100 + (rgb & 0xFF) * 11 / 100;
                grayImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
            }
        }

        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        double cellWidth = (double) width / numCols;
        double cellHeight = 2 * cellWidth;
        int numRows = (int) (height / cellHeight);

        if (numCols > width || numRows > height) {
            System.out.println("Too many columns or rows. Using default settings.");
            cellWidth = 6;
            cellHeight = 12;
            numCols = (int) (width / cellWidth);
            numRows = (int) (height / cellHeight);
        }

        // Convert to ASCII
        FileWriter outputFile = new FileWriter(outputPath);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int startX = (int) (j * cellWidth);
                int endX = Math.min((int) ((j + 1) * cellWidth), width);
                int startY = (int) (i * cellHeight);
                int endY = Math.min((int) ((i + 1) * cellHeight), height);

                double avg = calculateAverageGray(grayImage, startX, endX, startY, endY);
                int charIndex = Math.min((int) (avg * numChars / 255), numChars - 1);

                outputFile.write(CHAR_LIST.charAt(charIndex));
            }
            outputFile.write("\n");
        }
        outputFile.close();
    }

    private double calculateAverageGray(BufferedImage image, int startX, int endX, int startY, int endY) {
        double sum = 0;
        int count = 0;
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int gray = image.getRGB(x, y) & 0xFF;
                sum += gray;
                count++;
            }
        }
        return sum / count;
    }
}
