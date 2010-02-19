package en_deep.mlprocess;

import en_deep.mlprocess.exception.DataException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This creates the process plan from the input scenario XML file. It parses the XML file and
 * determines the dependencies between the tasks. The output is a {@link Vector} of {@link TaskSection}s
 * that have the correct dependencies set. All the {@link Task}s corresponding to the {@link TaskSection}
 * items are marked as undone.
 */
class ScenarioParser {

    /* DATA */

    /** The ready process plan */
    private Vector<TaskDescription> tasks;
    /** All occurrences of files */
    private Hashtable<String,Occurrences> fileOccurrences;
    
    /** The name of the processed file */
    private String fileName;
    /** The current line */
    private int line;
    /** The open input file */
    private RandomAccessFile input = null;
    /** The size of the open input file */
    private long fileSize = -1;
    /** Some left read data to be parsed with the next section */
    private StringBuffer readData;



    /* METHODS */

    /**
     * Parser initialization, just sets the file name and initializes data
     * containers.
     * @param fileName the name of the parsed file
     */
    ScenarioParser(String fileName) {

        this.fileName = fileName;
        this.tasks = new Vector<TaskDescription>();
        this.fileOccurrences = new Hashtable<String, Occurrences>();
        this.readData = new StringBuffer();
    }



    /**
     * Performs the actual parsing of the input file. Reads all the Task descriptions and stores
     * them for later retrieval. Marks all file occurrences, too.
     *
     * @throws DataException if there is a syntax error in the input file
     */
    void parse() throws DataException {
    
        String taskSection;
        
        // get all task descriptions and sort them out
        while ((taskSection = this.getNextSection()) != null){

            TaskDescription task = null;
            String taskName = null;
            String taskAlgorithm = null;
            Vector<String> taskInput = null;
            Vector<String> taskOutput = null;
            Hashtable<String, String> taskParameters = null;

            // split into clauses: task name, input, output, algorithm, parameters, end.
            String [] clauses = taskSection.split(";", 6);
            for (int i = 0; i < clauses.length; ++i){
                clauses[i] = clauses[i].trim();
            }
            // check for correct section ending
            if (!clauses[clauses.length -1].equals("end")){
                throw new DataException(DataException.ERR_END_EXPECTED, this.fileName, this.line);
            }
            // check for correct section beginning
            if (!clauses[0].startsWith("task ")){
                throw new DataException(DataException.ERR_TASK_EXPECTED, this.fileName, this.line);
            }
            taskName = clauses[0].split("\\s+", 2)[1]; // set the task name
            
            // handle all the section contents
            for (int i = 1; i < clauses.length - 1; ++i){

                String [] clause = clauses[i].split("\\s+", 2);

                if (clause[0].equals("in:")){
                    if (taskInput != null){
                        throw new DataException(DataException.ERR_DUPLICATE_CLAUSE, this.fileName, this.line);
                    }
                    taskInput = this.getFileList(clause[1]);
                }
                else if (clause[0].equals("out:")){
                    if (taskOutput != null){
                        throw new DataException(DataException.ERR_DUPLICATE_CLAUSE, this.fileName, this.line);
                    }
                    taskOutput = this.getFileList(clause[1]);
                }
                else if (clause[0].equals("algorithm:")){
                    if (taskAlgorithm != null){
                        throw new DataException(DataException.ERR_DUPLICATE_CLAUSE, this.fileName, this.line);
                    }
                    taskAlgorithm = clause[1];
                }
                else if (clause[0].equals("params:")){
                    if (taskParameters != null){
                        throw new DataException(DataException.ERR_DUPLICATE_CLAUSE, this.fileName, this.line);
                    }
                    taskParameters = this.getParameters(clause[1]);
                }
                else {
                    throw new DataException(DataException.ERR_UNKNOWN_CLAUSE, this.fileName, this.line);
                }
            }

            // check if the task description is complete
            if (taskInput == null || taskOutput == null || taskParameters  == null || taskAlgorithm == null){
                throw new DataException(DataException.ERR_MISSING_CLAUSE, this.fileName, this.line);
            }

            // build the actual task
            task = new TaskDescription(taskName, taskAlgorithm, taskParameters, taskInput, taskOutput);

            // mark all file occurences
            this.markFileUsages(task);

            // store the task
            this.tasks.add(task);
        }
    }

    /**
     * Marks all the given data sources of the current Task to the {@link Occurrences} table.
     *
     * @param task the task to be recorded
     * @throws DataException if there are multiple tasks that output the same data source
     */
    private void markFileUsages(TaskDescription task) throws DataException {

        for (String file : task.getInput()){
            this.markFileUsage(file, task, Occurrences.Purpose.INPUT);
        }
        for (String file: task.getOutput()){
            this.markFileUsage(file, task, Occurrences.Purpose.OUTPUT);
        }
    }

    /**
     * Marks one usage of a file, given the task it's used in and a purpose it's used for.
     *
     * @param file the file name
     * @param task the task the file is used in
     * @param purpose the purpose of the file (input / output)
     * @throws DataException
     */
    private void markFileUsage(String file, TaskDescription task, Occurrences.Purpose purpose) throws DataException {

        Occurrences oc = this.fileOccurrences.get(file);
        if (oc == null) {
            oc = new Occurrences();
            this.fileOccurrences.put(file, oc);
        }
        oc.add(task, purpose, this.fileName, this.line);
    }

