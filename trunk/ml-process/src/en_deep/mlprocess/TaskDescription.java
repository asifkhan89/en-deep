/*
 *  Copyright (c) 2009 Ondrej Dusek
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this list 
 *  of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this 
 *  list of conditions and the following disclaimer in the documentation and/or other 
 *  materials provided with the distribution.
 *  Neither the name of Ondrej Dusek nor the names of their contributors may be
 *  used to endorse or promote products derived from this software without specific 
 *  prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 *  OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package en_deep.mlprocess;

import en_deep.mlprocess.utils.StringUtils;
import java.io.Serializable;
import java.io.File;
import java.util.Comparator;
import java.util.Vector;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

/**
 * This class describes one {@link Task} within the {@link Plan}, with respect to the
 * algorithm used and its settings, inputs, outputs, dependencies and {@link TaskStatus status}.
 * 
 * @author Ondrej Dusek
 */
public class TaskDescription implements Serializable/*, Comparable<TaskDescription>*/ {
  
    /* CONSTANTS */

    /**
     * The possible progress statuses of a {@link Task}.
     * <ul>
     * <li>WAITING = waiting for another {@link Task}(s) to finish</li>
     * <li>PeNDING = ready to be processed</li>
     * <li>IN_PROGRESS = currently being processed</li>
     * <li>DONE = successfully finished</li>
     * <li>FAILED = finished with an error, this stops the processing of dependant tasks</li>
     * </ul>
     */
    public enum TaskStatus {
        WAITING, PENDING, IN_PROGRESS, DONE, FAILED
    }

    /** A character in the task name that indicates this task has been expanded */
    public static final char EXPANSION_MARK = '#';
    
    /* DATA */

    /** The task global ID */
    private String id;
    /** The current task status */
    private TaskStatus status;

    /** The task algorithm class name */
    private String algorithm;
    /** The task algorithm parameters */
    private Hashtable<String, String> parameters;
    /** The needed input files */
    private Vector<String> input;
    /** The output files generated by this Task */
    private Vector<String> output;

    /** Topological order of the task (-1 if not sorted) */
    private int topolOrder;

    /** All the Tasks that this Task depends on */
    private Vector<TaskDescription> iDependOn;
    /** All the Task that are depending on this one */
    private Vector<TaskDescription> dependOnMe;


    /* METHODS */

    /**
     * Creates a new TaskDescription, given the task id, algorithm, parameters,
     * inputs and outputs. Inputs and outputs specifications must be relative
     * to the {@link Process} working directory.
     *
     * @param id the id of the task
     * @param algorithm the {@link Task} class that will be used to compute the task
     * @param parameters the task settings
     * @param input the task input files
     * @param output the task ouptut files
     */
    public TaskDescription(String id, String algorithm, Hashtable<String, String> parameters,
            Vector<String> input, Vector<String> output){

        this.id = id;
        this.algorithm = algorithm;
        this.parameters = parameters;
        this.input = input;
        this.output = output;
        this.status = TaskStatus.PENDING; // no dependencies, yet
        this.topolOrder = -1; // not yet sorted
    }

    /**
     * Creates a copy of the given task description with a given id suffix.
     * All the members except algorithm are cloned, i.e. not just their references copied. Dependencies are
     * set as in the original task -- on the other side of the dependency as well.
     *
     * @param other the object to be copied
     * @param idSuffix a suffix to be pasted at the end of the original id in order to distinguish the two
     */
    private TaskDescription(TaskDescription other, String idSuffix){

        this.id = other.id + EXPANSION_MARK + idSuffix;
        this.algorithm = other.algorithm;
        this.parameters = (Hashtable<String,String>) other.parameters.clone();
        this.input = (Vector<String>) other.input.clone();
        this.output = (Vector<String>) other.output.clone();
        this.status = other.status;
        this.topolOrder = other.topolOrder;

        if (other.dependOnMe != null){ // set forward dependencies
            for (TaskDescription dep : other.dependOnMe){
                dep.setDependency(this);
            }
        }
        if (other.iDependOn != null){ // set backward dependencies
            for (TaskDescription dep : other.iDependOn){
                this.setDependency(dep);
            }
        }
    }


