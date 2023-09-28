package bep.fylogenetica.io.ipe;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class IpeTest {
	
	public static void main(String[] args) {
		IpeDocument ipe = new IpeDocument();
		ipe.addToPreamble("\\usepackage{mathpazo}");
		ipe.addToPreamble("\\usepackage[euler-digits]{eulervm}");
		
		ipe.drawLine(new Point2D(100, 100), new Point2D(200, 260), Color.RED);
		ipe.drawPolygon(Color.RED, true, new Point2D(110, 100), new Point2D(210, 260), new Point2D(310, 100));
		
		ArrayList<Point2D> data = new ArrayList<>();
		for (double x = 0; x < 4; x += 0.01) {
			data.add(new Point2D(x, Math.sqrt(x)));
		}
		ipe.drawObject(new Point2D(100, 400), new Point2D(150, 100), new IpeLineGraph(data));
		
		try {
			ipe.writeToFile(new File("/home/willem/ipetestje.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
