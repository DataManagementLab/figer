assemblyMergeStrategy in assembly := {
  case PathList("org", "w3c", xs @ _*) =>
    MergeStrategy.first
  case x =>
    // For all the other files, use the default sbt-assembly merge strategy
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("edu.washington.cs.figer.FigerSystem")