    /**
     * Sets a dependency for this task (i.e\. marks this {@link TaskDescription} as depending
     * on the parameter). Checks for duplicate dependencies, i.e. a dependency from task A to
     * task B is stored only once, even if it is enforced multiple times. Sets the task status
     * to waiting, if the source is not already done - the source task needs to be processed
     * first.
     *
     * @param source the governing {@link TaskDescription} that must be processed before this one.
     */
    public final void setDependency(TaskDescription source) {

        // if we have a dependency, we need to wait for it to finish (if not already finished)
        if (source.status != TaskStatus.DONE){
            this.status = TaskStatus.WAITING;
        }

        if (this.iDependOn == null){
            this.iDependOn = new Vector<TaskDescription>();
        }
        if (!this.iDependOn.contains(source)){
            this.iDependOn.add(source);
        }

        if (source.dependOnMe == null){
            source.dependOnMe = new Vector<TaskDescription>();
        }
        if (!source.dependOnMe.contains(this)){
            source.dependOnMe.add(this);
        }
    }

    /**
     * Returns the current task progress status.
     * @return the current task status
     */
    public TaskStatus getStatus(){
        return this.status;
    }

    /**
     * Returns the task topological order (zero-based), or -1 if not yet sorted.
     * @return the topological order of the task
     */
    public int getOrder(){
        return this.topolOrder;
    }

    /**
     * Sets the task's topological order (as done in task topological sorting in
     * {@link Plan#sortPlan(Vector)}).
     *
     * @param order the topological order for the task
     */
    public void setOrder(int order){
        this.topolOrder = order;
    }

    /**
     * Returns a list of all directly dependent tasks, or null if there are none. Returns
     * a copy that is not affected by subsequent changes to the dependencies.
     * 
     * @return a list of all tasks depending on this one
     */
    Vector<TaskDescription> getDependent(){

        if (this.dependOnMe == null){
            return null;
        }
        return (Vector<TaskDescription>) this.dependOnMe.clone();
    }


    /**
     * Returns a list of all direct prerequisites, or null if there are none. Returns
     * a copy that is not affected by subsequent changes to the dependencies.
     *
     * @return a list of all tasks this task depends on
     */
    Vector<TaskDescription> getPrerequisites(){
        if (this.iDependOn == null){
            return null;
        }
        return (Vector<TaskDescription>) this.iDependOn.clone();
    }


    /**
     * Returns a list of all - even transitively - dependent tasks, or null if there
     * are none. The resulting list is not checked for duplicities, so one task may
     * result in being added more times.
     * 
     * @return a list of all tasks transitively depending on this one
     */
    LinkedList<TaskDescription> getDependentTransitive(){

        LinkedList<TaskDescription> deps = new LinkedList<TaskDescription>();

        if (this.dependOnMe == null){
            return null;
        }
        deps.addAll(this.dependOnMe);
        for (TaskDescription dep : this.dependOnMe){

            LinkedList<TaskDescription> transDep = dep.getDependentTransitive();
            if (transDep != null){
                deps.addAll(transDep);
            }
        }
        return deps;
    }


