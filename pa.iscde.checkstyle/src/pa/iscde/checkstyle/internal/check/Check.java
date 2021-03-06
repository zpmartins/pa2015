package pa.iscde.checkstyle.internal.check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import pa.iscde.checkstyle.model.SeverityType;
import pa.iscde.checkstyle.model.Violation;

/**
 * This class defines the abstract concept Check, based on which all the
 * specific checks, concrete classes, should be implemented.
 */
public abstract class Check {
	private static final Logger LOGGER = Logger.getLogger(Check.class.getName());

	/**
	 * The number of characters to read at once.
	 */
	private static final int READ_BUFFER_SIZE = 1024;

	/**
	 * The unique identification for each check type.
	 */
	protected String checkId;

	/**
	 * The message that should be appear in the main report.
	 */
	protected String message;

	/**
	 * The resource name (class name) on which the check was performed.
	 */
	protected String resource;

	/**
	 * The default severity type associated to each check type.
	 */
	protected SeverityType severity;

	/**
	 * The file on which the check was performed.
	 */
	protected File file;

	/**
	 * The lines existing in the file in which this check is being performed.
	 */
	protected String[] lines;
	
	/**
	 * The files on which the check was performed.
	 */
	protected List<String> listFileNames = new ArrayList<String>();
	
	/**
	 * The file extension on which the check was performed.
	 */
	private static final String FILE_EXTENSION = "java";

	/**
	 * Constructor.
	 * 
	 * @param checkId
	 *            The identification of the check.
	 * @param message
	 *            The message that should be appear in the main report.
	 * @param severity
	 *            The default severity type associated to check.
	 */
	protected Check(String checkId, String message, SeverityType severity) {
		this.checkId = checkId;
		this.message = message;
		this.severity = severity;
	}

	public String getCheckId() {
		return checkId;
	}

	public SeverityType getSeverity() {
		return severity;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * This method is implemented for each concrete check in order to detect the
	 * existing violations based on the rules defined.
	 * 
	 * @param violations
	 *            A violation auxiliary structure to be updated for each
	 *            concrete check if a validation is detected.
	 */
	public abstract void process(Map<String, Violation> violations);

	/**
	 * This method is used to obtain, as an array structure, the lines existing
	 * within a file.
	 * 
	 * @return The lines existing within a file as an array structure.
	 */
	public String[] getFileLines() {
		final List<String> textLines = new ArrayList<>();
		try {
			String fileContents = readFile();

			if (Strings.isNullOrEmpty(fileContents)) {
				return null;
			}

			final BufferedReader reader = new BufferedReader(new StringReader(fileContents));
			while (true) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				textLines.add(line);
			}
		} catch (IOException e) {
			LOGGER.severe(
					String.format("It was possible to read file '%s' due to %s.", file.getName(), e.getMessage()));
		}
		lines = textLines.toArray(new String[textLines.size()]);
		return lines.clone();
	}

	/**
	 * This method is used to read the content of a certain file and transform
	 * it in a single sequence of characters.
	 * 
	 * @return A single sequence of characters representing the contents of a
	 *         certain file.
	 * @throws IOException
	 *             An exception thrown while the content's file is being
	 *             processed.
	 */
	private String readFile() throws IOException {
		if (file == null) {
			return null;
		}

		final StringBuilder buf = new StringBuilder();
		final FileInputStream stream = new FileInputStream(file);
		final Reader reader = new InputStreamReader(stream);

		try {
			final char[] chars = new char[READ_BUFFER_SIZE];
			while (true) {
				final int len = reader.read(chars);
				if (len < 0) {
					break;
				}
				buf.append(chars, 0, len);
			}
		} finally {
			Closeables.close(reader, false);
		}
		return buf.toString();
	}
	
	/**
	 * This method is used to obtain the list of files 
	 * on which the check is to be performed
	 * 
	 * @return The files as a list structure.
	 */
	public List<String> getFilesrecursively(File file) {
		
		File[] list = file.listFiles();
		
		if (list == null) return listFileNames;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	getFilesrecursively( f );
            }
            else {
 
                String fileExtension = (String) Files.getFileExtension(f.getAbsolutePath());
                if (fileExtension.equals(FILE_EXTENSION)){
                	listFileNames.add(f.getAbsolutePath());
                }
            }
        }
		
		return listFileNames;
	
	}
	
}
