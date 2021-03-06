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

import en_deep.mlprocess.manipulation.DataReader;
import en_deep.mlprocess.manipulation.DataReader.FeatType;
import en_deep.mlprocess.utils.StringUtils;

/**
 * This feature returns the lemma, POS, coarse POS and form of the parent node.
 * @author Ondrej Dusek
 */
public class Head extends ParametrizedFeature {

    public Head(DataReader reader){
        super(reader, FeatType.SYNT);
    }

    @Override
    public String getHeader() {
        return getParametrizedHeader("Parent", DataReader.STRING);
    }

    @Override
    public String generate(int wordNo, int predNo) {
        
        Integer headPos = this.reader.getHead(wordNo);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.attrPos.length; ++i){
            if (i > 0){
                sb.append(",");
            }
            if (headPos == 0){
                sb.append("\"\"");
            }
            else {
                sb.append(StringUtils.protect(this.reader.getWordInfo(headPos, this.attrPos[i])));
            }
        }

        return sb.toString();
    }

}
