package vw;

import vw.jni.NativeUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A JNI layer for submitting examples to VW and getting predictions back.  It should be noted
 * that at this time VW is NOT thread safe, and therefore neither is the JNI layer.  It should be noted
 * that this was originally written with a bulk interface that was later removed because of benchmarking
 * data found <a href="https://microbenchmarks.appspot.com/runs/817d246a-5f90-478a-bc27-d5912d2ff874#r:scenario.benchmarkSpec.methodName,scenario.benchmarkSpec.parameters.loss,scenario.benchmarkSpec.parameters.mutabilityPolicy,scenario.benchmarkSpec.parameters.nExamples">here</a>.
 */
public class VW implements Closeable {

    // This main method only exists to test the library implementation.  To test it just run
    // java -cp target/vw-jni-*-SNAPSHOT.jar vw.VW
    public static void main(String[] args) {
        new VW("").close();
        new VW("--quiet").close();
    }

    static {
        try {
            NativeUtils.loadOSDependentLibrary("/vw_jni", ".lib");
        }
        catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final AtomicBoolean isOpen;
    private final long nativePointer;

    /**
     * Create a new VW instance that is ready to either create predictions or learn based on examples
     * @param command The same string that is passed to VW, see
     *                <a href="https://github.com/JohnLangford/vowpal_wabbit/wiki/Command-line-arguments">here</a>
     *                for more information
     */
    public VW(String command) {
        isOpen = new AtomicBoolean(true);
        nativePointer = initialize(command);
    }

    /**
     * Runs prediction on <code>example</code> and returns the prediction output.
     *
     * @param example a single vw example string
     * @return A prediction
     */
    public float predict(String example) {
        if (isOpen.get()) {
            return predict(example, false, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Runs learning on <code>example</code> and returns the prediction output.
     *
     * @param example a single vw example string
     * @return A prediction
     */
    public float learn(String example) {
        if (isOpen.get()) {
            return predict(example, true, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Close the VW instance.  This MUST be called in order to free up the native memory.
     * After this is called no future calls to this object are permitted.
     */
    public void close() {
        if (isOpen.getAndSet(false)) {
            closeInstance(nativePointer);
        }
    }

    private native long initialize(String command);
    private native float predict(String example, boolean learn, long nativePointer);
    private native void closeInstance(long nativePointer);
}
