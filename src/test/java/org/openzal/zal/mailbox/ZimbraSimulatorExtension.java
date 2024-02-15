package org.openzal.zal.mailbox;

import org.openzal.zal.extension.Zimbra;

public interface ZimbraSimulatorExtension {

  void setup(Zimbra mZimbra, Zimbra zimbra);
  void cleanup(Zimbra zimbra);

}
