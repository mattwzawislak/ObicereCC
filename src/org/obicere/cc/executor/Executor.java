/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.executor;

import org.obicere.cc.executor.compiler.*;
import org.obicere.cc.executor.compiler.Compiler;
import org.obicere.cc.executor.language.Language;

/**
 * Source code compiler and executor. This is used internally during the run of
 * the input code.
 *
 * Lots of magic happens here. Do not touch anything.
 *
 * @author Obicere
 * @since 1.0
 */

public class Executor {

    public static final Compiler JAVA_COMPILER = new JavaCompiler();

    public static Compiler compilerByLanguage(final Language language){
        switch(language.getName()){
            case "Java":
                return JAVA_COMPILER;
            default:
                throw new UnsupportedLanguageException(language.getName());
        }
    }

    public static class UnsupportedLanguageException extends RuntimeException {

        public UnsupportedLanguageException(final String message){
            super(message);
        }

    }

}
