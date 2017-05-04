addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.7.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0" excludeAll(
  ExclusionRule(organization = "com.danieltrinh")))
libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.7"