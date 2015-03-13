package org.obicere.cc.util.protocol;

import org.obicere.cc.executor.Case;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Used to package appropriate data for a remote method invocation across
 * processes. This is done through a socket connection - the establishment
 * of the socket should already be in place. This does follow a very strict
 * format, with magic numbers used to denote data information. Corrupt
 * streams could happen, but the way this is handled is dependent on the
 * implementations.
 * <p>
 * Since this is just invoking, it assumes the stream will remain open.
 * Subsequent information must then be transmitted accordingly back, but
 * the validation of that information is not handled here, but rather in
 * the calling location.
 * <p>
 * The usage of a magic number is to denote the start of a call. The first
 * denotation will contain the information regarding what to execute and
 * how. The second, third, and subsequent calls will all be case
 * information for what the method should run on. Furthermore, the usage of
 * two unique magic numbers is being used. One signifying the starting and
 * the other signifying the calling. Each magic number is a predefined
 * constant that has a very special meaning. Any <i>true</i> programmer
 * would understand it. Each magic number is precisely <code>4</code> bytes
 * in length.
 * <p>
 * The first step for the invocation will be passing the information to
 * locate the method. We can assume we won't have to deal with return types
 * or parameters not supported by the {@link org.obicere.cc.util.protocol.PrimitiveProtocol}
 * and therefore the application. We can also assume there is no clauses
 * for exceptions, such as the Java <code>throws</code> keyword does.
 * <p>
 * Therefore the first set of information will limit the scope - hopefully
 * in decreasing effectiveness - until a match has been found. This will
 * start with the class name. Although other languages don't specifically
 * use that term, it is the accessible-compiled-executable file's name.
 * <p>
 * The second term is the method name. The third type is the return type.
 * From there, hopefully the method can be found. Ideally we are dealing
 * with the user-files where 1, maybe 2, methods have been created. In case
 * not, hold onto your hats because more is coming anyway.
 * <p>
 * The last set of information is the parameter types. This includes the
 * identifiers for each parameter and the dimension of the array. This is
 * to say that for the following examples:
 * <p>
 * <code>
 * <pre>
 * int:         id = 07     dimension = 0
 * int[]:       id = 07     dimension = 1
 * int[][]:     id = 07     dimension = 2
 * ...
 * String:      id = 0F     dimension = 0
 * String[]:    id = 0F     dimension = 1
 * String[][]:  id = 0F     dimension = 2
 * </pre>
 * </code>
 * <p>
 * We can assume that if you made the runner write (or I did for that
 * matter) that the lowest-component type for each parameter is a supported
 * type. Therefore the next dimension's up component type should have the
 * same dimension as the lower-dimension's component type.
 * <p>
 * Once all this information is passed, you wait until further instruction
 * is provided, in the form of cases. This will firstly be denoted by a
 * magic number, which is very meaningful.
 * <p>
 * Subsequently, the number of cases will follow - let this be
 * <code>n</code>. Then, there will be <code>n</code> arrays that follow.
 * Each containing the direct parameters for the call.
 * <p>
 * So for example, parameters:
 * <p>
 * <code>int[]{1, 5, 6}, "foo"</code>
 * <p>
 * Will be packed like so:
 * <p>
 * <code>new Object[]{new int[]{1, 5, 6}, "foo"}</code>
 * <p>
 * Therefore each case should be read with the generic
 * <code>Object[].class</code> type, or the equivalent value in the other
 * language.
 * <p>
 * After all <code>n</code> cases have been exhausted, no more information
 * will be available at the current time. More information may come forward
 * if the invoke is optimized to only pass call information once, but this
 * may not happen. Therefore depending on the magic numbers to guide the
 * instruction set is highly recommended.
 * <p>
 * The protocol for a method invocation is as follows: <code>
 * <pre>
 * int: magic_number
 * String: class_name
 * String: method_name
 *
 * byte: return_type_identifier
 * int: return_type_class_dimension
 *
 * byte: int_identifier
 * int: parameter_count
 *
 * byte: parameter_0_type_identifier
 * int: parameter_0_type_dimension
 * byte: parameter_1_type_identifier
 * int: parameter_1_type_dimension
 * byte: parameter_2_type_identifier
 * int: parameter_2_type_dimension
 * ...
 * byte: parameter_n_type_identifier
 * int: parameter_n_type_dimension
 *
 * int: magic_number
 *
 * int: case_count
 * byte: array_identifier
 * Object[]: case_0_parameters
 * byte: array_identifier
 * Object[]: case_1_parameters
 * byte: array_identifier
 * Object[]: case_2_parameters
 * ...
 * byte: array_identifier
 * Object[]: case_m_parameters
 * </pre>
 * </code>
 * <p>
 * For example, invoking a method named <code>foo</code> in a class
 * <code>Bar</code> that takes the parameters: <code>(int[], String)</code>
 * and returns of type <code>char</code>.
 * <p>
 * If we wish to also invoke <code>foo</code> with <code>2</code> cases:
 * <code>
 * <pre>
 * {1, 2, 5}, "a"
 * {5, 6, 0}, "de"
 * </pre>
 * </code>
 * <p>
 * The protocol invocation will then look like: <code>
 * <pre>
 * A9 4B F9 4D  // magic number denoting start
 * 0F           // String identifier
 * 00 00 00 03  // class name length
 * 00 42        // B
 * 00 61        // a
 * 00 72        // r
 * 0F           // String identifier
 * 00 00 00 03  // method name length
 * 00 66        // f
 * 00 6F        // o
 * 00 6F        // o
 *
 * 06           // char identifier
 * 00 00 00 00  // return type dimension
 * 07           // int identifier
 * 00 00 00 02  // number of parameters
 * 07           // int parameter
 * 00 00 00 01  // int parameter dimension
 * 0F           // string parameter
 * 00 00 00 00  // string parameter dimension
 *
 * 9B AA B2 A9  // Magic number denoting case call
 *
 * 00 00 00 02  // number of cases
 *
 * 10           // array identifier]
 * 00 00 00 02  // case 0 length
 *
 * 10           // array identifier
 * 00 00 00 03  // array length
 * 07           // int identifier
 * 00 00 00 01  // case 0, parameter 0, value 1
 * 07           // int identifier
 * 00 00 00 02  // case 0, parameter 0, value 2
 * 07           // int identifier
 * 00 00 00 05  // case 0, parameter 0, value 5
 *
 * 0F           // String identifier
 * 00 00 00 01  // String length
 * 00 61        // 'a'
 *
 * 10           // array identifier]
 * 00 00 00 02  // case 1 length
 *
 * 10           // array identifier
 * 00 00 00 03  // array length
 * 07           // int identifier
 * 00 00 00 05  // case 1, parameter 0, value 5
 * 07           // int identifier
 * 00 00 00 06  // case 1, parameter 0, value 6
 * 07           // int identifier
 * 00 00 00 00  // case 1, parameter 0, value 0
 *
 * 0F           // String identifier
 * 00 00 00 02  // String length
 * 00 64        // 'd'
 * 00 65        // 'e'
 * </pre>
 * </code>
 * <p>
 * Good luck.
 *
 * @author Obicere
 * @see org.obicere.cc.util.protocol.PrimitiveSocketProtocol
 * @see org.obicere.cc.util.protocol.MethodInvocationProtocol#MAGIC_NUMBER_START
 * @see org.obicere.cc.util.protocol.MethodInvocationProtocol#MAGIC_NUMBER_CALL
 */
