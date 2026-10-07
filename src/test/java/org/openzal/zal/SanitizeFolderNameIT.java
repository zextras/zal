package org.openzal.zal;

import org.openzal.zal.exceptions.NoSuchItemException;
import org.openzal.zal.exceptions.UnableToSanitizeFolderNameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class SanitizeFolderNameIT
{
  private Mailbox          mailbox;
  private OperationContext zcontext;
  private final String DEFAULT_FOLDER_NAME = "New Folder";

  @BeforeEach
  public void setUp() {
    NoSuchItemException noShuchFolderException = mock(NoSuchItemException.class);
    mailbox = mock(Mailbox.class);
    when(mailbox.getFolderByName(any(OperationContext.class),
                                 anyString(),
                                 anyInt())).thenThrow(noShuchFolderException);
    zcontext = mock(OperationContext.class);
  }

  @Test
  public void callOriginalName_withExampleName_returnSameName() {
    String name = "Example";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.getOriginalName(), name);
  }

  @Test
  public void sanitize_simpleName_returnTheSame() {
    String name = "SimpleName";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), name);
  }

  @Test
  public void sanitize_simpleNameWithSpaces_returnTheSame() {
    String name = "Simple Folder Name";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), name);
  }

  @Test
  public void sanitize_simpleNameWithTrailingSpaces_returnNameWithoutTrailingSpaces() {
    String name = "  Simple Folder Name  ";
    String nameExpected = "Simple Folder Name";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), nameExpected);
  }

  @Test
  public void sanitize_nameWithCtrlChars_returnNameWithoutCtrlChars() {
    String name = "Simple\tFolder Name\n";
    String nameExpected = "SimpleFolder Name";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), nameExpected);
  }

  @Test
  public void sanitize_nameWithCtrlCharsAndInvalidChars_returnNameWithoutCtrlCharsAndInvalidChars() {
    String name = "Si:mple\tFo/lder N\"ame\n";
    String nameExpected = "SimpleFolder Name";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), nameExpected);
  }

  @Test
  public void sanitize_nameWithSpaceCtrlCharsAndDots_returnNewFolder() {
    String name = "  \t..\n";
    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);
    assertEquals(sfn.sanitizeName(zcontext), DEFAULT_FOLDER_NAME);
  }

  @Test
  public void sanitize_nameOfAnExistingFolder_returnNextAvailableFolderName() {
    String name = "Folder";
    Folder existingFolder = mock(Folder.class);
    Mailbox mailbox = mock(Mailbox.class);

    when(mailbox.getFolderByName(any(OperationContext.class),
                                         eq(name),
                                         anyInt())).thenReturn(existingFolder);

    when(mailbox.getFolderByName(any(OperationContext.class),
                                         eq("Folder 1"),
                                         anyInt())).thenThrow(NoSuchItemException.class);

    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, name, 0);

    assertEquals(sfn.sanitizeName(zcontext), "Folder 1");

    verify(mailbox, times(1)).getFolderByName(any(OperationContext.class), eq("Folder"), anyInt());
    verify(mailbox, times(1)).getFolderByName(any(OperationContext.class), eq("Folder 1"), anyInt());
  }

  @Test
  public void sanitize_nameOfAnExistingFolder_throwsExceptionOnOverflow() {
    Folder existingFolder = mock(Folder.class);
    Mailbox mailbox = mock(Mailbox.class);

    when(mailbox.getFolderByName(any(OperationContext.class),
                                 anyString(),
                                 anyInt())).thenReturn(existingFolder);


    SanitizeFolderName sfn = new SanitizeFolderName(mailbox, "Folder", 0);

    try {
      String name = sfn.sanitizeName(zcontext);
      fail();
    } catch (UnableToSanitizeFolderNameException ignored) {}

    verify(mailbox, times(1000)).getFolderByName(any(OperationContext.class), anyString(), anyInt());
  }

}
