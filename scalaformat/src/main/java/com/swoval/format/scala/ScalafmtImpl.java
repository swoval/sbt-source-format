package com.swoval.format.scala;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.function.Consumer;
import org.scalafmt.interfaces.Scalafmt;
import org.scalafmt.interfaces.ScalafmtReporter;
import scala.Function3;

@SuppressWarnings("unused")
public interface ScalafmtImpl {
  static Function3<Path, Path, Consumer<String>, String> get() {
    return (Path c, Path p, Consumer<String> l) -> {
      try {
        return global.withReporter(new Reporter(l)).format(c, p, new String(Files.readAllBytes(p)));
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  Scalafmt global = Scalafmt.create(ScalafmtImpl.class.getClassLoader());

  class Reporter implements ScalafmtReporter {
    private final Consumer<String> logger;
    Reporter(final Consumer<String> logger) {
      this.logger = logger;
      }
    @Override
    public void error(final Path file, final String message) throws IllegalStateException {
      throw new IllegalStateException(file + " couldn't be formatted with scalafmt: " + message);
    }

    @Override
    public void error(final Path file, final Throwable e) throws IllegalStateException {
      throw new IllegalStateException(file + " couldn't be formatted with scalafmt", e);
    }

    @Override
    public void excluded(Path file) {}

    @Override
    public void parsedConfig(Path config, String scalafmtVersion) {}

    @Deprecated
    @Override
    public PrintWriter downloadWriter() {
      return new PrintWriter(System.out);
    }

    @Override
    public OutputStreamWriter downloadOutputStreamWriter() {

      return new OutputStreamWriter(
          new PrintStream(
              new BufferedOutputStream(
                  new OutputStream() {
                    final Vector<Byte> buffer = new java.util.Vector<>();

                    @Override
                    public void write(final int b) {
                      if (b == 10 || b == 13) {
                        final byte[] result = new byte[buffer.size()];
                        for (int i = 0; i < buffer.size(); ++i) result[i] = buffer.get(i);
                        logger.accept(new String(result));
                        buffer.clear();
                      } else {
                        buffer.add((byte) (b & 0xFF));
                      }
                    }
                  })));
    }
  }
}
