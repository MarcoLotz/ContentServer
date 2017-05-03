package com.marcolotz.configuration

case class ServerConfiguration(mountPath: String,
                               port: Int = 8080,
                               enableUserAuthentication: Boolean = false,
                               username: String = "",
                               password: String = "",
                               showHiddenFiles: Boolean = false,
                               filterExtensions: Boolean = false,
                               preemptiveFileSystemExploration: Boolean = true,
                               filteredoutExtensions: List[String] = List())