public class MethodInvocationProtocol extends PrimitiveSocketProtocol {

    /**
     * The magic number denoting the start of the method invocation
     * information. Following will be the class name, method name, method
     * return type and the method parameters.
     * <p>
     * Upon reading this magic number, one can assume the start of a new
     * invocation has been started.
     */

    private static final int MAGIC_NUMBER_START = 0xA94BF94D; // Can you solve the mystery?

    /**
     * The magic number denoting the start of the method call procedure.
     * Following will be the number of cases polled, followed by each of
     * the cases.
     * <p>
     * Upon reading this magic number, one can assume the start of a new
     * call procedure has been started.
     */

    private static final int MAGIC_NUMBER_CALL = 0x9BAAB2A9; // Can you solve the mystery?

    /**
     * Constructs a new method invocation protocol scheme on the given
     * socket, with optional flushing. The socket should already be
     * established and open for this to work properly. Otherwise exceptions
     * will occur.
     *
     * @param socket    The socket to transmit the data through.
     * @param autoFlush Whether or not to automatically flush data, or to
     *                  wait for the user's invocation to flush.
     * @throws IOException If the socket connection was not established,
     *                     closed, or numerous other possibilities.
     */

    public MethodInvocationProtocol(final Socket socket, final boolean autoFlush) throws IOException {
        super(socket, autoFlush);
    }

    /**
     * Constructs a new method invocation protocol scheme on the given
     * socket. The socket should already be established and open for this
     * to work properly. This instance will not automatically stream, so
     * calling the flush manually is required.
     *
     * @param socket The socket to transmit the data through.
     * @throws IOException If the socket connection was not established,
     *                     closed, or numerous other possibilities.
     */

    public MethodInvocationProtocol(final Socket socket) throws IOException {
        super(socket);
    }

