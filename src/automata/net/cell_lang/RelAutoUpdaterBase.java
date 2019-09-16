package net.cell_lang;


class RelAutoUpdaterBase {
  public Exception lastException;

  RuntimeException getLastRuntimeException() {
    if (lastException == null)
      return null;
    else if (lastException instanceof RuntimeException)
      return (RuntimeException) lastException;
    else
      return new RuntimeException(lastException);
  }
}