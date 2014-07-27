# hiera-explorer

A web-app that lets you explore your hiera files.

## Overview

Hiera is a tool by PuppetLabs that lets you organize configuration
data in hierarchically organized files (usually in yaml format). For
more information, see the 
[Hiera documentation](http://docs.puppetlabs.com/hiera/1/).

While Hiera offers a lot of flexibility, you quickly end up with your
configuration data spread across dozens of yaml files. Figuring out
which files and values are used in any given context can be difficult.

This is where hiera-explorer comes in. It analyzes your `hiera.yaml`
config file and your data files. For a given scope (e.g. values of the
relevant facts), it shows you the load order of data files, the
content of all the relevant data files and the merged data, taking
into account the hierarchy.

## Installation

### Pre-Requisites

hiera-explorer is a JVM web application, you need a Java Runtime
Environment version 1.6 or later to run it.

### From Pre-Built Binaries

Download the hiera-explorer standalone jar file from the
[Releases](https://github.com/christianberg/hiera-explorer/releases)
section of the Github project.

To start the app, run

```
java -jar hiera-explorer-0.1.0-standalone.jar
```

### From Source

hiera-explorer is written in Clojure, to build it from source you need
to have [Leiningen](http://leiningen.org) installed. To run the app
directly from the source checkout, run

```
lein ring server
```

To build a standalone jar file including all the dependencies, run

```
lein uberjar
```

This jar file can be used as shown in the previous section.

## Configuration

All configuration is currently done via environment variables, which
need to be set when the application is started. There is no config
file.

The following variables can be set:

### `HIERA_CONFIG`

Name of the hiera config file to load. The path can be absolute or
relative to the directory from which the application was started.

Defaults to `resources/example/hiera.yaml` (an example provided with
the source code).

It is usually enough to set only this variable.

### `HIERA_DATADIR`

Path of the directory where the hiera data files are located.

If this variable is not set, the directory is read from the `:yaml ->
:datadir` entry in the hiera config file.

If a relative path is given (either in the environment variable of the
hiera config file), it is interpreted relative to the location of the
hiera config file.

### `PORT`

Port for the embedded HTTP server to list on.

Defaults to `3000`.

## Known Issues

### Data File Format

Currently only YAML files are supported as hiera data files.

### Lookup Types

Only the Priority lookup type is used for displaying the merged data,
Array Merge and Hash Merge lookups are currently not represented. (See
the
[documentation on Hiera lookup types](http://docs.puppetlabs.com/hiera/1/lookup_types.html).)

### Not Based on Hiera Code

Hiera is not actually used to look up any values, so there may be
differences in the lookup logic between hiera and hiera-explorer. If
you come across such a case, please file a bug report!

## Contributing

Bug reports, pull requests and general feedback are very welcome!
Please make use of Github issues or ping me on Twitter (@cdberg).

## License

Copyright © 2014 Christian Berg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
