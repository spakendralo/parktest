package lu.pistache.parktest;


//Loads images, detects faces and draws bounding boxes.Determines exif orientation, if necessary.

//Import the basic graphics classes.

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.util.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


// Calls DetectFaces and displays a bounding box around each detected image.
public class DetectParkedCars extends JPanel {

    private static final long serialVersionUID = 1L;

    BufferedImage image;
    static int scale;
    DetectLabelsResult result;

    public DetectParkedCars(DetectLabelsResult labelsResult, BufferedImage bufImage) throws Exception {
        super();
        scale = 1; // increase to shrink image size.

        result = labelsResult;
        image = bufImage;


    }
    // Draws the bounding box around the detected faces.
    public void paintComponent(Graphics g) {
        float left = 0;
        float top = 0;
        int height = image.getHeight(this);
        int width = image.getWidth(this);

        Graphics2D g2d = (Graphics2D) g; // Create a Java2D version of g.

        // Draw the image.
        g2d.drawImage(image, 0, 0, width / scale, height / scale, this);
        g2d.setColor(new Color(0, 212, 0));

        // Iterate through faces and display bounding boxes.
        List<Label> labels = result.getLabels();
        for (Label label : labels) {
            if (label.getName().equals("Car")) { //otherwise it's a person, parking lot, cat, ...
                List<Instance> instances = label.getInstances();
                if (instances.isEmpty()) {
                    System.out.println("  " + "Nothing to plot");
                } else {
                    for (Instance instance : instances) {
                        System.out.println("  Confidence: " + instance.getConfidence().toString());
                        System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
                        BoundingBox box = instance.getBoundingBox();
                        left = width * box.getLeft();
                        top = height * box.getTop();
                        g2d.drawRect(Math.round(left / scale), Math.round(top / scale),
                                Math.round((width * box.getWidth()) / scale), Math.round((height * box.getHeight())) / scale);
                    }
                }


            }

        }
    }


    public static void main(String arg[]) throws Exception {

        int height = 0;
        int width = 0;


        String photo = "src/main/resources/sweden.jpg";
        BufferedImage image = ImageIO.read(new FileInputStream(new File(photo)));

        ByteBuffer imageBytes;
        try (InputStream inputStream = new FileInputStream(new File(photo))) {
            imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
        }

        DetectLabelsRequest request = new DetectLabelsRequest()
                .withImage(new Image().withBytes(imageBytes))
                .withMaxLabels(30).withMinConfidence((float) 0.10);

        width = image.getWidth();
        height = image.getHeight();

        // Call DetectFaces
        AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();
        DetectLabelsResult result = amazonRekognition.detectLabels(request);
        List<Label> labels = result.getLabels();


        for (Label label : labels) {
            System.out.println("Label: " + label.getName());
            System.out.println("Confidence: " + label.getConfidence().toString() + "\n");

            List<Instance> instances = label.getInstances();
            System.out.println("Instances of " + label.getName());
            if (instances.isEmpty()) {
                System.out.println("  " + "None");
            } else {
                for (Instance instance : instances) {
                    System.out.println("  Confidence: " + instance.getConfidence().toString());
                    System.out.println("  Bounding box: " + instance.getBoundingBox().toString());
                }
            }
            System.out.println("Parent labels for " + label.getName() + ":");
            List<Parent> parents = label.getParents();
            if (parents.isEmpty()) {
                System.out.println("  None");
            } else {
                for (Parent parent : parents) {
                    System.out.println("  " + parent.getName());
                }
            }
            System.out.println("--------------------");
            System.out.println();

        }

        // Create frame and panel.
        JFrame frame = new JFrame("RotateImage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DetectParkedCars panel = new DetectParkedCars(result, image);
        panel.setPreferredSize(new Dimension(image.getWidth() / scale, image.getHeight() / scale));
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);

    }
}



