package com.marcolotz.configuration

case class ServerConfiguration(mountPath: String,
                               port: Int,
                               username: String,
                               password: String,
                               showHiddenFiles: Boolean)
