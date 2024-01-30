package org.openzal.zal.exceptions;

public class ServiceException extends Exception {

  private final Throwable serviceException;

  public ServiceException(Throwable serviceException) {
    this.serviceException = serviceException;
  }

  public Throwable getServiceException() {
    return serviceException;
  }
}
