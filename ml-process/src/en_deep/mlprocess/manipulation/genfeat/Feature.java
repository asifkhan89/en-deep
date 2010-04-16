/*
 *  Copyright (c) 2010 Ondrej Dusek
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

package en_deep.mlprocess.manipulation.genfeat;

import en_deep.mlprocess.manipulation.StToArff;
import en_deep.mlprocess.manipulation.StToArff.StToArffConfig;
import java.lang.reflect.Constructor;
import java.util.Vector;

/**
 * This is the super-class for all generated features, which are used
 * in the {@link StToArff} conversion process.
 * 
 * @author Ondrej Dusek
 */
public abstract class Feature {

    /* CONSTANTS */
    
    protected static final String LF = System.getProperty("line.separator");

    /* DATA */

    /** The used ST-file format setting */
    StToArffConfig config;


    /* METHODS */

    /**
     * Constructor, to be used by subclasses only
     * @param config the language-specific ST-file configuration
     */
    protected Feature(StToArffConfig config){
        this.config = config;
    }

    /**
     * This creates a {@link Feature} object for the given name. Returns null if
     * the process fails.
     *
     * @param name the desired class name (within the {@link en_deep.mlprocess.manipulation.genfeats} package)
     * @return the {@link Feature} object to use with the {@link StToArff} class.
     */
    public static Feature createFeature(String name, StToArffConfig config) {

        Feature res = null;
        Class featureClass = null;
        Constructor featureConstructor = null;

        // retrieve the task class
        try {
            if (!name.contains(".")){
                name = Feature.class.getPackage().getName() + "." + name;
            }
            featureClass = Class.forName(name);
        }
        catch (ClassNotFoundException ex) {
            return null;
        }

        // try to call a constructor with no parameters
        try {
            featureConstructor = featureClass.getConstructor(StToArffConfig.class);
            res = (Feature) featureConstructor.newInstance(config);
        }
        catch(Exception ex){
            return null;
        }

        return res;
    }

    /**
     * Returns the ARFF header for this generated feature. The header is does not end with a new-line
     * character.
     * @return the ARFF-style header (name &amp; type)
     */
    public abstract String getHeader();

    /**
     * The main method -- generates the feature value for the given word
     * in a sentence, relative to the given predicate.
     *
     * @param sentence the whole sentence, where the value is computed
     * @param wordNo the number of the word to which the value applies
     * @param predNo the number of the word which is a predicate and to which the value of the feature is related
     * @return the value of the generated feature, in a string representation
     */
    public abstract String generate(Vector<String []> sentence, int wordNo, int predNo);

}