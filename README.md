# JGraLab

JGraLab is a Java graph library implementing so-called TGraphs: typed,
attributed, ordered, and directed graphs.  Beyond the plain data structure, the
library contains an extensive algorithm library, the graph query language
GReQL, the transformation API GReTL, and tons of utilities.

## Installation and Building

### Building from Sources

The `jgralab` project depends on the `common` project.  It is important that
they reside next to each other on your filesystem.  For example, clone both
projects into some base folder `jgsrc` so that the filesystem structure is like
so.

    jgsrc/               # your jgralab workspace
    +-> common/          # the common project
    +-> jgralab/         # this project, i.e., jgralab itself

To build `jgralab` you need to have [Apache Ant](http://ant.apache.org/).
Build `common` first and then `jgralab`.

    $ cd jgsrc/common/
    $ ant
    $ cd ../jgralab/
    $ ant

Both projects contain Eclipse `.project` and `.classpath` files, so that you
can import them in Eclipse as existing projects.

### Downloading a Pre-Compiled Jar

You can download nightly builds from our
[snapshots page](http://userpages.uni-koblenz.de/~ist/jgralab/snapshots/).  The
source jars contain the compiled class files and the source files.  As such,
they are preferrable to the binary-only builds when you intend to develop
applications using JGraLab, because then you get nicer code completion, e.g.,
the real names of method parameters instead of just `arg0`.

## Documentation

### API Documentation

You can find the API documentation for the current JGraLab master branch at our
[API Docs Page](http://userpages.uni-koblenz.de/~ist/jgralab/api/).  This
documentation is regenerated every night.

### Coverage Report

Every night, we run all our test cases against the current master branch and
measure our coverage using
[Atlassian Clover](http://www.atlassian.com/software/clover/overview).  The
results are accessible at our
[Coverage Report Page](http://userpages.uni-koblenz.de/~ist/jgralab/clover-report/).

### Tutorials and Getting Started

Eventually, we are going to create appropriate documentation in the Wiki, so
stay tuned...

### Getting Help

Join us on the official [JGraLab IRC Channel](irc://irc.freenode.net/#jgralab)
(channel `#jgralab` on `irc.freenode.net`).

## License

Copyright (C) 2007-2012 The JGraLab Team <ist@uni-koblenz.de>

Distributed under the General Public License (Version 3), with the following
additional grant:

    Additional permission under GNU GPL version 3 section 7

    If you modify this Program, or any covered work, by linking or combining it
    with Eclipse (or a modified version of that program or an Eclipse plugin),
    containing parts covered by the terms of the Eclipse Public License (EPL),
    the licensors of this Program grant you additional permission to convey the
    resulting work.  Corresponding Source for a non-source form of such a
    combination shall include the source code for the parts of JGraLab used as
    well as that of the covered work.


<!-- Local Variables:        -->
<!-- mode: markdown          -->
<!-- indent-tabs-mode: nil   -->
<!-- End:                    -->