    /**
     * Parse a list of files within a clause.
     * @param clause the clause string to be parsed
     * @return a list of file names
     * @throws DataException if there are invalid characters in the file names
     */
    private Vector<String> getFileList(String clause) throws DataException {

        Vector<String> list = this.parseCSV(clause); // raw parsing

        // remove quotes & spaces
        for(int i = 0; i < list.size(); ++i){

            String s = list.elementAt(i).trim();

            if (s.startsWith("\"") && s.endsWith("\"")){
                s = this.unquote(s);
            }
            else if (s.contains("\"") || s.matches("\\s")){
                throw new DataException(DataException.ERR_INVALID_CHAR_IN_FILE_NAME, this.fileName, this.line);
            }
            list.setElementAt(s, i);
        }
        
        // return the result
        return list;
    }

    /**
     * Unquotes a quoted string, i.e\. removes quotes from the beginning and the end and
     * converts double quotes to single. Throws an exception for a single quote within the string.
     * @param string the quoted string to be processed
     * @return the string, unquoted
     */
    private String unquote(String string) throws DataException {

        string = string.substring(1, string.length()-1);
        if (string.matches("[^\"]\"[^\"]")){
            throw new DataException(DataException.ERR_INVALID_CHAR_IN_FILE_NAME, this.fileName, this.line);
        }
        return string.replaceAll("\"\"", "\"");
    }

    /**
     * Split a comma-separated string that may contain quotes, ignore quoted commas and
     * heed the unquoted ones.
     * @param string the string to be split
     * @return the list of values that were separated by unquoted commas
     */
    private Vector<String> parseCSV(String string) {

        Vector<String> list = new Vector<String>();
        boolean quoted = false;
        int st = 0;

        for (int cur = 0; cur < string.length(); ++cur){
            if (string.charAt(cur) == '"'){
                quoted = !quoted;
            }
            else if (string.charAt(cur) == ',' && !quoted){
                list.add(string.substring(st, cur));
                st = cur + 1;
            }
        }
        if (st < string.length() - 1){
            list.add(string.substring(st));
        }
        return list;
    }

    /**
     * Parse a string that contains comma-separated name = value pairs, values
     * may be enclosed in quotes. Parameter names must be unique.
     *
     * @param string the string to be parsed
     * @return the resulting name - value list
     * @throws DataException if there are duplicate parameters or illegal characters
     */
    private Hashtable<String, String> getParameters(String string) throws DataException {

        Hashtable<String, String> parameters = new Hashtable<String, String>();

        // remove quotes & spaces, split names and values
        for(String listMember : this.parseCSV(string)){

            String [] nameVal = listMember.split("=", 2);

            nameVal[0].trim();
            nameVal[1].trim();

            if (nameVal[0].matches("[^a-zA-Z0-9_\\.-]")){
                throw new DataException(DataException.ERR_INVALID_CHAR_IN_PARAMETER, this.fileName, this.line);
            }
            if (nameVal[1].startsWith("\"") && nameVal[1].endsWith("\"")){
                nameVal[1] = this.unquote(nameVal[1]);
            }
            else if (nameVal[1].contains("\"") || nameVal[1].matches("\\s")){
                throw new DataException(DataException.ERR_INVALID_CHAR_IN_PARAMETER, this.fileName, this.line);
            }

            parameters.put(nameVal[0], nameVal[1]);
        }

        // return the result
        return parameters;
    }

    /**
     * Parses the input file with respect to the individual task descriptions. Changes new-line characters
     * to spaces.
     * @return the next task description section
     */
    private String getNextSection() throws IOException {

        // open the file, if it's not already open
        if (this.input == null){
            this.input = new RandomAccessFile(this.fileName, "r");
            this.fileSize = this.input.length();
        }

        // TODO write getNextSection, using readClause !
    }

    /**
     * Reads one semicolon-separated clause line from the input file. Finds an unquoted semicolon and reads
     * everything up to it, then makes a string out of it. Counts lines.
     * @return the next line from the open file
     */
    private String readClause() throws IOException, DataException {

        long pos1 = this.input.getFilePointer();
        long curPos; // we need to prevent from reaching the end-of-file, for the
                     // damn thing would freeze otherwise
        long pos2;
        int c = this.input.read();
        int lastC = -1;
        byte [] rawLine;
        boolean quoted = false;

        curPos = this.input.getFilePointer();
        if (curPos == this.fileSize){ // EOF check
            return null;
        }

        while(curPos < this.fileSize && (c != ';' || quoted)){

            // line counting: ignore two subsequent windows new-line characters
            if (c == '\n' && lastC == '\r'){
                
            }
            // line counting: count all else
            else if (c == '\n' || c == '\r'){
                this.line++;
            }
            // heed the quote characters
            else if (c == '"'){
                quoted = !quoted;
            }
            c = this.input.read();
            curPos++;
            lastC = c;
        }
        pos2 = this.input.getFilePointer();

        if (curPos == this.fileSize && quoted){
            throw new DataException(DataException.ERR_UNEXPECTED_EOF, this.fileName, this.line);
        }

        // since we know if we're at the end of the clause, we don't need curPos anymore
        rawLine = new byte [(int)(pos2 - pos1)];
        this.input.seek(pos1);
        this.input.read(rawLine);

        return (new String(rawLine)).trim();
    }




}
