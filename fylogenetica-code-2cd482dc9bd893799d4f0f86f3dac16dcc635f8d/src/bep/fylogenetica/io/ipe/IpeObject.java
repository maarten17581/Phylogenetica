package bep.fylogenetica.io.ipe;

/**
 * Implemented by any object that can be included inside an Ipe object.
 */
public interface IpeObject {
	
	/**
	 * Draws this object on the given Ipe document.
	 * 
	 * <p>Instead of calling this method, you should call the equivalent
	 * {@link IpeDocument#drawObject(IpeObject)}.</p>
	 * 
	 * @param p The coordinate of the lower-left corner of the object.
	 * @param size The size of the object.
	 * @param ipe The document to draw on.
	 */
	void draw(Point2D p, Point2D size, IpeDocument ipe);
}
