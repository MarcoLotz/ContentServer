package com.marcolotz.contentserver.configuration

case class ServerConfiguration(mountPath: String = "",
                               tempDirectory: String = "",
                               port: Int = 8080,
                               enableUserAuthentication: Boolean = false,
                               username: String = "",
                               password: String = "",
                               showHiddenFiles: Boolean = false,
                               filteredoutExtensions: List[String] = List())