    /**
     * Writes the starting information to the stream to signify the
     * following method should be found. This should contain all the
     * information required to locate the stream. Other information can be
     * packed of course. However it is suggested to transmit this data
     * first, since the usage of a magic-number identifier will help with
     * streaming. The protocol itself follows the format: <code>
     * <pre>
     * int: magic_number
     * String: class_name
     * String: method_name
     *
     * byte: return_type_identifier
     * int: return_type_class_dimension
     *
     * byte: int_identifier
     * int: parameter_count
     *
     * byte: parameter_0_type_identifier
     * int: parameter_0_type_dimension
     * byte: parameter_1_type_identifier
     * int: parameter_1_type_dimension
     * byte: parameter_2_type_identifier
     * int: parameter_2_type_dimension
     * ...
     * byte: parameter_n_type_identifier
     * int: parameter_n_type_dimension
     * </pre>
     * </code>
     *
     * @param className    The name of the class containing the method.
     * @param methodName   The name of the method to invoke.
     * @param returnClass  The return type of the method.
     * @param paramClasses The classes of each of the parameters.
     * @see org.obicere.cc.util.protocol.MethodInvocationProtocol#MAGIC_NUMBER_START
     */

    public void writeInvocation(final String className, final String methodName, final Class<?> returnClass, final Class<?>... paramClasses) {
        Objects.requireNonNull(className, "Class name cannot be null.");
        Objects.requireNonNull(methodName, "Method name cannot be null.");
        Objects.requireNonNull(returnClass, "Return class type cannot be null.");
        Objects.requireNonNull(paramClasses, "Parameter classes cannot be null.");
        protocol.writeLength(MAGIC_NUMBER_START); // need to use raw write
        protocol.write(className);
        protocol.write(methodName);
        writeClass(returnClass);
        write(paramClasses.length);
        for (final Class<?> param : paramClasses) {
            writeClass(param);
        }
    }

    /**
     * Writes the given class to the stream. This will include the
     * lowest-component type of the class as well as the dimension of the
     * class. The dimension is defined to be the level of array. The
     * specific format is: <code>
     * <pre>
     * byte: class_identifier
     * int: class_dimension
     * </pre>
     * </code>
     *
     * @param cls The class to write to the stream.
     * @see org.obicere.cc.util.protocol.MethodInvocationProtocol#dimension(Class)
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#componentIdentifier(Class)
     */

    private void writeClass(final Class<?> cls) {
        Objects.requireNonNull(cls, "Parameter class cannot be null.");
        final int id = protocol.componentIdentifier(cls);
        if (id == -1) {
            throw new IllegalArgumentException("No valid identifier for class: " + cls + " found.");
        }
        protocol.writeIdentifier(id);
        protocol.writeLength(dimension(cls));
    }

    /**
     * Writes the list of cases to the stream. These will be written as an
     * {@link Object}[] instance, so the respective receiver should use the
     * equivalent class. This will be first denoted by a magic number,
     * signifying the start. At that point it is safe to assume the data is
     * being transmitted and that a call procedure is in order.
     * <p>
     * The number of cases will also be written to the stream, to ensure
     * integrity. Also, each case will packs its own length. This is to
     * also ensure integrity when compared to the given parameter length in
     * the starting procedure. <code>
     * <pre>
     * The specific format is:
     *
     * int: magic_number
     *
     * int: case_count
     * byte: array_identifier
     * Object[]: case_0_parameters
     * byte: array_identifier
     * Object[]: case_1_parameters
     * byte: array_identifier
     * Object[]: case_2_parameters
     * ...
     * byte: array_identifier
     * Object[]: case_m_parameters
     * </pre>
     * </code>
     * <p>
     * Where <code>m</code> is the number of cases.
     *
     * @param cases The cases to write to the stream and invoke with.
     */

    public void writeCases(final Case[] cases) {
        Objects.requireNonNull(cases, "Cases cannot be null.");
        final int length = cases.length;
        protocol.writeLength(MAGIC_NUMBER_CALL); // need to use raw write
        protocol.writeLength(length);
        for (final Case c : cases) {
            Objects.requireNonNull(c, "Null cases cannot be submitted.");
            protocol.write(c.getParameters());
        }
    }

    /**
     * Retrieves the dimension of the given class. The dimension of the
     * class is a recursive definition. So long as the given class has a
     * component type, then get that dimension and add <code>1</code> to
     * it.
     * <p>
     * This works as: <code>
     * <pre>
     * dimension(T)         = 0
     * dimension(T[])       = dimension(T)      + 1 = 0 + 1 = 1
     * dimension(T[][])     = dimension(T[])    + 1 = 1 + 1 = 2
     * dimension(T[][][])   = dimension(T[][])  + 1 = 2 + 1 = 3
     * ...
     * </pre>
     * </code>
     * <p>
     * This dimensions is then used to properly generate the parameter type
     * names and to find the correct method, since for most languages
     * <code>int[]</code> is different from <code>int</code>.
     *
     * @param cls The class to find the dimension of.
     * @return The dimension of the class. By default this is
     * <code>0</code>. Single dimensional arrays have a dimension of
     * <code>1</code> and so on.
     */

    private int dimension(final Class<?> cls) {
        int i = 0;
        Class<?> comp = cls;
        while ((comp = comp.getComponentType()) != null) {
            i++;
        }
        return i;
    }

    // If you're actually trying to solve the mystery of the magic numbers
    // now is a good time to tell you there is none. I used random.org
    // don't trust people on the internet
}
