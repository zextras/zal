package org.openzal.zal.db;

import com.zimbra.cs.db.TestMariaDB;
import java.sql.PreparedStatement;

public class ZalTestDatabase
{
  private static String sVolumeDirectory;

  public static void start(String volumeDirectory) throws Exception
  {
    sVolumeDirectory = volumeDirectory;
    com.zimbra.cs.db.DbPool.global(TestMariaDB.start(volumeDirectory));
    resetVolumes();
  }

  public static void clear() throws Exception
  {
    TestMariaDB.clear();
    resetVolumes();
  }

  private static void resetVolumes() throws Exception
  {
    com.zimbra.cs.db.DbPool.DbConnection conn = com.zimbra.cs.db.DbPool.getConnection();
    try
    {
      conn.prepareStatement("UPDATE zimbra.current_volumes SET secondary_message_volume_id = NULL").executeUpdate();
      conn.prepareStatement("DELETE FROM zimbra.volume WHERE id > 2").executeUpdate();
      PreparedStatement insert = conn.prepareStatement(
        "INSERT INTO zimbra.volume (id, type, name, path, file_bits, file_group_bits, mailbox_bits, mailbox_group_bits, compress_blobs, compression_threshold)"
          + " VALUES (3, 2, 'message2', ?, 12, 8, 12, 8, 0, 4096)"
      );
      insert.setString(1, sVolumeDirectory + "/store2");
      insert.executeUpdate();
      conn.prepareStatement("UPDATE zimbra.current_volumes SET secondary_message_volume_id = 3").executeUpdate();
      conn.commit();
    }
    finally
    {
      com.zimbra.cs.db.DbPool.quietClose(conn);
    }
  }
}
