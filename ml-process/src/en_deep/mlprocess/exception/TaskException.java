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

package en_deep.mlprocess.exception;

import en_deep.mlprocess.Task;

/**
 * The exception class for all run-time task errors.
 * @author Ondrej Dusek
 */
public class TaskException extends GeneralException {

    /* CONSTANTS */

    /** Error message: "Wrong number of outputs." */
    public static final int ERR_WRONG_NUM_OUTPUTS = 1;
    /** Error message: "Wrong number of inputs." */
    public static final int ERR_WRONG_NUM_INPUTS = 2;
    /** Error message: "Task class not found." */
    public static final int ERR_TASK_CLASS_NOT_FOUND = 3;
    /** Error message: "The Task class does not conform to specifications." */
    public static final int ERR_TASK_CLASS_INCORRECT = 4;
    /** Error message: "I/O error during task operation." */
    public static final int ERR_IO_ERROR = 5;
    /** Error message: "Invalid task parameters were specified." */
    public static final int ERR_INVALID_PARAMS = 6;
    /** Error message: "The expansion patterns in output file names are not specified correctly." */
    public static final int ERR_OUTPUT_PATTERNS = 7;
    /** Error code: "Wrong pattern specifications." */
    public static final int ERR_PATTERN_SPECS = 8;
    /** Error code: "Task initialization error." */
    public static final int ERR_TASK_INIT_ERR = 9;
    /** Error code: "Invalid input data." */
    public static final int ERR_INVALID_DATA = 10;
    /** Error code: "No files found for the pattern." */
    public static final int ERR_NO_FILES = 11;
    /** Error code: "Directory doesn't exist or is empty." */
    public static final int ERR_EMPTY_DIR = 12;

    /* DATA */

    /** Id of the task that triggered the exception */
    String taskId;
    /** Additional message */
    String message;

    /* METHODS */

    /**
     * Creates a new {@link Task} exception with the given code, according
     * to the in-class constants.
     *
     * @param taskId  the id of the {@link Task} that triggered the exception
     * @param code the exception code
     */
    public TaskException(int code, String taskId){
        super(code);
        this.taskId = taskId;
        this.message = "";
    }

    /**
     * Creates a new {@link Task} exception with the given code, according
     * to the in-class constants, and an additional error message.
     *
     * @param taskId  the id of the {@link Task} that triggered the exception
     * @param message an additional specifying error message
     * @param code the exception code
     */
    public TaskException(int code, String taskId, String message){
        super(code);
        this.taskId = taskId;
        this.message = message;
    }

    /**
     * Returns the error message according to the error code.
     * @return the appropriate error message
     */
    @Override
    public String getMessage() {

        switch(this.code){
            case ERR_OK:
                return this.taskId + ": No error. " + this.message;
            case ERR_WRONG_NUM_OUTPUTS:
                return this.taskId + ": Wrong number of outputs. " + this.message;
            case ERR_WRONG_NUM_INPUTS:
                return this.taskId + ": Wrong number of inputs. " + this.message;
            case ERR_TASK_CLASS_NOT_FOUND:
                return this.taskId + ": Task class not found. " + this.message;
            case ERR_TASK_CLASS_INCORRECT:
                return this.taskId + ": The Task class does not conform to specifications. " + this.message;
            case ERR_IO_ERROR:
                return this.taskId + ": I/O error during task operation. " + this.message;
            case ERR_INVALID_PARAMS:
                return this.taskId + ": Invalid task parameters were specified. " + this.message;
            case ERR_OUTPUT_PATTERNS:
                return this.taskId + ": The expansion patterns in output file names are not specified correctly. " + this.message;
            case ERR_PATTERN_SPECS:
                return this.taskId + ": Wrong pattern specifications. " + this.message;
            case ERR_TASK_INIT_ERR:
                return this.taskId + ": Task initialization error. " + this.message;
            case ERR_INVALID_DATA:
                return this.taskId + ": Invalid input data. " + this.message;
            case ERR_NO_FILES:
                return this.taskId + ": No files found for the pattern. " + this.message;
            case ERR_EMPTY_DIR:
                return this.taskId + ": Directory doesn't exist or is empty. " + this.message;
            default:
                return "Unknown error. " + this.message;
        }
    }

}
