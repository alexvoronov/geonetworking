name              := "geonetworking"

version           := "0.1.0-SNAPSHOT"

organization      := "net.gcdc"

mainClass         := Some("net.gcdc.geonetworking.GnBtpRunner")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"


// Time
libraryDependencies += "org.threeten" % "threetenbp" % "1.2"

// Junit & hamcrest (matchers)
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test

libraryDependencies += "junit" % "junit" % "4.12" % Test

libraryDependencies += "org.hamcrest" % "hamcrest-all" % "1.3" % Test

libraryDependencies += "org.hamcrest" % "hamcrest-core" % "1.3" % Test

libraryDependencies += "org.hamcrest" % "hamcrest-library" % "1.3" % Test

libraryDependencies += "org.hamcrest" % "hamcrest-integration" % "1.3" % Test

libraryDependencies += "org.hamcrest" % "hamcrest-integration" % "1.3" % Test

// Json 
libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "commons-net" % "commons-net" % "3.3"

libraryDependencies += "nl.jqno.equalsverifier" % "equalsverifier" % "1.7.2"


publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

// disable publishing the main API jar
publishArtifact in (Compile, packageDoc) := false