    /**
     * Returns true if all the tasks on which this task depends are already topologically
     * sorted, i.e. their {@link topolOrder} is >= 0. If we have no prerequisities, "all of
     * them are sorted".
     *
     * @return the sorting status of the prerequisities tasks
     */
    boolean allPrerequisitiesSorted() {

        if (this.iDependOn == null){ // if there are none, they're all sorted.
            return true;
        }
        for (TaskDescription prerequisity : this.iDependOn){
            if (prerequisity.getOrder() < 0){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the task's algorithm class name.
     * @return the name of the algorithm for this task
     */
    String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns the ID of this task.
     * @return the task's id.
     */
    String getId(){
        return this.id;
    }

    /**
     * Returns the parameters for the algorithm.
     * @return the task algorithm parameters
     */
    Hashtable<String, String> getParameters() {
        return this.parameters;
    }

    /**
     * Returns the needed input files for this Task
     * @return the input files
     */
    Vector<String> getInput() {
        return this.input;
    }


    /**
     * Returns the generated output files for this Task
     * @return the output files
     */
    Vector<String> getOutput() {
        return this.output;
    }


    /**
     * Sets new task status, updating all the statuses of the dependent tasks (if the
     * new status is {@link TaskStatus#DONE}.
     * @param status the new task status
     */
    public void setStatus(TaskStatus status) {

        this.status = status;
        
        // if we're done, update the depending tasks (if there are no other tasks
        // they've been waiting for, set their status to pending)
        if (status == TaskStatus.DONE){
            if (this.dependOnMe == null){
                return;
            }
            for (TaskDescription dependentTask : this.dependOnMe){

                if (dependentTask.status == TaskStatus.WAITING){

                    boolean otherPrerequisityNotDone = false;

                    for (TaskDescription prerequisity : dependentTask.iDependOn){
                        if (prerequisity.status != TaskStatus.DONE){
                            otherPrerequisityNotDone = true;
                            break;
                        }
                    }

                    if (!otherPrerequisityNotDone){
                        dependentTask.status = TaskStatus.PENDING;
                    }
                }
            }
        }
    }


    /**
     * Creates a copy of this task with input file patterns "*" (incl. "||"sub-specifications) expanded for
     * the given string. All the other properties of the original task, including dependencies, are preserved.
     *
     * @param expansion the pattern expansion to be used
     * @return a copy of this task, expanded, or a total copy, if the expansions are not valid
     */
    public TaskDescription expand(String expansion) {

        TaskDescription copy = new TaskDescription(this, expansion);

        for (int i = 0; i < this.input.size(); ++i){

            String pattern = StringUtils.normalizeFilePattern(copy.input.get(i));
            int pos = pattern.indexOf("*");
            
            if (pos != -1 && pos == pattern.lastIndexOf("*")){
                pattern = pattern.substring(0, pos) + expansion
                        + (pattern.length() > pos + 1 ? pattern.substring(pos + 1) : "");
            }
            copy.input.set(i, pattern);
        }

        return copy;
    }


    /**
     * Creates a copy of this task the input file pattern "***" at the given position expanded for
     * the given string. All the other parameters, including dependencies, are preserved.
     * If there's no "***" at the given position in the inputs or the pattern "||"-sub-specification
     * doesn't match, this returns null.
     *
     * @param expansion the pattern expansion to be used
     * @return a copy of this task, expanded, or null if the expansion is not valid.
     */
    public TaskDescription expand(String expansion, int inputNo){

        TaskDescription copy = new TaskDescription(this, expansion);
        String pattern = StringUtils.normalizeFilePattern(copy.input.get(inputNo));
        int pos = pattern.indexOf("***");

        if (pos != -1 && pos == pattern.lastIndexOf("***")){
            pattern = pattern.substring(0, pos) + expansion
                    + (pattern.length() < pos - 1 ? pattern.substring(pos + 1) : "");
        }
        copy.input.set(inputNo, pattern);

        return copy;
    }


    /**
     * Looses dependencies to all tasks whose ids match the given prefix.
     * @param idPrefix the prefix to drop dependency to
     */
    public void looseDeps(String idPrefix) {

        // loose both prefix directions
        this.looseDeps(idPrefix, true);
        this.looseDeps(idPrefix, false);
    }

    /**
     * Looses dependencies in the given direction to all tasks whose ids match
     * the given prefix.
     * @param backwards the direction used in search -- true = backwards, false = forwards
     * @param idPrefix the prefix to drop dependency to
     */
    public void looseDeps(String idPrefix, boolean backwards){

        Vector<TaskDescription> dependList = backwards ? this.iDependOn : this.dependOnMe;

        if (idPrefix == null){ // null id prefix -- remove all dependencies
            idPrefix = "";
        }
        if (dependList != null){

            // remove all wanted dependencies from the list
            for (int i = dependList.size() - 1; i >= 0; --i){
                if (dependList.get(i).getId().startsWith(idPrefix)){

                    TaskDescription dep = dependList.remove(i);
                    if (backwards){
                        dep.dependOnMe.remove(this);
                    }
                    else {
                        dep.iDependOn.remove(this);
                    }
                }
            }
            if (dependList.isEmpty()){ // make the list null if it's empty
                if (backwards){
                    this.iDependOn = null;
                }
                else {
                    this.dependOnMe = null;
                }
            }
        }
    }


    /**
     * Unbinds the task from all dependencies completely.
     */
    public void looseAllDeps(){
        this.looseDeps(null);
    }

    /**
     * Removes the dependency of this task on the given other task. The other task must depend on this task,
     * otherwise this has no effect
     * @param other the task that should no longer depend on this one
     */
    public void removeDependency(TaskDescription other) {

        if (this.dependOnMe != null){
            this.dependOnMe.remove(other);
        }
        if (other.iDependOn != null){
            other.iDependOn.remove(this);
        }
    }

    @Override
    public String toString() {

        StringBuilder iDO = new StringBuilder();
        StringBuilder dOM = new StringBuilder();

        if (this.iDependOn != null){
            for (TaskDescription td : this.iDependOn){
                iDO.append(td.id).append(" ");
            }
        }
        if (this.dependOnMe != null){
            for (TaskDescription td : this.dependOnMe){
                dOM.append(td.id).append(" ");
            }
        }

        return this.id + ": " + this.status + "\n"
                + "\talgorithm: " + this.algorithm.toString()
                + "\n\tparams: " + this.parameters.toString()
                + "\n\tiDependOn: " + iDO.toString() + "\n\tdependOnMe: " + dOM.toString()
                + "\n\tinput: " + this.input.toString() + "\n\toutput:" + this.output.toString() + "\n";
    }


    /**
     * If this task description is an expansion of an original tasks, returns the actual pattern replacement
     * in inputs and outputs of the original task.
     *
     * @return the replacement of patterns in inputs and outputs of the original task, or <tt>null</tt> if not applicable
     */
    String getPatternReplacement() {

        if (this.id.indexOf('#') == -1){
            return null;
        }
        return this.id.substring(this.id.indexOf('#') + 1).replaceAll("#", "_");
    }

    /**
     * Replaces the pos-th input with a list of replacements.
     *
     * @param pos the position to be affected
     * @param replacements the list of replacements for the original input (pattern) at that position
     */
    void replaceInput(int pos, Vector<String> replacements) {

        this.input.remove(pos);
        this.input.addAll(pos, replacements);
    }

    /**
     * Replaces the pos-th input with the given replacement.
     *
     * @param pos the position to be affected
     * @param replacement the replacement for the given position
     */
    void replaceOutput(int pos, String replacement) {

        this.output.set(pos, replacement);
    }


    /**
     * Resets the current task status to PENDING, if it's already set as IN_PROGRESS, DONE or ERROR.
     * Does nothing if the task status is WAITING or PENDING. Sets all dependent tasks to WAITING.
     */
    public void resetStatus() {

        Stack<TaskDescription> toUpdate;

        if (this.status == TaskStatus.PENDING || this.status == TaskStatus.WAITING){
            return;
        }
        this.status = TaskStatus.PENDING;

        if (this.dependOnMe == null){
            return;
        }
        
        // update all depending tasks
        toUpdate = new Stack<TaskDescription>();
        for (TaskDescription t : this.dependOnMe){
            toUpdate.push(t);
        }

        while (!toUpdate.empty()){
            TaskDescription t = toUpdate.pop();

            t.status = TaskStatus.WAITING;
            if (t.dependOnMe == null){
                continue;
            }
            for (TaskDescription dep : t.dependOnMe){
                toUpdate.push(dep);
            }
        }
    }

    /**
     * This compares two task descriptions. They are assumed to be equal, if they have the same
     * id, same algorithm and its parameters and same inputs and outputs. Dependencies are 
     * not taken into account.
     * 
     * @param obj the object to compare this TaskDescription with
     * @return true if the given object is equal to this one, false otherwise
     */
    @Override
    public boolean equals(Object obj) {

        if (obj.getClass() == this.getClass()){
            TaskDescription other = (TaskDescription) obj;

            return (this.id.equals(other.id) && this.algorithm.equals(other.algorithm)
                    && this.parameters.equals(other.parameters)
                    && this.input.equals(other.input) && this.output.equals(other.output));
        }
        else {
            return false;
        }
    }


    /**
     * Provides a hash code for the TaskDescription, using id, algorithm, parameters,
     * inputs and outputs.
     *
     * @return the hash code for this TaskDescription
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 97 * hash + (this.algorithm != null ? this.algorithm.hashCode() : 0);
        hash = 97 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        hash = 97 * hash + (this.input != null ? this.input.hashCode() : 0);
        hash = 97 * hash + (this.output != null ? this.output.hashCode() : 0);
        return hash;
    }


    /**
     * Tries to search for one pattern within a given field. Only such strings are returned in which
     * the pattern is found only once and is not followed by path separator character(s). The list
     * of indexes is ascending.
     *
     * @param pattern the pattern to search for
     * @param output if true, it searches within output, if false, it searches within input specifications
     * @return list of indexes at which the pattern was found, or null if none such exist
     */
    private Vector<Integer> getPatternPos(String pattern, boolean output) {

        Vector<Integer> ret = null;
        Vector<String> field = output ? this.output : this.input;

        for (int i = 0; i < field.size(); ++i){
            String elem = field.get(i);
            // ensure we don't return ** as * etc. -- TODO check for multiple patterns in one string ?
            if (elem.indexOf(pattern) != -1 && elem.indexOf(pattern) == elem.lastIndexOf(pattern)
                    && (elem.indexOf(File.separator) == -1 || elem.lastIndexOf(File.separator) < elem.lastIndexOf(pattern))){
                if (ret == null){
                    ret = new Vector<Integer>();
                }
                ret.add(i);
            }
        }
        return ret;
    }


    /**
     * This behaves exactly same as {@link findPattern}, but returns only after just one given pattern has been found.
     *
     * @param pattern the pattern to search for
     * @param output if true, it searches within the task input, otherwise, it searches within the output
     * @return true if the given pattern has been found within the given field
     */
    private boolean hasPattern(String pattern, boolean output) {

        Vector<String> field = output ? this.output : this.input;

        for (int i = 0; i < field.size(); ++i){
            String elem = field.get(i);
            // ensure we don't return ** as * etc. -- TODO check for multiple patterns in one string ?
            if (elem.indexOf(pattern) != -1 && elem.indexOf(pattern) == elem.lastIndexOf(pattern)){
                return true;
            }
        }
        return false;
    }


    /**
     * Returns all the positions in the input specification, on which the given pattern is found.
     *
     * @param pattern the pattern to search for, should be "*", "**" or "***"
     * @return the list of all positions on which the pattern is found, or null
     */
    public Vector<Integer> getInputPatternPos(String pattern){
        return this.getPatternPos(pattern, false);
    }


    /**
     * Returns all the positions in the output specification, on which the given pattern is found.
     *
     * @param pattern the pattern to search for, should be "*", "**" or "***"
     * @return the list of all positions on which the pattern is found, or null
     */
    public Vector<Integer> getOutputPatternPos(String pattern){
        return this.getPatternPos(pattern, true);
    }


    /**
     * Returns true, if the TaskDescription has the given pattern in its input specifications.
     *
     * @param pattern the pattern to search for, should be "*", "**" or "***"
     * @return true if the pattern occurs in the input specifications, false otherwise
     */
    public boolean hasInputPattern(String pattern){
        return this.hasPattern(pattern, false);
    }


    /**
     * Returns true, if the TaskDescription has the given pattern in its output specifications.
     *
     * @param pattern the pattern to search for, should be "*", "**" or "***"
     * @return true if the pattern occurs in the output specifications, false otherwise
     */
    public boolean hasOutputPattern(String pattern){
        return this.hasPattern(pattern, true);
    }

    /**
     * This compares the tasks according to their topological order.
     */
    public static class TopologicalComparator implements Comparator<TaskDescription> {

        /**
         * Compares two TaskDescription according to their topological order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object for comparison
         * @return -1 if the first one has lower topological order, 1 for greater and 0 for equal
         */
        public int compare(TaskDescription o1, TaskDescription o2) {

            if (o1.topolOrder < o2.topolOrder){
                return -1;
            }
            else if (o1.topolOrder > o2.topolOrder){
                return 1;
            }
            return 0;
        }
    }

}
