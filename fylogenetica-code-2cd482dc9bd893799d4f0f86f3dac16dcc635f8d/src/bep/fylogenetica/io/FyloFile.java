package bep.fylogenetica.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Quartet;

/**
 * This class provides methods to write a representation of the quartets and
 * taxon count to a file, and to read such a file again.
 */
public class FyloFile {
	
	/**
	 * Writes to a file.
	 * 
	 * @param f The {@link Fylogenetica} object to write.
	 * @param file The file to write to.
	 * @throws FileNotFoundException If the file could not be created or written to.
	 */
	public static void write(Fylogenetica f, File file) throws FileNotFoundException {
		PrintWriter w = new PrintWriter(file);
		
		w.println(f.model.taxonCount);
		w.println();
		
		for (Quartet q : f.model.quartets) {
			w.println(q.left1 + " " + q.left2 + " " + q.right1 + " " + q.right2);
		}
		
		w.close();
	}

	/**
	 * Reads from a file.
	 * 
	 * @param f The {@link Fylogenetica} object to put the results in.
	 * @param file The file to read from.
	 * @throws FileNotFoundException If the file could not be found or read from.
	 */
	public static void read(Fylogenetica f, File file) throws FileNotFoundException {
		Scanner s = new Scanner(file);
		
		f.model.taxonCount = s.nextInt();
		
		f.model.quartets.clear();
		while (s.hasNextInt()) {
			f.model.quartets.add(new Quartet(s.nextInt(), s.nextInt(), s.nextInt(), s.nextInt()));
		}
	}

}
