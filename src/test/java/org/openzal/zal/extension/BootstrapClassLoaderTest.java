// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package org.openzal.zal.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BootstrapClassLoaderTest {

  /** Loaded from a jar by the class loader under test; it has no behaviour of its own. */
  public static class Fixture {}

  @Test
  void definesExtensionClassesFromTheirJarWithoutSigners(@TempDir Path dir) throws Exception {
    String entryName = Fixture.class.getName().replace('.', '/') + ".class";
    Path jar = dir.resolve("extension.jar");
    try (InputStream in = Fixture.class.getClassLoader().getResourceAsStream(entryName);
        OutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
      ((JarOutputStream) out).putNextEntry(new JarEntry(entryName));
      in.transferTo(out);
    }
    URL jarUrl = jar.toUri().toURL();

    BootstrapClassLoader loader =
        new BootstrapClassLoader(new URL[] {jarUrl}, ClassLoader.getPlatformClassLoader(), false);
    Class<?> loaded = loader.loadClass(Fixture.class.getName());

    assertSame(loader, loaded.getClassLoader());
    assertNotSame(Fixture.class, loaded);
    assertEquals(jarUrl, loaded.getProtectionDomain().getCodeSource().getLocation());
    // Non-null signers, even an empty array, mark the class as signed for class
    // data sharing, which then refuses to archive it.
    assertNull(loaded.getSigners());
    assertNull(loaded.getProtectionDomain().getCodeSource().getCodeSigners());
  }
}
