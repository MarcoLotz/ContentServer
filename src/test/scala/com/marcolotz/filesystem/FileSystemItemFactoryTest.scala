package com.marcolotz.filesystem
import com.marcolotz.ContentServerTest

/**
  * Created by prometheus on 01/08/2017.
  */
class FileSystemItemFactoryTest extends ContentServerTest{

  // Test normal files
  test("normal file is file")(assert(getFileFromResources("/file1.test").isFile))
  test("normal file is not directory")(assert(!getFileFromResources("/file1.test").isDirectory))
  test("normal file is not playable")(assert(!getFileFromResources("/file1.test").isDirectory))

  // Test Directories
  test("Directory is not file")(assert(!getFileFromResources("/innerDir").isFile))
  test("Directory is directory")(assert(getFileFromResources("/innerDir").isDirectory))
  test("Directory not playable")(assert(!getFileFromResources("/innerDir").isPlayable))

  // Test playable files
  test("stream file is file")(assert(getFileFromResources("/innerDir/file4.mp4").isFile))
  test("stream file is not directory")(
    assert(!getFileFromResources("/innerDir/file4.mp4").isDirectory))
  test("stream file is not playable")(
    assert(getFileFromResources("/innerDir/file4.mp4").isPlayable))
}
