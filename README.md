<object data="http://jgralab.github.io/jgralab/images/jgralab-logo.svg" width="600">
  <img src="http://jgralab.github.io/jgralab/images/jgralab-logo.png" alt="JGraLab Logo" width="600">
</object>

# JGraLab

JGraLab is a Java graph library implementing so-called TGraphs: typed,
attributed, ordered, and directed graphs.  Beyond the plain data structure, the
library contains an extensive algorithm library, the graph query language
GReQL, the transformation API GReTL, and tons of utilities.

Current build status: [![Build Status](https://secure.travis-ci.org/jgralab/jgralab.png)](http://travis-ci.org/jgralab/jgralab)

## Installation and Building

### Building from Sources

The `jgralab` project depends on the `common` project.  It is important that
they reside next to each other on your filesystem.  For example, clone both
projects into some base folder `jgsrc` so that the filesystem structure is like
so.

    jgsrc/               # your jgralab workspace
    +-> jgralab/         # this project, i.e., jgralab itself

To build `jgralab` you need to have [Apache Ant](http://ant.apache.org/).
Build `jgralab`.

    $ cd jgsrc/jgralab/
    $ ant

The `jgralab` project contains an Eclipse `.project` and `.classpath` file, so that you
can import it in Eclipse as existing project.

### Getting Releases via Maven or Leiningen

The current and past JGraLab releases are pushed to
[the JGraLab Clojars site](https://clojars.org/de.uni-koblenz.ist/jgralab) so
that you can fetch them easily as project dependency via the
[Maven](http://maven.apache.org/) or [Leiningen](http://leiningen.org/) build
and project management systems.

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

We've started writing a tutorial in the
[JGraLab Wiki](https://github.com/jgralab/jgralab/wiki).  It's not yet
complete, but we'll keep working on extending it.

### Getting Help

Join us on the official [JGraLab IRC Channel](irc://irc.freenode.net/#jgralab)
(channel `#jgralab` on `irc.freenode.net`).  If you don't have or don't want to
install an IRC client, you can also
[chat directly in your browser](http://webchat.freenode.net/?channels=jgralab).

## Publications

  - _GReTL: an extensible, operational, graph-based transformation language_,
    Jürgen Ebert and Tassilo Horn, In: Software and Systems Modeling, 2012,
    http://dx.doi.org/10.1007/s10270-012-0250-3
  - _The GReTL Transformation Language_, Tassilo Horn and Jürgen Ebert, In:
    Theory and Practice of Model Transformations - 4th International
    Conference, ICMT 2011, http://dx.doi.org/10.1007/978-3-642-21732-6_11
  - _Solving the TTC 2011 Compiler Optimization Case with GReTL_, Tassilo Horn,
    TTC 2011, http://arxiv.org/abs/1111.4745v1
  - _Solving the TTC 2011 Reengineering Case with GReTL_, Tassilo Horn, TTC
    2011, http://arxiv.org/abs/1111.4747
  - _Saying Hello World with GReTL - A Solution to the TTC 2011 Instructive
    Case_, Tassilo Horn, TTC 2011, http://arxiv.org/abs/1111.4762
  - _Reverse Engineering Using Graph Queries_, Jürgen Ebert and Daniel
    Bildhauer, In: Schürr, Andy; Lewerentz, Claus; Engels, Gregor; Schäfer,
    Wilhelm; Westfechtel, Bernhard: Graph Transformations and Model Driven
    Engineering, 2010, http://uni-koblenz.de/~ist/documents/Ebert2010REU.pdf

## License

Copyright (C) 2006-2013 The JGraLab Team <ist@uni-koblenz.de>

Distributed under the
[General Public License, Version 3](http://www.gnu.org/copyleft/gpl.html) with
the following additional grant:

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

